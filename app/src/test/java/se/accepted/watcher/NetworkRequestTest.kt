package se.accepted.watcher

import app.cash.turbine.test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.accepted.watcher.model.PostsResponse
import se.accepted.watcher.model.PostsResponseItem
import se.accepted.watcher.network.PostsService
import se.accepted.watcher.ui.main.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class NetworkRequestTest {

    private val mockWebServer
        get() = MockWebServer()


    private val okHttpClient
        get() =
            OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .build()

    private fun postsService(
        mockWebServer: MockWebServer,
        okHttpClient: OkHttpClient,
    ): PostsService =
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PostsService::class.java)

    private fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
        val inputStream = javaClass.classLoader?.getResourceAsStream("api-response/$fileName")

        val source = inputStream?.let { inputStream.source().buffer() }
        source?.let {
            enqueue(
                MockResponse()
                    .setResponseCode(code)
                    .setBody(source.readString(StandardCharsets.UTF_8))
            )
        }
    }

    private fun expectedPostResponse(): PostsResponse {
        val item = PostsResponseItem(userId = 1,
            id = 1,
            title = "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
            body = "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
        )
        val expected = PostsResponse().also {
            it.add(item)
        }
        return expected
    }


    @Test
    fun itShouldReturnThePostsState() = runTest {
        //Given
        val mockWebServer = mockWebServer
        val httpClient = okHttpClient
        val postsService = postsService(mockWebServer, httpClient)

        val actor = TheActor.run {
            postsActor(postsService).toActor(PostsState.NotFetched)
        }

        val job = actor.start()

        //When

        AppStream.messages.test {
            val posts = CompletableDeferred<PostsState>()
            AppStream.send(GetPostsState(posts))
            val postState = posts.await()

            //Then
            assertEquals(PostsState.NotFetched, postState)
            job.cancel()
            cancelAndConsumeRemainingEvents()
        }
    }


    @Test
    fun itShouldFetchPostData() = runTest {
        //Given
        val jsonFileName = "postsresponse.json"
        val mockWebServer = mockWebServer
        val httpClient = okHttpClient
        val postsService = postsService(mockWebServer, httpClient)
        mockWebServer.enqueueResponse(jsonFileName, 200)

        val actor = TheActor.run {
            postsActor(postsService).toActor(PostsState.NotFetched)
        }

        val job = actor.start()

        //When

        AppStream.messages.test {
            val posts = CompletableDeferred<PostsState>()
            AppStream.send(FetchPosts)

            //Then
            AppStream.send(GetPostsState(posts))
            val postState = posts.await()

            assertEquals(expectedPostResponse(), (postState as PostsState.Success).posts)
            job.cancel()
            cancelAndConsumeRemainingEvents()
        }
    }


    @After
    fun teardown() {
        mockWebServer.shutdown()
    }


}
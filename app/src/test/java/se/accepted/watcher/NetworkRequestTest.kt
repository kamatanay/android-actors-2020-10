package se.accepted.watcher

import app.cash.turbine.test
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.accepted.watcher.AppStream.send
import se.accepted.watcher.model.PostsResponse
import se.accepted.watcher.network.PostsService
import se.accepted.watcher.ui.main.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class NetworkRequestTest {

    private val mockWebServer = MockWebServer()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }


    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()
    }

    private val postsService by lazy {
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PostsService::class.java)
    }

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

    private fun getPostsTestObject(fileName: String): PostsResponse? {
        val uri = javaClass.classLoader?.getResource("api-response/$fileName")
        uri?.let {
            return Gson().fromJson(String(File(it.path).readBytes()), PostsResponse::class.java)

        } ?: return null
    }


    @Test
    fun testItShouldTestPostsApi() {
        val jsonFileName = "postsresponse.json"
        mockWebServer.enqueueResponse(jsonFileName, 200)
        runTest {
            val actual = postsService.getPostsTest()
            val expected = getPostsTestObject(jsonFileName)
            assert(expected != null)
            assertEquals(actual.first(), expected?.first())


        }
    }

    @Test
    fun itShouldSendMessageToFetchPosts() = runTest {
        val vm = PostsViewModel()
        launch(Dispatchers.Main) {
            AppStream.messages.test {
                assertEquals(awaitItem(), FetchPosts)
                val message = awaitItem()
                if (message is GetPostsState) {
                    message.state.complete(PostsState.Error)
                }

            }


        }

        vm.sharedPostUiFlow.test {
            vm.fetchPosts()
            assertEquals(awaitItem(), PostsFetchFailed)
        }


    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }


}
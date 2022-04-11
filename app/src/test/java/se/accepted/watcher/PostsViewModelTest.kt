package se.accepted.watcher

import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import se.accepted.watcher.model.PostsResponse
import se.accepted.watcher.model.PostsResponseItem
import se.accepted.watcher.ui.main.*

class PostsViewModelTest {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
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
    fun itShouldFetchPostsAndReturnPostsResponse() = runTest {
        val vm = PostsViewModel()
        launch(Dispatchers.Main) {
            AppStream.messages.test {
                assertEquals(awaitItem(), FetchPosts)
                val message = awaitItem()
                if (message is GetPostsState) {
                    message.state.complete(PostsState.Success(expectedPostResponse()))
                }
            }

        }
        vm.sharedPostUiFlow.test {
            vm.fetchPosts()
            val uiMessage = awaitItem()
            assertEquals(expectedPostResponse(), (uiMessage as PostsFetched).posts)
        }
    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}
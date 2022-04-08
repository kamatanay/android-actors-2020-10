package se.accepted.watcher.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import se.accepted.watcher.AppStream.send
import se.accepted.watcher.UIMessage
import se.accepted.watcher.model.PostsResponse

data class PostsFetched(val posts: PostsResponse) : UIMessage
object PostsFetchFailed : UIMessage

class PostsViewModel : ViewModel() {

    private val postFlow = MutableSharedFlow<UIMessage>()
    val sharedPostUiFlow = postFlow.asSharedFlow()

    suspend fun fetchPosts() {
        send(FetchPosts)
        checkPosts()
    }

    private suspend fun checkPosts() {
        when (val state = getPosts()) {
            PostsState.Error -> {
                postFlow.emit(PostsFetchFailed)
            }
            is PostsState.Success -> {
                postFlow.emit(PostsFetched(state.posts))
            }
        }
    }

    private suspend fun getPosts(): PostsState {
        val postSate = CompletableDeferred<PostsState>()
        send(GetPostsState(state = postSate))
        return postSate.await()
    }

}
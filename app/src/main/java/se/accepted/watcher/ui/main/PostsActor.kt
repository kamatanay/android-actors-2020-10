package se.accepted.watcher.ui.main

import kotlinx.coroutines.CompletableDeferred
import se.accepted.watcher.Message
import se.accepted.watcher.State
import se.accepted.watcher.model.PostsResponse
import se.accepted.watcher.network.PostsService


object FetchPosts : Message
data class GetPostsState(val state: CompletableDeferred<PostsState> = CompletableDeferred()) :
    Message

sealed class PostsState : State {
    data class Success(val posts: PostsResponse) : PostsState()
    object Error : PostsState()
    object NotFetched : PostsState()
}

fun postsActor(postsService: PostsService): ActorFunction<PostsState> {

    suspend fun getPosts(): PostsState {
        return try {
            val response = postsService.getPosts()
            PostsState.Success(response)
        } catch (e: Exception) {
            PostsState.Error
        }
    }

    tailrec suspend fun TheActorScope.function(state: PostsState) {
        when (val message = channel.receive()) {

            is FetchPosts -> {
                val posts = getPosts()
                send(posts)
                function(posts)
            }

            is GetPostsState -> {
                message.state.complete(state)
                function(state)
            }

            else -> function(state)
        }
    }

    return TheActorScope::function


}
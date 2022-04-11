package se.accepted.watcher.network

import retrofit2.http.GET
import se.accepted.watcher.model.PostsResponse

interface PostsService {

    @GET("/posts")
    suspend fun getPosts(): PostsResponse

}
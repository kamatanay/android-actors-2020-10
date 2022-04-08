package se.accepted.watcher.model


import com.google.gson.annotations.SerializedName

data class PostsResponseItem(
    @SerializedName("userId")
    val userId: Int? = null,
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("body")
    val body: String? = null
)
package ru.netology.nmedia.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryRoomImpl(
    private val postsLiveData: MutableLiveData<FeedModel>
) : PostRepository {

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999"
        val gson = Gson()
        val postsType: Type? = object : TypeToken<List<Post>>() {}.type
        val jsonType = "application/json".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun getAll(): List<Post> {
        val call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts")
                .build()
        )
        val response = call.execute()
        val responseText = response.body.string()
        return gson.fromJson(responseText, postsType)
    }

    override fun likeById(id: Long) {
        try {
            val currentFeed = postsLiveData.value ?: throw IllegalStateException("FeedModel is null")

            val post = currentFeed.posts.find { it.id == id }
                ?: throw IllegalStateException("Post with id $id not found")

            val request = if (post.likedByMe) {
                Request.Builder()
                    .url("$BASE_URL/api/posts/$id/likes")
                    .delete()
                    .build()
            } else {
                Request.Builder()
                    .url("$BASE_URL/api/posts/$id/likes")
                    .post(RequestBody.EMPTY)
                    .build()
            }

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw RuntimeException("Server error: ${response.code} ${response.message}")
            }

            val updatedPost = gson.fromJson(response.body.string(), Post::class.java)
                ?: throw RuntimeException("Empty response body")

            val newPosts = currentFeed.posts.map { if (it.id == id) updatedPost else it }
            val newFeed = currentFeed.copy(posts = newPosts)

            postsLiveData.postValue(newFeed)
        } catch (e: Exception) {
            Log.e("NET_E", e.stackTraceToString())
        }
    }

    override fun save(post: Post): Post {
        val call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts")
                .post(gson.toJson(post).toRequestBody(jsonType))
                .build()
        )

        val response = call.execute()
        val responseText = response.body.string()
        return gson.fromJson(responseText, Post::class.java)
    }

    override fun removeById(id: Long) {
        val call = client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts/$id")
                .delete()
                .build()
        )

        call.execute()
    }
}
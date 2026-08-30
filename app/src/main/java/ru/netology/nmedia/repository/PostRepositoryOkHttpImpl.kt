package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

class PostRepositoryOkHttpImpl: PostRepository {

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
        val request = Request.Builder()
            .url("$BASE_URL/api/posts")
            .build()

        client.newCall(
            request
        ).execute().use { response ->
            if (!response.isSuccessful)
                throw IOException("Unexpected code $response")

            return gson.fromJson(response.body.string(), postsType)
        }
    }

    override fun getPostById(id: Long): Post {
        val request = Request.Builder().url("$BASE_URL/api/posts/$id").build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            return gson.fromJson(response.body.string(), Post::class.java)
        }
    }

    override fun likeById(id: Long): Post {
        val currentPost = getPostById(id)
        val request = if (currentPost.likedByMe) {
            Request.Builder().url("$BASE_URL/api/posts/$id/likes").delete().build()
        } else {
            Request.Builder().url("$BASE_URL/api/posts/$id/likes").post(RequestBody.EMPTY).build()
        }

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Server error: ${response.code}")
            }

            val updatedPost = gson.fromJson(response.body.string(), Post::class.java)
            return updatedPost
        }
    }

    override fun save(post: Post): Post {
        client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts")
                .post(gson.toJson(post).toRequestBody(jsonType))
                .build()
        ).execute().use { response ->
            return gson.fromJson(response.body.string(), Post::class.java)
        }
    }

    override fun removeById(id: Long) {
        client.newCall(
            Request.Builder()
                .url("$BASE_URL/api/posts/$id")
                .delete()
                .build()
        ).execute().close()
    }
}
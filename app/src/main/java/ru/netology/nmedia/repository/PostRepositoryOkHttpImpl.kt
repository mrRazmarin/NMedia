package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.ApiConfig.BASE_URL
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryOkHttpImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        return client.newCall(request)
            .execute().body.string()
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }

    override fun likeById(id: Long) {
        // TODO: do this in homework
    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .execute()
            .close()
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/posts")
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body.string()
                    try {
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            })
    }

    // Новые асинхронные методы
    override fun likeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        val request = Request.Builder()
            .post(RequestBody.EMPTY) // или без тела
            .url("$BASE_URL/api/posts/$id/likes") // предположительный эндпоинт
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                callback(Result.success(Unit))
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }
        })
    }

    override fun saveAsync(post: Post, callback: (Result<Unit>) -> Unit) {
        val request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("$BASE_URL/api/posts")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                callback(Result.success(Unit))
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }
        })
    }

    override fun removeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        val request = Request.Builder()
            .delete()
            .url("$BASE_URL/api/posts/$id")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                callback(Result.success(Unit))
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }
        })
    }
}

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:9999"
    const val AVATARS_PATH = "/avatars/"
}

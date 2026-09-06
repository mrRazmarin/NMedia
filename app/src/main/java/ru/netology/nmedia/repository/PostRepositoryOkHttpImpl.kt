package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post


class PostRepositoryOkHttpImpl : PostRepository {
    override fun getAll(): List<Post> {
        return PostApi.service.getAll().execute().body().orEmpty()
    }

    override fun likeById(id: Long) {
        // TODO: do this in homework
    }

    override fun save(post: Post) {
        PostApi.service.save(post).execute()
    }

    override fun removeById(id: Long) {
        PostApi.service.deleteById(id = id)
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(
                    call: Call<List<Post>>,
                    response: Response<List<Post>>
                ) {
                    if (!response.isSuccessful){
                        callback.onError(RuntimeException(response.errorBody()?.string()))
                        return
                    }

                    callback.onSuccess(response.body().orEmpty())
                }

                override fun onFailure(
                    call: Call<List<Post>>,
                    throwable: Throwable
                ) {
                    callback.onError(throwable)
                }

            })
    }

    // Новые асинхронные методы
    override fun likeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        PostApi.service.like(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(RuntimeException("Like failed: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    override fun dislikeByIdAsync(
        id: Long,
        callback: (Result<Unit>) -> Unit
    ) {
        PostApi.service.dislike(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback(Result.success(Unit))
                } else {
                    callback(Result.failure(RuntimeException("Dislike failed: ${response.code()}")))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback(Result.failure(t))
            }
        })
    }

    override fun saveAsync(post: Post, callback: (Result<Unit>) -> Unit) {
        /*val request = Request.Builder()
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
        })*/
    }

    override fun removeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        /*val request = Request.Builder()
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
        })*/
    }
}

object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:9999"
    const val AVATARS_PATH = "/avatars/"
}

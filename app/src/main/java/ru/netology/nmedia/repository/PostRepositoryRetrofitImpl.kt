package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import kotlin.collections.orEmpty


class PostRepositoryRetrofitImpl : PostRepository {
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
                    if (!response.isSuccessful) {
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

    override fun saveAsync(post: Post, callback: (Result<Post>) -> Unit) {
        PostApi.service.save(post).enqueue(
            object : Callback<Post> {
                override fun onResponse(
                    call: Call<Post>,
                    response: Response<Post>
                ) {
                    if (!response.isSuccessful) {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        callback(Result.failure(RuntimeException("Server error: ${response.code()} - $errorMsg")))
                    } else {
                        val body = response.body()
                        if (body != null) {
                            callback(Result.success(body))
                        } else {
                            callback(Result.failure(RuntimeException("Response body is null")))
                        }
                    }
                }

                override fun onFailure(
                    call: Call<Post>,
                    throwable: Throwable
                ) {
                    callback(Result.failure(throwable))
                }
            }
        )
    }

    override fun likeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        PostApi.service.like(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                callback(response.toUnitResult())
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

    override fun removeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit) {
        PostApi.service.deleteById(id = id).enqueue(
            object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    callback(response.toUnitResult())
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback(Result.failure(t))
                }
            }
        )
    }

    private fun Response<*>.toUnitResult(): Result<Unit> {
        return if (isSuccessful) {
            Result.success(Unit)
        } else {
            val errorMsg = errorBody()?.string() ?: "Unknown error"
            val exception = when (code()) {
                in 400..499 -> RuntimeException("Client error: ${code()} - $errorMsg")
                in 500..599 -> RuntimeException("Server error: ${code()} - $errorMsg")
                else -> RuntimeException("Unexpected error: ${code()} - $errorMsg")
            }
            Result.failure(exception)
        }
    }
}

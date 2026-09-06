package ru.netology.nmedia.repository

import retrofit2.Callback
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun save(post: Post)
    fun removeById(id: Long)

    fun getAllAsync(callback: GetAllCallback)

    // Новые асинхронные методы с колбэками
    fun likeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit)
    fun dislikeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit)
    fun saveAsync(post: Post, callback: (Result<Post>) -> Unit)
    fun removeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Throwable) {}
    }
}

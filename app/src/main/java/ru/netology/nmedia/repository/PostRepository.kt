package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long)
    fun save(post: Post)
    fun removeById(id: Long)

    fun getAllAsync(callback: GetAllCallback)

    // Новые асинхронные методы с колбэками
    fun likeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit)
    fun saveAsync(post: Post, callback: (Result<Unit>) -> Unit)
    fun removeByIdAsync(id: Long, callback: (Result<Unit>) -> Unit)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }
}

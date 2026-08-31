package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long): Post
    fun getPostById(id: Long): Post
    fun save(post: Post): Post
    fun removeById(id: Long)
}

package ru.netology.nmedia.dto

import ru.netology.nmedia.api.ApiConfig

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val authorAvatar: String? = null,
)

fun Post.getAvatarUrl(): String? {
    return authorAvatar?.let {
        "${ApiConfig.BASE_URL}${ApiConfig.AVATARS_PATH}$it"
    }
}
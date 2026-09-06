package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .apply {
        if(BuildConfig.DEBUG) {
            addInterceptor (HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
    }
    .build()

private val retrofit = Retrofit.Builder()
    .client(client).addConverterFactory(GsonConverterFactory.create())
    .baseUrl("${ApiConfig.BASE_URL}${ApiConfig.BASE_PATH}")
    .build()
    .also {
        android.util.Log.d("PostApi", "baseUrl = ${it.baseUrl()}")
    }

interface PostApi {
    @GET("posts")
    fun getAll(): Call<List<Post>>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

    @DELETE("posts/{id}")
    fun deleteById(@Path("id") id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun like(@Path("id") id: Long): Call<Unit>

    @DELETE("posts/{id}/likes")
    fun dislike(@Path("id") id: Long): Call<Unit>

    companion object {
        val service: PostApi by lazy {
            retrofit.create()
        }
    }
}

object ApiConfig {
    const val BASE_URL = "${BuildConfig.BASE_URL}"
    const val BASE_PATH = "${BuildConfig.BASE_PATH}"
    const val AVATARS_PATH = "/avatars/"
}
package ru.netology.nmedia.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryOkHttpImpl
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

private val empty = Post(
    id = 0, author = "", content = "", published = "", likes = 0, likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated
    private val repository: PostRepository = PostRepositoryOkHttpImpl()

    init {
        load()
    }

    fun load() {
        thread {
            _data.postValue(FeedModel(loading = true))

            val state = try {
                val posts = repository.getAll()

                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (_: Exception) {
                FeedModel(error = true)
            }

            _data.postValue(state)
        }
    }

    fun save(content: String) {
        thread {
            edited.value?.let {
                val text = content.trim()
                if (it.content != text) {
                    repository.save(it.copy(content = text))
                }
                _postCreated.postValue(Unit)
            }

            edited.postValue(empty)
        }
    }

    fun edit(post: Post) {
        thread {
            try {
                edited.postValue(post)
            } catch (e: Exception) {
                Log.e("NET_E", e.stackTraceToString())
            }

        }
    }

    fun likeById(id: Long) {
        thread {
            try {
                val updatedPost = repository.likeById(id)

                Handler(Looper.getMainLooper()).post {
                    val current = _data.value ?: return@post
                    val newPost = current.posts.map {
                        if (it.id == id) updatedPost
                        else it
                    }
                    _data.value = current.copy(posts = newPost)
                }
            } catch (_: Exception) {

            }
        }
    }

    fun removeById(id: Long) {
        thread {
            repository.removeById(id)
            load()
        }
    }
}

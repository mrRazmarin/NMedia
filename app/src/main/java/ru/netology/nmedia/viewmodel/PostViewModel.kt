package ru.netology.nmedia.viewmodel

import android.app.Application
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
    id = 0,
    content = "",
    author = "",
    likedByMe = false,
    likes = 0,
    published = "",
    authorAvatar = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryOkHttpImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Throwable) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save(content: String) {
        val post = edited.value ?: return
        // Сохраняем локальную копию, чтобы избежать изменения во время асинхронного вызова
        val currentPost = post.copy(content = content)

        repository.saveAsync(currentPost) { result ->
            if (result.isSuccess) {
                _postCreated.postValue(Unit)
            }
            // В любом случае сбрасываем редактируемый пост
            edited.postValue(empty)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun toggleLike(post: Post) {
        val oldPosts = _data.value?.posts.orEmpty()
        val index = oldPosts.indexOfFirst { it.id == post.id }
        if (index == -1) return

        val updatedPost = post.copy(
            likedByMe = !post.likedByMe,
            likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
        )

        val newPosts = oldPosts.toMutableList().apply { set(index, updatedPost) }
        _data.value = _data.value?.copy(posts = newPosts)

        val callback: (Result<Unit>) -> Unit = { result ->
            if (result.isFailure) {
                // Откат при ошибке
                _data.value = _data.value?.copy(posts = oldPosts)
                // Можно также показать сообщение об ошибке через отдельный LiveData
            }
        }

        if (updatedPost.likedByMe) {
            repository.likeByIdAsync(post.id, callback)
        } else {
            repository.dislikeByIdAsync(post.id, callback)
        }
    }

    fun removeById(id: Long) {
        // Оптимистичное удаление
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        repository.removeByIdAsync(id) { result ->
            if (result.isFailure) {
                // Восстанавливаем список при ошибке
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }
}

package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryRetrofitImpl
import ru.netology.nmedia.util.SingleLiveEvent

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
    private val repository: PostRepository = PostRepositoryRetrofitImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _saveFinished = SingleLiveEvent<Unit>()
    val saveFinished: LiveData<Unit> = _saveFinished

    private val _error = SingleLiveEvent<String>()
    val error: LiveData<String> = _error

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
        if(content.trim() == post.content.trim()) {
            edited.postValue(empty)
            _saveFinished.postValue(Unit)
        } else {
            repository.saveAsync(currentPost) { result ->
                result.onSuccess {
                    val oldPosts = _data.value?.posts.orEmpty()
                    val index = oldPosts.indexOfFirst { it.id == currentPost.id }

                    if (index != -1) {
                        val newPosts = oldPosts.toMutableList().apply {
                            set(index, result.getOrNull() ?: currentPost)
                        }
                        _data.value = _data.value?.copy(posts = newPosts)
                    }
                    _saveFinished.postValue(Unit)
                }.onFailure {
                    _error.postValue(result.exceptionOrNull()?.message ?: "Ошибка сохранения")
                }
                edited.postValue(empty)
            }
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }
    fun editToEmpty() {
        edited.value = empty
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
                _data.value = _data.value?.copy(posts = oldPosts)
            }
        }

        if (updatedPost.likedByMe) {
            repository.likeByIdAsync(post.id, callback)
        } else {
            repository.dislikeByIdAsync(post.id, callback)
        }
    }

    fun removeById(id: Long) {
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

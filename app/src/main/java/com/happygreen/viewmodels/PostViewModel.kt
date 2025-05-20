package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class PostsUiState(
    val posts: List<Post> = emptyList(),
    val currentGroupId: Int? = null,
    val selectedPost: Post? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class PostViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    fun loadPosts(groupId: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentGroupId = groupId) }
            try {
                val response = RetrofitInstance.apiService.getPosts(groupId)
                if (response.isSuccessful) {
                    val posts = response.body() ?: emptyList()
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento dei post: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento dei post: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento dei post: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getPostDetails(postId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.getPost(postId)
                if (response.isSuccessful) {
                    val post = response.body()
                    _uiState.update { it.copy(selectedPost = post, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento del post: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento del post: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento del post: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createPost(
        title: String,
        content: String,
        groupId: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        locationName: String? = null,
        imageUrl: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newPost = Post(
                    id = 0, // Il server assegnerà l'ID effettivo
                    title = title,
                    content = content,
                    authorId = 0,  // Il server determinerà l'utente corrente
                    authorUsername = "", // Il server assegnerà il nome utente
                    groupId = groupId,
                    imageUrl = imageUrl,
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName,
                    createdAt = "", // Il server assegnerà la data
                    updatedAt = ""  // Il server assegnerà la data
                )

                val response = RetrofitInstance.apiService.createPost(newPost)

                if (response.isSuccessful) {
                    loadPosts(groupId) // Ricarica la lista aggiornata
                    onSuccess()
                } else {
                    onError("Errore nella creazione del post: ${response.message()}")
                }
            } catch (e: IOException) {
                onError("Errore di rete nella creazione del post: ${e.message}")
            } catch (e: Exception) {
                onError("Errore imprevisto nella creazione del post: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSelectedPost() {
        _uiState.update { it.copy(selectedPost = null) }
    }
}
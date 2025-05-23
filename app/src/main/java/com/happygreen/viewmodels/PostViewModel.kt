package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend_happygreen.utils.DateUtils
import com.happygreen.data.RetrofitInstance
import com.happygreen.data.TokenManager
import com.happygreen.models.CreatePostRequest
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

    // Ottieni il TokenManager per accedere alle informazioni dell'utente
    private val tokenManager = TokenManager.getInstance()

    fun loadPosts(groupId: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentGroupId = groupId) }
            try {
                val response = RetrofitInstance.apiService.getPosts(groupId)
                if (response.isSuccessful) {
                    val posts = response.body()?.results?.map { post ->
                        post.copy(
                            authorUsername = post.authorUsername ?: "Anonimo",
                            createdAt = post.createdAt ?: DateUtils.getCurrentDateString()
                        )
                    } ?: emptyList()
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
                // Crea direttamente la richiesta senza creare un oggetto Post completo
                val createPostRequest = CreatePostRequest(
                    title = title,
                    content = content,
                    group = groupId,  // Nota: usa 'group' non 'groupId'
                    latitude = latitude,
                    longitude = longitude,
                    locationName = locationName
                )

                val response = RetrofitInstance.apiService.createPost(createPostRequest)

                if (response.isSuccessful) {
                    // Dopo la creazione del post, aggiorniamo la lista locale
                    // per includere il nuovo post con le informazioni dell'utente corrente
                    response.body()?.let { createdPost ->
                        // Ottieni il nome utente corrente
                        val currentUsername = tokenManager.getUsername() ?: "Utente"
                        val currentDate = DateUtils.getCurrentDateString()

                        // Crea un post con le informazioni complete per l'UI
                        val completePost = createdPost.copy(
                            authorUsername = if (createdPost.authorUsername.isNullOrEmpty()) currentUsername else createdPost.authorUsername,
                            createdAt = createdPost.createdAt ?: currentDate,
                            updatedAt = createdPost.updatedAt ?: currentDate
                        )

                        // Aggiorna la lista dei post aggiungendo il nuovo post
                        _uiState.update { currentState ->
                            currentState.copy(
                                posts = listOf(completePost) + currentState.posts,
                                isLoading = false
                            )
                        }
                    }

                    // Ricarica anche la lista dal server per essere sicuri
                    loadPosts(groupId)
                    onSuccess()
                } else {
                    // Log dell'errore per debug
                    val errorBody = response.errorBody()?.string()
                    onError("Errore nella creazione del post: $errorBody")
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
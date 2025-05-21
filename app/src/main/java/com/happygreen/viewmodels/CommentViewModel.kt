
package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val currentPostId: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CommentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentPostId = postId) }
            try {
                val response = RetrofitInstance.apiService.getPostComments(postId)
                if (response.isSuccessful) {
                    val comments = response.body()?.results ?: emptyList()
                    _uiState.update { it.copy(comments = comments, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento dei commenti: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento dei commenti: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento dei commenti: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addComment(
        content: String,
        postId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newComment = Comment(
                    id = 0,  // Il server assegnerà l'ID effettivo
                    postId = postId,
                    authorId = 0,  // Il server determinerà l'utente corrente
                    authorUsername = "",  // Il server assegnerà il nome utente corretto
                    content = content,
                    createdAt = "",  // Il server assegnerà la data
                    updatedAt = ""  // Il server assegnerà la data
                )

                val response = RetrofitInstance.apiService.addComment(postId, newComment)

                if (response.isSuccessful) {
                    loadComments(postId) // Ricarica la lista aggiornata
                    onSuccess()
                } else {
                    onError("Errore nell'aggiunta del commento: ${response.message()}")
                }
            } catch (e: IOException) {
                onError("Errore di rete nell'aggiunta del commento: ${e.message}")
            } catch (e: Exception) {
                onError("Errore imprevisto nell'aggiunta del commento: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetState() {
        _uiState.value = CommentsUiState()
    }
}
package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.ApiService
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Badge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class BadgesUiState(
    val badges: List<Badge> = emptyList(),
    val selectedBadge: Badge? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BadgeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BadgesUiState())
    val uiState: StateFlow<BadgesUiState> = _uiState.asStateFlow()

    init {
        loadBadges()
    }

    fun loadBadges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.getBadges()
                if (response.isSuccessful) {
                    val badges = response.body() ?: emptyList()
                    _uiState.update { it.copy(badges = badges, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento dei badge: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento dei badge: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento dei badge: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectBadge(badge: Badge) {
        _uiState.update { it.copy(selectedBadge = badge) }
    }

    fun clearSelectedBadge() {
        _uiState.update { it.copy(selectedBadge = null) }
    }

    // Filtra i badge per categoria o livello di punti
    fun filterBadgesByRequiredPoints(minPoints: Int, maxPoints: Int): List<Badge> {
        return _uiState.value.badges.filter {
            it.pointsRequired in minPoints..maxPoints
        }.sortedBy { it.pointsRequired }
    }

    // Ottieni categorie di badge in base ai nomi
    fun getBadgeCategories(): List<String> {
        return _uiState.value.badges
            .map { extractCategory(it.name) }
            .distinct()
    }

    // Estrae la categoria dal nome del badge (es. "Eco-Detective" -> "Eco")
    private fun extractCategory(badgeName: String): String {
        val parts = badgeName.split("-", " ")
        return if (parts.size > 1 && parts[0].length > 2) parts[0] else "Generale"
    }
}
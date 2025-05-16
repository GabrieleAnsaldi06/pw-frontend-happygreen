package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Badge
import com.happygreen.models.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val availableBadges: List<Badge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadAvailableBadges()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.getMyProfile()
                if (response.isSuccessful) {
                    val profile = response.body()
                    _uiState.update { it.copy(userProfile = profile, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento del profilo: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento del profilo: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento del profilo: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadAvailableBadges() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getBadges()
                if (response.isSuccessful) {
                    val badges = response.body() ?: emptyList()
                    _uiState.update { it.copy(availableBadges = badges) }
                }
            } catch (e: Exception) {
                // Non impostiamo l'errore qui per non sovrascrivere eventuali errori
                // più importanti relativi al profilo utente
            }
        }
    }

    // Metodo per calcolare i badge che l'utente può ancora ottenere
    fun calculateUnlockedBadges(): List<Badge> {
        val profile = _uiState.value.userProfile ?: return emptyList()
        val availableBadges = _uiState.value.availableBadges

        // Filtriamo i badge già ottenuti
        val earnedBadgeIds = profile.badges.map { it.id }

        // Ritorniamo i badge che l'utente ha già sbloccato in base ai punti
        return availableBadges.filter {
            it.requiredPoints <= profile.points && !earnedBadgeIds.contains(it.id)
        }
    }

    // Metodo per calcolare i badge che l'utente può ancora ottenere
    fun calculateNextBadges(): List<Badge> {
        val profile = _uiState.value.userProfile ?: return emptyList()
        val availableBadges = _uiState.value.availableBadges

        // Filtriamo i badge già ottenuti
        val earnedBadgeIds = profile.badges.map { it.id }

        // Ritorniamo i badge che l'utente non ha ancora sbloccato
        return availableBadges.filter {
            it.requiredPoints > profile.points && !earnedBadgeIds.contains(it.id)
        }.sortedBy { it.requiredPoints }
    }

    fun refreshProfile() {
        loadUserProfile()
        loadAvailableBadges()
    }
}
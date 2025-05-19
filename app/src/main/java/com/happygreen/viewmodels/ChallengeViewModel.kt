package com.happygreen.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Challenge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val selectedChallenge: Challenge? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val completeSuccess: Boolean = false
)

class ChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        loadChallenges()
    }

    fun loadChallenges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, completeSuccess = false) }
            try {
                val response = RetrofitInstance.apiService.getChallenges()
                if (response.isSuccessful) {
                    val challenges = response.body() ?: emptyList()
                    _uiState.update { it.copy(challenges = challenges, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento delle sfide: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento delle sfide: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento delle sfide: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectChallenge(challenge: Challenge) {
        _uiState.update { it.copy(selectedChallenge = challenge) }
    }

    fun clearSelectedChallenge() {
        _uiState.update { it.copy(selectedChallenge = null) }
    }

    fun completeChallenge(challengeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, completeSuccess = false) }
            try {
                val response = RetrofitInstance.apiService.completeChallenge(challengeId)
                if (response.isSuccessful) {
                    // Aggiorna lo stato e ricarica le sfide per avere i dati aggiornati
                    _uiState.update { it.copy(completeSuccess = true) }
                    loadChallenges()
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel completamento della sfida: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel completamento della sfida: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel completamento della sfida: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // Filtra le sfide per punti
    fun filterChallengesByPoints(minPoints: Int, maxPoints: Int): List<Challenge> {
        return _uiState.value.challenges.filter {
            it.points in minPoints..maxPoints
        }.sortedBy { it.points }
    }

    // Recupera le sfide completate
    fun getCompletedChallenges(): List<Challenge> {
        return _uiState.value.challenges.filter { it.isCompleted }
    }

    // Recupera le sfide non completate
    fun getIncompleteChallenges(): List<Challenge> {
        return _uiState.value.challenges.filter { !it.isCompleted }
    }

    // Recupera le sfide attive (con deadline non scaduta o senza deadline)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getActiveChallenges(): List<Challenge> {
        val currentDate = java.time.LocalDate.now().toString()
        return _uiState.value.challenges.filter { challenge ->
            !challenge.isCompleted && (challenge.endDate > currentDate)
        }
    }
}
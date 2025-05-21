package com.happygreen.viewmodels

import android.os.Build
import android.util.Log
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
    val activeChallenges: List<Challenge> = emptyList(),
    val completedChallenges: List<Challenge> = emptyList(),
    val selectedChallenge: Challenge? = null,
    val isLoading: Boolean = false,
    val isLoadingActiveChallenges: Boolean = false,
    val isLoadingCompletedChallenges: Boolean = false,
    val error: String? = null,
    val completeSuccess: Boolean = false,
    val challengeCompletionStatus: Map<Int, Boolean> = emptyMap() // Aggiungiamo una mappa per tenere traccia dello stato di completamento
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
                    val body = response.body()
                    var challenges = emptyList<Challenge>()
                    if (body?.results != null)
                        challenges = body.results
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
                // Prima unisciti alla sfida (in caso non sia già iscritto)
                try {
                    val joinResponse = RetrofitInstance.apiService.joinChallenge(challengeId)
                    Log.d("ChallengeViewModel", "Join challenge response: ${joinResponse.code()}")
                } catch (e: Exception) {
                    Log.e("ChallengeViewModel", "Error joining challenge, might already be joined", e)
                    // Ignora errori di join, forse l'utente è già iscritto
                }

                // Poi completa la sfida
                val response = RetrofitInstance.apiService.completeChallenge(challengeId)
                if (response.isSuccessful) {
                    // Aggiorna lo stato e ricarica le sfide per avere i dati aggiornati
                    updateChallengeCompletionStatus(challengeId, true)
                    _uiState.update { it.copy(completeSuccess = true, isLoading = false) }

                    // Ricarica tutte le liste di sfide
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

    // Recupera le sfide completate, simile a getActiveChallenges
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCompletedChallenges(authViewModel: AuthViewModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCompletedChallenges = true) }
            try {
                val username = authViewModel.username.value
                val completedChallenges = mutableListOf<Challenge>()

                for (challenge in _uiState.value.challenges) {
                    try {
                        val participants = RetrofitInstance.apiService.getChallengeParticipantsByID(challenge.id).body()
                        Log.d("Partecipanti challenge completata " + challenge.id, participants.toString())

                        if (!participants.isNullOrEmpty()) {
                            for (participant in participants) {
                                if (participant.username == username && participant.completed) {
                                    completedChallenges.add(challenge)
                                    // Aggiorna anche lo stato nella mappa
                                    updateChallengeCompletionStatus(challenge.id, true)
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ChallengeViewModel", "Error checking participants for challenge ${challenge.id}", e)
                    }
                }

                _uiState.update {
                    it.copy(
                        completedChallenges = completedChallenges,
                        isLoadingCompletedChallenges = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading completed challenges", e)
                _uiState.update { it.copy(isLoadingCompletedChallenges = false) }
            }
        }
    }

    // Recupera le sfide non completate
    fun getIncompleteChallenges(): List<Challenge> {
        return _uiState.value.challenges.filter { !it.isCompleted }
    }

    // Funzione per aggiornare lo stato di completamento di una sfida nella mappa locale
    private fun updateChallengeCompletionStatus(challengeId: Int, isCompleted: Boolean) {
        val updatedMap = _uiState.value.challengeCompletionStatus.toMutableMap()
        updatedMap[challengeId] = isCompleted
        _uiState.update { it.copy(challengeCompletionStatus = updatedMap) }
    }

    // Verifica se una sfida è stata completata dall'utente corrente e restituisce il risultato attraverso il callback
    fun checkCompleted(challenge: Challenge, authViewModel: AuthViewModel, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val username = authViewModel.username.value

                // Se la sfida è già marcata come completata nello stato, restituisce true
                if (challenge.isCompleted) {
                    updateChallengeCompletionStatus(challenge.id, true)
                    callback(true)
                    return@launch
                }

                // Controlla se lo stato di completamento è già salvato nella mappa locale
                if (_uiState.value.challengeCompletionStatus.containsKey(challenge.id)) {
                    callback(_uiState.value.challengeCompletionStatus[challenge.id] ?: false)
                    return@launch
                }

                // Altrimenti, verifica tra i partecipanti
                val response = RetrofitInstance.apiService.getChallengeParticipantsByID(challenge.id)
                val participants = response.body()

                var isCompleted = false
                if (!participants.isNullOrEmpty()) {
                    for (participant in participants) {
                        if (participant.username == username && participant.completed) {
                            isCompleted = true
                            break
                        }
                    }
                }

                Log.d("ChallengeCheck", "Challenge ${challenge.id} completed by $username: $isCompleted")
                updateChallengeCompletionStatus(challenge.id, isCompleted)
                callback(isCompleted)
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error checking if challenge is completed", e)
                updateChallengeCompletionStatus(challenge.id, false)
                callback(false)
            }
        }
    }

    // Nuovo metodo per ottenere lo stato di completamento dalla mappa locale
    fun isChallengeCompleted(challengeId: Int): Boolean {
        return _uiState.value.challengeCompletionStatus[challengeId] ?: false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getActiveChallenges(authViewModel: AuthViewModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingActiveChallenges = true) }
            try {
                val username = authViewModel.username.value
                val activeChallenges = mutableListOf<Challenge>()

                Log.d("Challenges", _uiState.value.challenges.toString())
                Log.d("username", username.toString())

                for (challenge in _uiState.value.challenges) {
                    try {
                        val response = RetrofitInstance.apiService.getChallengeParticipantsByID(challenge.id)
                        val participants = response.body()
                        Log.d("partecipanti challenge" + challenge.id, participants.toString())

                        var isUserParticipating = false
                        var isUserCompleted = false

                        // Controlla se l'utente è tra i partecipanti e se ha completato la sfida
                        if (!participants.isNullOrEmpty()) {
                            for (participant in participants) {
                                if (participant.username == username) {
                                    isUserParticipating = true
                                    isUserCompleted = participant.completed
                                    // Aggiorna lo stato di completamento nella mappa locale
                                    updateChallengeCompletionStatus(challenge.id, participant.completed)
                                    break
                                }
                            }
                        }

                        // Aggiungi alle sfide attive se l'utente partecipa ma non ha completato
                        // oppure se non c'è nessun partecipante ancora
                        if ((isUserParticipating && !isUserCompleted) || participants.isNullOrEmpty()) {
                            activeChallenges.add(challenge)
                        }
                    } catch (e: Exception) {
                        Log.e("ChallengeViewModel", "Error checking participants for challenge ${challenge.id}", e)
                    }
                }

                _uiState.update {
                    it.copy(
                        activeChallenges = activeChallenges,
                        isLoadingActiveChallenges = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ChallengeViewModel", "Error loading active challenges", e)
                _uiState.update { it.copy(isLoadingActiveChallenges = false) }
            }
        }
    }
}
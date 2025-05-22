package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import com.happygreen.models.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class GroupsUiState(
    val groups: List<Group> = emptyList(),
    val selectedGroup: Group? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class GroupViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.getGroups()
                if (response.isSuccessful) {
                    val groups = response.body()?.results?.map { group ->
                        // Ensure members is never null
                        group.copy(members = group.members ?: emptyList())
                    } ?: emptyList()
                    _uiState.update { it.copy(groups = groups, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento dei gruppi: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore nel caricamento dei gruppi: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun getGroupDetails(groupId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.getGroup(groupId)
                if (response.isSuccessful) {
                    val group = response.body()
                    _uiState.update { it.copy(selectedGroup = group, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Errore nel caricamento del gruppo: ${response.message()}",
                            isLoading = false
                        )
                    }
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        error = "Errore di rete nel caricamento del gruppo: ${e.message}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Errore imprevisto nel caricamento del gruppo: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun createGroup(name: String, description: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newGroup = Group(
                    id = 0, // Il server assegnerà l'ID effettivo
                    name = name,
                    description = description,
                    creatorId = 0,   // Il server determinerà l'utente corrente
                    createdAt = "",  // Il server assegnerà la data
                    updatedAt = "",  // Il server assegnerà la data
                    members = emptyList()
                )
                val response = RetrofitInstance.apiService.createGroup(newGroup)

                if (response.isSuccessful) {
                    loadGroups() // Ricarica la lista aggiornata
                    onSuccess()
                } else {
                    onError("Errore nella creazione del gruppo: ${response.message()}")
                }
            } catch (e: IOException) {
                onError("Errore di rete nella creazione del gruppo: ${e.message}")
            } catch (e: Exception) {
                onError("Errore imprevisto nella creazione del gruppo: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun joinGroup(groupId: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = RetrofitInstance.apiService.joinGroup(groupId)

                if (response.isSuccessful) {
                    // Aggiorna il gruppo selezionato se era quello a cui ci si è uniti
                    if (_uiState.value.selectedGroup?.id == groupId) {
                        getGroupDetails(groupId)
                    }
                    loadGroups() // Ricarica la lista aggiornata
                    onSuccess()
                } else {
                    onError("Errore nell'unirsi al gruppo: ${response.message()}")
                }
            } catch (e: IOException) {
                onError("Errore di rete nell'unirsi al gruppo: ${e.message}")
            } catch (e: Exception) {
                onError("Errore imprevisto nell'unirsi al gruppo: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearSelectedGroup() {
        _uiState.update { it.copy(selectedGroup = null) }
    }
}
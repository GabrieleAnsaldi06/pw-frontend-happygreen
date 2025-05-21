package com.example.frontend_happygreen.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.models.Challenge
import com.happygreen.viewmodels.AuthViewModel
import com.happygreen.viewmodels.ChallengeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChallengeScreen(
    authViewModel: AuthViewModel,
    challengeViewModel: ChallengeViewModel = viewModel()
) {
    val uiState by challengeViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Le Tue Sfide",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Si è verificato un errore",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { challengeViewModel.loadChallenges() }) {
                        Text("Riprova")
                    }
                }
            }
        } else {
            // Tab layout per sfide attive e completate
            var selectedTabIndex by remember { mutableStateOf(0) }

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = {
                        selectedTabIndex = 0
                        challengeViewModel.getActiveChallenges(authViewModel)
                    },
                    text = { Text("Sfide Attive") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = {
                        selectedTabIndex = 1
                        challengeViewModel.getCompletedChallenges(authViewModel)
                    },
                    text = { Text("Completate") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> ActiveChallengesList(
                    authViewModel = authViewModel,
                    challengeViewModel = challengeViewModel,
                    onChallengeClick = { challengeViewModel.selectChallenge(it) },
                    onCompleteChallenge = { challengeViewModel.completeChallenge(it.id) }
                )
                1 -> CompletedChallengesList(
                    authViewModel = authViewModel,
                    challengeViewModel = challengeViewModel,
                    onChallengeClick = { challengeViewModel.selectChallenge(it) }
                )
            }
        }
    }

    // Dialog per visualizzare i dettagli della sfida selezionata
    if (uiState.selectedChallenge != null) {
        var showCompleteButton by remember { mutableStateOf(false) }

        // Verifica lo stato di completamento della sfida selezionata
        LaunchedEffect(uiState.selectedChallenge) {
            challengeViewModel.checkCompleted(uiState.selectedChallenge!!, authViewModel) { isCompleted ->
                showCompleteButton = !isCompleted
            }
        }

        ChallengeDetailsDialog(
            challenge = uiState.selectedChallenge!!,
            onDismiss = { challengeViewModel.clearSelectedChallenge() },
            onComplete = { challengeViewModel.completeChallenge(uiState.selectedChallenge!!.id) },
            showCompleteButton = showCompleteButton
        )
    }

    // Snackbar per mostrare messaggi di completamento
    LaunchedEffect(uiState.completeSuccess) {
        if (uiState.completeSuccess) {
            // In una vera implementazione, qui si mostrerebbe uno Snackbar tramite ScaffoldState
            // Per ora, lasciamo solo un placeholder per l'effetto
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActiveChallengesList(
    authViewModel: AuthViewModel,
    challengeViewModel: ChallengeViewModel,
    onChallengeClick: (Challenge) -> Unit,
    onCompleteChallenge: (Challenge) -> Unit
) {
    val uiState by challengeViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        challengeViewModel.getActiveChallenges(authViewModel)
    }

    if (uiState.isLoadingActiveChallenges || uiState.isLoadingCompletedChallenges) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val challenges = uiState.activeChallenges

        if (challenges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nessuna sfida attiva disponibile",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(challenges) { challenge ->
                    var showCompleteButton by remember { mutableStateOf(false) }

                    // Verifica se la sfida è completata utilizzando il callback
                    LaunchedEffect(challenge.id) {
                        challengeViewModel.checkCompleted(challenge, authViewModel) { isCompleted ->
                            showCompleteButton = !isCompleted
                        }
                    }

                    ChallengeItem(
                        challenge = challenge,
                        onClick = { onChallengeClick(challenge) },
                        showCompleteButton = showCompleteButton,
                        onComplete = { onCompleteChallenge(challenge) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompletedChallengesList(
    authViewModel: AuthViewModel,
    challengeViewModel: ChallengeViewModel,
    onChallengeClick: (Challenge) -> Unit
) {
    val uiState by challengeViewModel.uiState.collectAsState()

    // Carica le sfide completate all'inizio
    LaunchedEffect(Unit) {
        challengeViewModel.getCompletedChallenges(authViewModel)
    }

    if (uiState.isLoadingCompletedChallenges) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val challenges = uiState.completedChallenges

        if (challenges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Nessuna sfida completata",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(challenges) { challenge ->
                    ChallengeItem(
                        challenge = challenge,
                        onClick = { onChallengeClick(challenge) },
                        showCompleteButton = false,
                        onComplete = { }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(
    challenge: Challenge,
    onClick: () -> Unit,
    showCompleteButton: Boolean,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${challenge.points} punti",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                challenge.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                challenge.endDate?.let {
                    Text(
                        "Scadenza: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                } ?: Text(
                    "Nessuna scadenza",
                    style = MaterialTheme.typography.bodySmall
                )

                if (showCompleteButton) {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Completa")
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeDetailsDialog(
    challenge: Challenge,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    showCompleteButton: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(challenge.title)
        },
        text = {
            Column {
                Text(challenge.description)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Punti: ${challenge.points}")
                challenge.endDate?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scadenza: $it")
                }
            }
        },
        confirmButton = {
            if (showCompleteButton) {
                Button(onClick = {
                    onComplete()
                    onDismiss()
                }) {
                    Text("Completa Sfida")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}
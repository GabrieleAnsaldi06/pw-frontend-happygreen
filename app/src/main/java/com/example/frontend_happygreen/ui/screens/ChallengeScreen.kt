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
import com.happygreen.viewmodels.ChallengeViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChallengeScreen(
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
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Sfide Attive") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Completate") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTabIndex) {
                0 -> ChallengesList(
                    challenges = challengeViewModel.getActiveChallenges(),
                    onChallengeClick = { challengeViewModel.selectChallenge(it) },
                    showCompleteButton = true,
                    onCompleteChallenge = { challengeViewModel.completeChallenge(it.id) }
                )
                1 -> ChallengesList(
                    challenges = challengeViewModel.getCompletedChallenges(),
                    onChallengeClick = { challengeViewModel.selectChallenge(it) },
                    showCompleteButton = false
                )
            }
        }
    }

    // Dialog per visualizzare i dettagli della sfida selezionata
    if (uiState.selectedChallenge != null) {
        ChallengeDetailsDialog(
            challenge = uiState.selectedChallenge!!,
            onDismiss = { challengeViewModel.clearSelectedChallenge() },
            onComplete = { challengeViewModel.completeChallenge(uiState.selectedChallenge!!.id) },
            showCompleteButton = !uiState.selectedChallenge!!.isCompleted
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

@Composable
fun ChallengesList(
    challenges: List<Challenge>,
    onChallengeClick: (Challenge) -> Unit,
    showCompleteButton: Boolean,
    onCompleteChallenge: (Challenge) -> Unit = {}
) {
    if (challenges.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nessuna sfida disponibile",
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
                    showCompleteButton = showCompleteButton && !challenge.isCompleted,
                    onComplete = { onCompleteChallenge(challenge) }
                )
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
package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.theme.*
import com.example.frontend_happygreen.utils.DateUtils.formatDate
import com.happygreen.models.Group
import com.happygreen.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    groupViewModel: GroupViewModel = viewModel(),
    onGroupClick: (Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by groupViewModel.uiState.collectAsState()
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        groupViewModel.loadGroups()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LavenderBlush,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header personalizzato
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(DelftBlue, VistaBlue)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "I Miei Gruppi",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { showCreateGroupDialog = true },
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Crea gruppo",
                            tint = Color.White
                        )
                    }
                }
            }

            // Contenuto principale
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Caricamento gruppi...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    uiState.error != null -> {
                        ErrorCardGroup(
                            error = uiState.error,
                            onRetry = { groupViewModel.loadGroups() }
                        )
                    }

                    uiState.groups.isEmpty() -> {
                        EmptyGroupsCard(
                            onCreateGroup = { showCreateGroupDialog = true }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.groups) { group ->
                                EnhancedGroupCard(
                                    group = group,
                                    onClick = { onGroupClick(group.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB con gradiente
        FloatingActionButton(
            onClick = { showCreateGroupDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(Saffron, TicklePink)
                    ),
                    CircleShape
                ),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Crea nuovo gruppo",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showCreateGroupDialog) {
        EnhancedCreateGroupDialog(
            onDismiss = { showCreateGroupDialog = false },
            onConfirm = { name, description ->
                groupViewModel.createGroup(
                    name = name,
                    description = description,
                    onSuccess = { showCreateGroupDialog = false },
                    onError = { /* Handle error */ }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedGroupCard(
    group: Group,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White,
                            LavenderBlush.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(VistaBlue, DelftBlue)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DelftBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = DelftBlue.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Saffron.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Saffron
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${group.membersCount} membri",
                                style = MaterialTheme.typography.labelMedium,
                                color = Saffron,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = group.createdAt?.let { formatDate(it) } ?: "Data non disponibile",
                        style = MaterialTheme.typography.labelSmall,
                        color = DelftBlue.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyGroupsCard(onCreateGroup: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(TicklePink.copy(alpha = 0.2f), Saffron.copy(alpha = 0.2f))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = DelftBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Nessun gruppo trovato",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DelftBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crea un nuovo gruppo o unisciti a uno esistente per iniziare a condividere con altri utenti",
                style = MaterialTheme.typography.bodyMedium,
                color = DelftBlue.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateGroup,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DelftBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Crea nuovo gruppo",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorCardGroup(error: String?, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ErrorRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Errore",
                tint = ErrorRed,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Si è verificato un errore",
                style = MaterialTheme.typography.titleMedium,
                color = ErrorRed,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error ?: "Errore sconosciuto",
                style = MaterialTheme.typography.bodyMedium,
                color = ErrorRed.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Text("Riprova")
            }
        }
    }
}

@Composable
fun EnhancedCreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                "Crea nuovo gruppo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DelftBlue
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nome gruppo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DelftBlue,
                        focusedLabelColor = DelftBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    label = { Text("Descrizione") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DelftBlue,
                        focusedLabelColor = DelftBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(groupName, groupDescription) },
                enabled = groupName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DelftBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crea", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DelftBlue
                )
            ) {
                Text("Annulla", fontWeight = FontWeight.Medium)
            }
        }
    )
}
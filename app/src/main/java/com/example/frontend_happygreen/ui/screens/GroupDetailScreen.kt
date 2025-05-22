package com.example.frontend_happygreen.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.models.Group
import com.happygreen.viewmodels.GroupViewModel
import com.happygreen.viewmodels.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: Int,
    groupViewModel: GroupViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel(),
    onBackClick: () -> Unit,
    onPostClick: (Int) -> Unit,
    onCreatePost: () -> Unit
) {
    val groupState by groupViewModel.uiState.collectAsState()
    val postState by postViewModel.uiState.collectAsState()

    val group = groupState.selectedGroup
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Carica i dettagli del gruppo e i post
    LaunchedEffect(groupId) {
        groupViewModel.getGroupDetails(groupId)
        postViewModel.loadPosts(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Dettagli gruppo") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                actions = {
                    // Mostra l'opzione per unirsi se l'utente non è membro
                    if (group != null && !isMember(group)) {
                        IconButton(onClick = { showJoinDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Unisciti al gruppo"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Mostra il FAB solo se l'utente è membro del gruppo
            if (group != null && isMember(group)) {
                FloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Crea nuovo post"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Gestione stati di caricamento e errore per il gruppo
            if (groupState.isLoading && group == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (groupState.error != null && group == null) {
                ErrorView(
                    error = groupState.error,
                    onRetry = { groupViewModel.getGroupDetails(groupId) }
                )
            } else if (group == null) {
                Text(
                    text = "Gruppo non trovato",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                // Contenuto principale quando il gruppo è caricato
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Spazio per il FAB
                ) {
                    // Info gruppo
                    item {
                        GroupInfoCard(group)
                    }

                    // Intestazione sezione post
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Post del gruppo",
                                style = MaterialTheme.typography.titleLarge
                            )

                            if (isMember(group)) {
                                IconButton(onClick = { showCreatePostDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Crea post"
                                    )
                                }
                            }
                        }
                    }

                    // Gestione stato di caricamento/errore/vuoto per i post
                    when {
                        postState.isLoading && postState.posts.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        postState.error != null && postState.posts.isEmpty() -> {
                            item {
                                ErrorCard(
                                    error = postState.error,
                                    onRetry = { postViewModel.loadPosts(groupId) }
                                )
                            }
                        }
                        postState.posts.isEmpty() -> {
                            item {
                                EmptyPostsView(
                                    isMember = isMember(group),
                                    onCreatePost = { showCreatePostDialog = true }
                                )
                            }
                        }
                        else -> {
                            // Lista dei post
                            items(postState.posts) { post ->
                                PostItem(
                                    post = post,
                                    onClick = { onPostClick(post.id) }
                                )
                            }
                        }
                    }
                }

                // Indicator di caricamento durante refresh
                if (postState.isLoading && postState.posts.isNotEmpty()) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }
            }
        }
    }

    // Dialog per unirsi al gruppo
    if (showJoinDialog && group != null) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Unisciti al gruppo") },
            text = {
                Text("Vuoi unirti al gruppo \"${group.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        groupViewModel.joinGroup(
                            groupId = group.id,
                            onSuccess = {
                                showJoinDialog = false
                                Toast.makeText(context, "Ti sei unito al gruppo!", Toast.LENGTH_SHORT).show()
                                // Ricarica i dettagli del gruppo
                                groupViewModel.getGroupDetails(groupId)
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("Unisciti")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per creare un nuovo post
    if (showCreatePostDialog && group != null) {
        CreatePostDialog(
            groupId = groupId,
            postViewModel = postViewModel,
            onDismiss = { showCreatePostDialog = false },
            onPostCreated = {
                showCreatePostDialog = false
                Toast.makeText(context, "Post creato con successo!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun GroupInfoCard(group: Group) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Informazioni sul gruppo",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberCountChip(memberCount = group.membersCount)

                Text(
                    text = "Creato il: ${group.createdAt?.let { formatDate(it) }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCountChip(memberCount: Int) {
    AssistChip(
        onClick = { /* Potrebbe aprire una lista dei membri */ },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        label = {
            Text("$memberCount membri")
        }
    )
}

@Composable
fun EmptyPostsView(isMember: Boolean, onCreatePost: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Article,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nessun post ancora",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isMember) "Sii il primo a condividere qualcosa!" else "Unisciti al gruppo per poter pubblicare",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isMember) {
                Button(onClick = onCreatePost) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crea post")
                }
            }
        }
    }
}

@Composable
fun ErrorView(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Errore",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Si è verificato un errore",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error ?: "Errore sconosciuto",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Riprova")
        }
    }
}

@Composable
fun ErrorCard(error: String?, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Errore",
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Errore nel caricamento dei post",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = error ?: "Errore sconosciuto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Riprova")
            }
        }
    }
}

// Helper function to determine if the current user is a member of the group
// In un'applicazione reale, si verificherebbe con l'ID utente autenticato
fun isMember(group: Group): Boolean {
    // Per questa implementazione, assumiamo che l'utente sia membro se il gruppo ha membri
    // In una vera app, confronteresti l'ID dell'utente corrente con gli ID dei membri
    return group.members?.isNotEmpty() ?: false
}
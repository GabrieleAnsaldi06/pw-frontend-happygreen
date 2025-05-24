package com.example.frontend_happygreen.ui.screens

import android.widget.Toast
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.utils.DateUtils.formatDate
import com.happygreen.viewmodels.CommentViewModel
import com.happygreen.viewmodels.PostViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    postViewModel: PostViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCommentClick: (Int) -> Unit
) {
    val uiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Carica i dettagli del post
    LaunchedEffect(postId) {
        postViewModel.getPostDetails(postId)
    }

    val post = uiState.selectedPost

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = post?.title ?: "Dettaglio post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Torna indietro"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (post != null) {
                                onCommentClick(post.id)
                            }
                        },
                        enabled = post != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Commenti"
                        )
                    }

                    IconButton(
                        onClick = {
                            if (post != null) {
                                // Implementazione della condivisione
                                Toast.makeText(context, "Condivisione non ancora implementata", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = post != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Condividi"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Errore di caricamento",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "Si è verificato un errore",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { postViewModel.getPostDetails(postId) }
                    ) {
                        Text(text = "Riprova")
                    }
                }
            } else if (post != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pubblicato da: ${post.authorUsername ?: "Sconosciuto"}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "• ${formatDate(post.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (post.imageUrl != null) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Immagine del post",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (post.locationName != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 ${post.locationName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onCommentClick(post.id) },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Visualizza commenti")
                    }
                }
            }
        }
    }
}
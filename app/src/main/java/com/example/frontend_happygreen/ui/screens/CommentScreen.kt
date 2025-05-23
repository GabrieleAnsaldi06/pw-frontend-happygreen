package com.example.frontend_happygreen.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.models.Comment
import com.happygreen.models.Post
import com.happygreen.viewmodels.CommentViewModel
import com.happygreen.viewmodels.PostViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: Int,
    onBack: () -> Unit,
    commentViewModel: CommentViewModel = viewModel(),
    postViewModel: PostViewModel = viewModel()
) {
    val commentUiState by commentViewModel.uiState.collectAsState()
    val postUiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    // Carica il post e i commenti quando lo schermo viene visualizzato
    LaunchedEffect(postId) {
        postViewModel.getPostDetails(postId)
        commentViewModel.loadComments(postId)
    }

    // Clean up when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            commentViewModel.resetState()
            postViewModel.clearSelectedPost()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = postUiState.selectedPost?.title ?: "Commenti",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Torna indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Scrivi un commento...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        enabled = !isSubmitting,
                        minLines = 1,
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp)
                    )

                    FilledIconButton(
                        onClick = {
                            if (commentText.trim().isBlank()) {
                                Toast.makeText(context, "Il commento non può essere vuoto", Toast.LENGTH_SHORT).show()
                                return@FilledIconButton
                            }

                            isSubmitting = true
                            scope.launch {
                                commentViewModel.addComment(
                                    content = commentText.trim(),
                                    postId = postId,
                                    onSuccess = {
                                        commentText = ""
                                        isSubmitting = false
                                        Toast.makeText(context, "Commento aggiunto!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { error ->
                                        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        isSubmitting = false
                                    }
                                )
                            }
                        },
                        enabled = commentText.trim().isNotBlank() && !isSubmitting,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Invia commento",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                postUiState.isLoading && postUiState.selectedPost == null -> {
                    // Caricamento iniziale del post
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Caricamento post...")
                    }
                }

                postUiState.error != null && postUiState.selectedPost == null -> {
                    // Errore nel caricamento del post
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Errore nel caricamento del post",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = postUiState.error ?: "Errore sconosciuto",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { postViewModel.getPostDetails(postId) }
                        ) {
                            Text("Riprova")
                        }
                    }
                }

                postUiState.selectedPost != null -> {
                    // Post caricato con successo
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header del post
                        PostDetailHeader(postUiState.selectedPost!!)

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        // Sezione commenti
                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                commentUiState.isLoading && commentUiState.comments.isEmpty() -> {
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Caricamento commenti...")
                                    }
                                }

                                commentUiState.error != null && commentUiState.comments.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Errore nel caricamento dei commenti",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = commentUiState.error ?: "Errore sconosciuto",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { commentViewModel.loadComments(postId) }
                                        ) {
                                            Text("Riprova")
                                        }
                                    }
                                }

                                commentUiState.comments.isEmpty() -> {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "💬",
                                            fontSize = 48.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Ancora nessun commento",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Sii il primo a commentare questo post!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                else -> {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Header con conteggio commenti
                                        item {
                                            Text(
                                                text = "${commentUiState.comments.size} commenti",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }

                                        items(
                                            items = commentUiState.comments,
                                            key = { comment -> comment.id }
                                        ) { comment ->
                                            CommentItem(comment = comment)
                                        }
                                    }

                                    // Indicatore di caricamento durante il refresh
                                    if (commentUiState.isLoading) {
                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .align(Alignment.TopCenter)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostDetailHeader(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "di ${post.authorUsername}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = formatPostDate(post.createdAt ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )

            if (post.locationName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.locationName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.authorUsername,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = formatCommentDate(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 18.sp
            )
        }
    }
}

// Utilità per formattare le date dei commenti
fun formatCommentDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return dateString

        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInMinutes = diffInMillis / (1000 * 60)
        val diffInHours = diffInMinutes / 60
        val diffInDays = diffInHours / 24

        when {
            diffInMinutes < 1 -> "Ora"
            diffInMinutes < 60 -> "${diffInMinutes}m"
            diffInHours < 24 -> "${diffInHours}h"
            diffInDays < 7 -> "${diffInDays}g"
            else -> {
                val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}

// Utilità per formattare le date dei post
fun formatPostDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return dateString

        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}
package com.example.frontend_happygreen.ui.components

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.screens.CreatePostDialog
import com.happygreen.viewmodels.PostViewModel
import kotlinx.coroutines.launch

/**
 * Componente riutilizzabile per il pulsante di creazione post
 * Da inserire nella topBar di una Scaffold o come FloatingActionButton
 */
@Composable
fun CreatePostIconButton(
    groupId: Int,
    postViewModel: PostViewModel = viewModel(),
    onPostCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Crea post"
        )
    }

    if (showDialog) {
        CreatePostDialog(
            groupId = groupId,
            postViewModel = postViewModel,
            onDismiss = { showDialog = false },
            onPostCreated = {
                showDialog = false
                onPostCreated()
                Toast.makeText(context, "Post creato con successo!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Componente riutilizzabile per il FloatingActionButton di creazione post
 */
@Composable
fun CreatePostFloatingActionButton(
    groupId: Int,
    postViewModel: PostViewModel = viewModel(),
    onPostCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    FloatingActionButton(onClick = { showDialog = true }) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Crea post"
        )
    }

    if (showDialog) {
        CreatePostDialog(
            groupId = groupId,
            postViewModel = postViewModel,
            onDismiss = { showDialog = false },
            onPostCreated = {
                showDialog = false
                onPostCreated()
                Toast.makeText(context, "Post creato con successo!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
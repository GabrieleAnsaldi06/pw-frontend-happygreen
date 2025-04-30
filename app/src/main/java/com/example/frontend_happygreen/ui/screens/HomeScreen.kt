package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    username: String,
    onNavigateToCamera: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Benvenuto, $username!", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onNavigateToCamera, modifier = Modifier.fillMaxWidth()) {
            Text("Scanner Oggetti")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onNavigateToQuiz, modifier = Modifier.fillMaxWidth()) {
            Text("Quiz e Sfide")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onNavigateToMap, modifier = Modifier.fillMaxWidth()) {
            Text("Mappa Condivisioni")
        }
    }
}

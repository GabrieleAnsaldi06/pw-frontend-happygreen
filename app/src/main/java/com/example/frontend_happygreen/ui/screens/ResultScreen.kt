package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(score: Int, total: Int, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quiz Completato!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hai risposto correttamente a $score su $total domande.")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Ricomincia")
        }
    }
}

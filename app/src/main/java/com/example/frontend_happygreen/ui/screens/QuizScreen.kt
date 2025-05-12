package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happygreen.viewmodels.QuizViewModel

@Composable
fun QuizScreen(
    quizViewModel: QuizViewModel = viewModel()
) {
    val uiState by quizViewModel.uiState.collectAsState()

    if (uiState.isQuizFinished) {
        ResultScreen(
            score = uiState.score,
            total = uiState.questions.size,
            onRestart = { quizViewModel.restartQuiz() }
        )
    } else {
        val currentQuestion = uiState.questions[uiState.currentIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Domanda ${uiState.currentIndex + 1} di ${uiState.questions.size}", style = MaterialTheme.typography.titleMedium)
            Text(currentQuestion.question, style = MaterialTheme.typography.titleLarge)

            currentQuestion.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.selectedAnswerIndex == index,
                            onClick = { quizViewModel.selectAnswer(index) }
                        )
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = uiState.selectedAnswerIndex == index,
                        onClick = { quizViewModel.selectAnswer(index) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(option)
                }
            }

            Button(
                onClick = { quizViewModel.nextQuestion() },
                enabled = uiState.selectedAnswerIndex != -1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (uiState.currentIndex == uiState.questions.lastIndex)
                        "Fine"
                    else
                        "Avanti"
                )
            }
        }
    }
}

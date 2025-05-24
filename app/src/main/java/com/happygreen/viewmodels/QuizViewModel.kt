package com.happygreen.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happygreen.data.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class QuizUiState(
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswerIndex: Int = -1,
    val score: Int = 0,
    val isQuizFinished: Boolean = false
)

class QuizViewModel : ViewModel() {

    init {
        loadQuizzes()
    }

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState

    private fun loadQuizzes() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.apiService.getQuizzes()
                if (response.isSuccessful) {
                    val quizzes = response.body()?.results ?: emptyList()
                    if (quizzes.isNotEmpty()) {
                        _uiState.update { it.copy(questions = quizzes) }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun selectAnswer(index: Int) {
        _uiState.update { it.copy(selectedAnswerIndex = index) }
    }

    fun nextQuestion() {
        val current = _uiState.value

        val isCorrect = current.selectedAnswerIndex == current.questions[current.currentIndex].correctAnswerIndex
        val newScore = if (isCorrect) current.score + 1 else current.score

        if (current.currentIndex < current.questions.lastIndex) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedAnswerIndex = -1,
                    score = newScore
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    score = newScore,
                    isQuizFinished = true
                )
            }
        }
    }

    fun restartQuiz() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedAnswerIndex = -1,
                score = 0,
                isQuizFinished = false
            )
        }
    }
}

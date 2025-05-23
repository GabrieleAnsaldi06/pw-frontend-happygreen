package com.happygreen.models

import java.util.Date

data class PaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)
// Modello per il profilo utente
data class UserProfile(
    val id: Int,
    val username: String,
    val bio: String = "",
    val avatarUrl: String? = null,
    val points: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val badges: List<Badge>? = null
)

// Modello per i badge/achievements
data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val pointsRequired: Int
)

// Modello per i gruppi
data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val creatorId: Int,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val members: List<GroupMembership>? = null
) {
    // Add a helper property to safely get members
    val membersList: List<GroupMembership>
        get() = members ?: emptyList()

    val membersCount: Int
        get() = membersList.size
}

// Modello per l'appartenenza ai gruppi
data class GroupMembership(
    val userId: Int,
    val username: String,
    val groupId: Int,
    val role: String,
    val joinedAt: String
)

data class CreatePostRequest(
    val title: String,
    val content: String,
    val group: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null
)

// Modello per i post
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val authorId: Int,
    val authorUsername: String? = null,
    val groupId: Int,
    val imageUrl: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// Modello per i commenti
data class Comment(
    val id: Int,
    val postId: Int,
    val authorId: Int,
    val authorUsername: String,
    val content: String,
    val createdAt: String,
    val updatedAt: String
)

// Modello per gli oggetti riconosciuti
data class RecognizedObject(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val eco_impact: String,
    val recycling_info: String,
    val sustainability_score: Int,
    val imageUrl: String? = null
)

// Modello per le registrazioni delle scansioni
data class ScanRecord(
    val id: Int,
    val userId: Int,
    val recognizedObjectId: Int,
    val imageUrl: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val createdAt: String
)

// Modello per i quiz
data class Quiz(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val questions: List<QuizQuestion> = emptyList()
)

// Modello per le domande del quiz
data class QuizQuestion(
    val id: Int,
    val quizId: Int,
    val question: String,
    val options: List<QuizOption> = emptyList()
)

// Modello per le opzioni del quiz
data class QuizOption(
    val id: Int,
    val questionId: Int,
    val text: String,
    val isCorrect: Boolean
)

// Modello per i tentativi di quiz
data class QuizAttempt(
    val id: Int,
    val userId: Int,
    val quizId: Int,
    val score: Int,
    val completed: Boolean,
    val startedAt: String,
    val completedAt: String? = null
)

// Modello per le sfide
data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val startDate: String,
    val endDate: String,
    val createdAt: String,
    val isParticipating: Boolean = false,
    val isCompleted: Boolean = false
)

// Modello per la partecipazione alle sfide
data class ChallengeParticipation(
    val id: Int,
    val username: String,
    val challengeId: Int,
    val completed: Boolean,
    val joinedAt: String,
    val completedAt: String? = null
)

// Modello per i prodotti scansionabili via barcode
data class Product(
    val id: Int,
    val barcode: String,
    val name: String,
    val description: String,
    val manufacturer: String = "",
    val ecoFriendly: Boolean,
    val recyclable: Boolean,
    val sustainabilityScore: Int,
    val ecoInfo: String,
    val alternatives: String = "",
    val imageUrl: String? = null
)

// Modello per le scansioni dei prodotti
data class ProductScan(
    val id: Int,
    val userId: Int,
    val productId: Int,
    val createdAt: String
)
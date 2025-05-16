package com.happygreen.models

// Modello per i gruppi
data class Group(
    val id: Int,
    val name: String,
    val description: String,
    val createdAt: String,
    val createdBy: Int,
    val members: List<Int> = emptyList()
)

// Modello per i post
data class Post(
    val id: Int,
    val content: String,
    val imageUrl: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val createdBy: Int,
    val groupId: Int,
    val username: String? = null
)

// Modello per i commenti
data class Comment(
    val id: Int,
    val content: String,
    val createdAt: String,
    val createdBy: Int,
    val postId: Int,
    val username: String? = null
)

// Modello per gli oggetti riconosciuti
data class RecognizedObject(
    val id: Int,
    val name: String,
    val description: String,
    val sustainabilityInfo: String,
    val recyclable: Boolean,
    val category: String
)

// Modello per i badge/achievements
data class Badge(
    val id: Int,
    val name: String,
    val description: String,
    val iconUrl: String,
    val requiredPoints: Int
)

// Modello per i prodotti scansionabili via barcode
data class Product(
    val id: Int,
    val name: String,
    val barcode: String,
    val brand: String,
    val sustainabilityScore: Int,
    val ecoFriendly: Boolean,
    val sustainabilityInfo: String
)

// Modello per il profilo utente
data class UserProfile(
    val id: Int,
    val username: String,
    val email: String,
    val points: Int,
    val badges: List<Badge> = emptyList()
)

// Modello per la sfida
data class Challenge(
    val id: Int,
    val title: String,
    val description: String,
    val points: Int,
    val deadline: String?,
    val completed: Boolean = false
)
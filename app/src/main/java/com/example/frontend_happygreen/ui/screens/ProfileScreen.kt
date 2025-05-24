package com.example.frontend_happygreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend_happygreen.ui.theme.*
import com.happygreen.models.Badge
import com.happygreen.viewmodels.AuthViewModel
import com.happygreen.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit,
    onNavigateToBadges: () -> Unit,
    authViewModel: AuthViewModel
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val profile = uiState.userProfile

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
        profileViewModel.loadAvailableBadges()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DelftBlue,
                        VistaBlue,
                        LavenderBlush
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header con logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Caricamento profilo...",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            } else if (profile != null) {
                // Avatar con gradiente
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Saffron, TicklePink)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.username.first().toString().uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Username
                Text(
                    text = "@${profile.username}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Bio se disponibile
                if (profile.bio.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Card punti con design moderno
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        LavenderBlush.copy(alpha = 0.5f)
                                    ),
                                    radius = 800f
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Saffron
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Punti Eco",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DelftBlue
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = profile.points.toString(),
                                style = MaterialTheme.typography.displayLarge,
                                color = DelftBlue,
                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Progress bar con gradiente
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .background(
                                        DelftBlue.copy(alpha = 0.1f),
                                        RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(calculateLevelProgress(profile.points))
                                        .height(12.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(Saffron, TicklePink)
                                            ),
                                            RoundedCornerShape(6.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Livello ${calculateLevel(profile.points)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = DelftBlue
                                )

                                Text(
                                    text = "Prossimo: ${(calculateLevel(profile.points) * 100)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DelftBlue.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Card badge con contenuto migliorato
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        EnhancedBadgesSection(
                            badges = profile.badges ?: emptyList(),
                            nextBadges = profileViewModel.calculateNextBadges()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            } else if (uiState.error != null) {
                // Error message con design migliorato
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = ErrorRed
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Si è verificato un errore: ${uiState.error}",
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { profileViewModel.refreshProfile() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRed
                            )
                        ) {
                            Text("Riprova")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedBadgesSection(
    badges: List<Badge>,
    nextBadges: List<Badge>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "I Tuoi Badge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DelftBlue
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Saffron.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${badges.size}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = Saffron,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (badges.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = LavenderBlush.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = DelftBlue.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Non hai ancora guadagnato nessun badge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DelftBlue.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completa sfide e quiz per ottenerli!",
                        style = MaterialTheme.typography.bodySmall,
                        color = DelftBlue.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(badges) { badge ->
                    EnhancedBadgeItem(badge, isEarned = true)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Prossimi Badge",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DelftBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (nextBadges.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = SuccessGreen.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Complimenti! Hai sbloccato tutti i badge disponibili",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nextBadges.take(3)) { badge ->
                    EnhancedBadgeItem(badge, isEarned = false)
                }
            }
        }
    }
}

@Composable
fun EnhancedBadgeItem(badge: Badge, isEarned: Boolean) {
    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) Saffron.copy(alpha = 0.1f) else DelftBlue.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = badge.name,
                modifier = Modifier.size(32.dp),
                tint = if (isEarned) Saffron else DelftBlue.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (isEarned) DelftBlue else DelftBlue.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            if (!isEarned) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${badge.pointsRequired} pt",
                    style = MaterialTheme.typography.labelSmall,
                    color = TicklePink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun calculateLevel(points: Int): Int = (points / 100) + 1
fun calculateLevelProgress(points: Int): Float {
    val level = calculateLevel(points)
    val pointsForCurrentLevel = (level - 1) * 100
    val pointsForNextLevel = level * 100
    return (points - pointsForCurrentLevel).toFloat() / (pointsForNextLevel - pointsForCurrentLevel)
}
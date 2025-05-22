package com.example.frontend_happygreen.nav

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.frontend_happygreen.ui.screens.*
import com.happygreen.viewmodels.*

/**
 * Grafo di navigazione completo per l'app HappyGreen
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompleteNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== HOME SECTION ==========
        composable("home") {
            val username by authViewModel.username.collectAsState()

            HomeScreen(
                onNavigateToCamera = { navController.navigate("scan") },
                onNavigateToQuiz = { navController.navigate("quiz") },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToChallenges = { navController.navigate("challenges") },
                onNavigateToGroups = { navController.navigate("groups") },
                onLogout = {
                    authViewModel.logout()
                },
                username = username ?: "Utente"
            )
        }

        // ========== SCAN SECTION ==========
        composable("scan") {
            CameraScanScreen(
                onNavigateToBarcodeScan = { navController.navigate("barcode_scan") },
                onScanResult = { navController.navigate("scan_result") }
            )
        }

        composable("scan_result") {
            ObjectScanResultScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("barcode_scan") {
            BarcodeScanScreen(
                onBack = { navController.popBackStack() },
                onScanResult = {
                    // Gestisci il risultato del barcode
                    navController.popBackStack()
                }
            )
        }

        // ========== MAP SECTION ==========
        composable("map") {
            MapScreen()
        }

        // ========== CHALLENGES SECTION ==========
        composable("challenges") {
            ChallengeScreen(authViewModel)
        }

        // ========== QUIZ SECTION ==========
        composable("quiz") {
            QuizScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ========== GROUPS SECTION ==========
        composable("groups") {
            GroupsScreen(
                onGroupClick = { groupId ->
                    navController.navigate("group_detail/$groupId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "group_detail/{groupId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: -1
            val groupViewModel: GroupViewModel = viewModel()
            val postViewModel: PostViewModel = viewModel()

            GroupDetailScreen(
                groupId = groupId,
                groupViewModel = groupViewModel,
                postViewModel = postViewModel,
                onBackClick = { navController.popBackStack() },
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                },
                onCreatePost = { navController.navigate("create_post/$groupId") }
            )
        }

        // ========== POSTS SECTION ==========
        composable(
            route = "post_detail/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: -1
            val postViewModel: PostViewModel = viewModel()

            PostDetailScreen(
                postId = postId,
                postViewModel = postViewModel,
                onBackClick = { navController.popBackStack() },
                onCommentClick = {
                    navController.navigate("comments/$postId")
                }
            )
        }

        composable(
            route = "create_post/{groupId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: -1
            val postViewModel: PostViewModel = viewModel()

            CreatePostScreen(
                groupId = groupId,
                postViewModel = postViewModel,
                onBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "comments/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: -1

            CommentsScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }

        // ========== PROFILE SECTION ==========
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                },
                onNavigateToBadges = { navController.navigate("badges") },
                authViewModel = authViewModel
            )
        }

        composable("badges") {
            BadgesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Grafo di navigazione per l'autenticazione
 */
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { },  // AuthViewModel gestisce la navigazione
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Utility per definire le rotte dell'app
 */
object AppRoutes {
    // Main screens
    const val HOME = "home"
    const val SCAN = "scan"
    const val MAP = "map"
    const val CHALLENGES = "challenges"
    const val PROFILE = "profile"

    // Auth screens
    const val LOGIN = "login"
    const val REGISTER = "register"

    // Secondary screens
    const val QUIZ = "quiz"
    const val SCAN_RESULT = "scan_result"
    const val BARCODE_SCAN = "barcode_scan"
    const val GROUPS = "groups"
    const val BADGES = "badges"

    // Parametric routes
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val POST_DETAIL = "post_detail/{postId}"
    const val CREATE_POST = "create_post/{groupId}"
    const val COMMENTS = "comments/{postId}"

    // Helper functions
    fun groupDetail(groupId: Int) = "group_detail/$groupId"
    fun postDetail(postId: Int) = "post_detail/$postId"
    fun createPost(groupId: Int) = "create_post/$groupId"
    fun comments(postId: Int) = "comments/$postId"
}
package com.example.frontend_happygreen

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.frontend_happygreen.nav.PostNavigation
import com.example.frontend_happygreen.ui.screens.*
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme
import com.happygreen.data.TokenManager
import com.happygreen.data.RetrofitInstance
import com.happygreen.viewmodels.*

class MainActivity : ComponentActivity() {
    @SuppressLint("ViewModelConstructorInComposable")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inizializza TokenManager (se non è già stato fatto nell'Application)
        try {
            TokenManager.getInstance()
        } catch (e: IllegalStateException) {
            // Se TokenManager non è stato inizializzato nell'Application, inizializzalo qui
            TokenManager.initialize(applicationContext)
        }

        // Imposta il token di autenticazione se esiste
        TokenManager.getInstance().getAccessToken()?.let { token ->
            RetrofitInstance.setAuthToken(token)
        }

        setContent {
            FrontendhappygreenTheme {
                val navController = rememberNavController()
                val authViewModel = AuthViewModel(application)

                // Check if user is authenticated
                val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

                if (isAuthenticated) {
                    // Main app scaffold with bottom navigation
                    MainAppScaffold(
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                } else {
                    // Authentication flow
                    AuthNavHost(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainAppScaffold(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Scan,
        Screen.Map,
        Screen.Challenges,
        Screen.Profile
    )

    var selectedItem by remember { mutableStateOf(0) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCamera = { navController.navigate(Screen.Scan.route) },
                    onNavigateToQuiz = { navController.navigate("quiz") },
                    onNavigateToMap = { navController.navigate(Screen.Map.route) },
                    onNavigateToChallenges = { navController.navigate(Screen.Challenges.route) },
                    onLogout = onLogout,
                    username = authViewModel.username.collectAsState().value ?: "Utente"
                )
            }
            composable(Screen.Scan.route) {
                CameraScanScreen()
            }
            composable(Screen.Map.route) {
                MapScreen()
            }
            composable(Screen.Challenges.route) {
                ChallengeScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout
                )
            }
            composable("quiz") {
                QuizScreen()
            }
            composable("scan_result") {
                ObjectScanResultScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("barcode_scan") {
                BarcodeScanScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("groups") {
                GroupsScreen(
                    onGroupClick = { groupId ->
                        navController.navigate("group_detail/$groupId")
                    }
                )
            }
            composable("group_detail/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: -1
                GroupDetailScreen(
                    groupId = groupId,
                    onBackClick = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate("post_detail/$postId") // Aggiungi la navigazione al post
                    }
                )
            }
            composable("group_detail/{groupId}") { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId")?.toIntOrNull() ?: -1
                PostNavigation(
                    startDestination = "group_detail/$groupId",
                    onBackToGroups = { navController.popBackStack() },
                    groupViewModel = viewModel(),
                    postViewModel = viewModel()
                )
            }
            composable("post_detail/{postId}") { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                PostDetailScreen(
                    postId = postId,
                    onBackClick = { navController.popBackStack() },
                    onCommentClick = {
                        navController.navigate("comment_screen/$postId")
                    }
                )
            }
            composable("badges") {
                BadgesScreen()
            }
        }
    }
}

@Composable
fun AuthNavHost(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { },  // AuthViewModel will handle navigation
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

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Scan : Screen("scan", "Scan", Icons.Default.CameraAlt)
    object Map : Screen("map", "Mappa", Icons.Default.Map)
    object Challenges : Screen("challenges", "Sfide", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "Profilo", Icons.Default.Person)
}
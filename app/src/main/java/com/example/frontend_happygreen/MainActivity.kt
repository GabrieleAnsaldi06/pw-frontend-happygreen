package com.example.frontend_happygreen

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
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
                    AuthNavHost(authViewModel = authViewModel)
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
        Screen.Barcode,
        Screen.Groups,
        Screen.Map,
        Screen.Challenges,
        Screen.Profile,
        Screen.Quiz
    )

    var selectedItem by remember { mutableStateOf(0) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Update selected item based on current destination
    LaunchedEffect(currentDestination?.route) {
        val currentRoute = currentDestination?.route
        selectedItem = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: selectedItem
    }

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
            // Schermata Home
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCamera = { navController.navigate(Screen.Scan.route) },
                    onNavigateToQuiz = { navController.navigate("quiz") },
                    onNavigateToMap = { navController.navigate(Screen.Map.route) },
                    onNavigateToChallenges = { navController.navigate(Screen.Challenges.route) },
                    onNavigateToGroups = { navController.navigate(Screen.Groups.route) },
                    onLogout = onLogout,
                    username = authViewModel.username.collectAsState().value ?: "Utente"
                )
            }

            // Schermata Scan principale
            composable(Screen.Scan.route) {
                CameraScanScreen(
                    onNavigateToBarcodeScan = { navController.navigate(Screen.Barcode.route) },
                    onScanResult = { navController.navigate("scan_result") }
                )
            }

            // Schermata Barcode Scan (ora accessibile direttamente dalla navbar)
            composable(Screen.Barcode.route) {
                BarcodeScanScreen(
                    onBack = { navController.navigate(Screen.Home.route) },
                    onScanResult = {
                        // Gestisci il risultato del barcode e torna alla home
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            // Schermata Gruppi (ora accessibile direttamente dalla navbar)
            composable(Screen.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId ->
                        navController.navigate("group_detail/$groupId")
                    },
                    onBack = { navController.navigate(Screen.Home.route) }
                )
            }

            // Schermata Mappa
            composable(Screen.Map.route) {
                MapScreen()
            }

            // Schermata Sfide
            composable(Screen.Challenges.route) {
                ChallengeScreen(authViewModel)
            }

            // Schermata Profilo
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToBadges = { navController.navigate("badges") },
                    authViewModel = authViewModel
                )
            }

            // Quiz Screen
            composable("quiz") {
                QuizScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Risultato scan oggetti
            composable("scan_result") {
                ObjectScanResultScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // Dettaglio gruppo
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
                    onBackClick = { navController.navigate(Screen.Groups.route) },
                    onPostClick = { postId ->
                        navController.navigate("post_detail/$postId")
                    },
                    onCreatePost = { navController.navigate("create_post/$groupId") }
                )
            }

            // Dettaglio post
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

            // Creazione post
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

            // Commenti del post
            composable(
                route = "comments/{postId}",
                arguments = listOf(
                    navArgument("postId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val postId = backStackEntry.arguments?.getInt("postId") ?: -1

                CommentsScreen(
                    postId = postId,
                    onBack = { navController.popBackStack() },
                )
            }

            // Badges/Distintivi
            composable("badges") {
                BadgesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun AuthNavHost(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

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

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Scan : Screen("scan", "Scan", Icons.Default.CameraAlt)
    object Barcode : Screen("barcode_scan", "Barcode", Icons.Default.QrCodeScanner)
    object Groups : Screen("groups", "Gruppi", Icons.Default.Group)
    object Map : Screen("map", "Mappa", Icons.Default.Map)
    object Challenges : Screen("challenges", "Sfide", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "Profilo", Icons.Default.Person)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Quiz)
}
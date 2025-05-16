package com.example.frontend_happygreen.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend_happygreen.ui.screens.*
import com.happygreen.ui.screens.LoginScreen
import com.happygreen.ui.screens.RegisterScreen
import com.happygreen.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val username by authViewModel.username.collectAsState()

            HomeScreen(
                onNavigateToCamera = { navController.navigate("camera") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                username = username ?: "Utente",
                onNavigateToQuiz = { navController.navigate("quiz") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }

        composable("camera") {
            CameraScanScreen()
        }

        composable("quiz") {
            QuizScreen()
        }

        composable("map") {
            MapScreen()
        }
    }
}
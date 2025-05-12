package com.example.frontend_happygreen.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.frontend_happygreen.ui.screens.*
import com.happygreen.ui.screens.LoginScreen
import com.happygreen.ui.screens.RegisterScreen
import com.happygreen.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("home") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onRegisterSuccess = { navController.navigate("home") }
            )
        }

        composable("home") {
            val authViewModel: AuthViewModel = viewModel()
            val username by authViewModel.username.collectAsState()

            HomeScreen(
                onNavigateToCamera = { navController.navigate("camera") },
                onLogout = { navController.navigate("login") },
                username = username ?: "Anonimo",
                onNavigateToQuiz = { navController.navigate("quiz") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }


        composable("scan") {
            CameraScanScreen()
        }

        composable("quiz") {
            QuizScreen()
        }


        composable("map") {
            MapScreen()        }
    }
}

package com.example.frontend_happygreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.frontend_happygreen.nav.AppNavGraph
import com.example.frontend_happygreen.ui.theme.FrontendhappygreenTheme
import com.happygreen.viewmodels.AuthViewModel
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrontendhappygreenTheme {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides this
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
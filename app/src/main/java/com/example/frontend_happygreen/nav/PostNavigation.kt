package com.example.frontend_happygreen.nav

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.frontend_happygreen.ui.screens.PostDetailScreen
import com.example.frontend_happygreen.ui.screens.*
import com.happygreen.viewmodels.*


/**
 * Componente di navigazione che gestisce il flusso delle schermate relative ai post
 */
@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun PostNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "group_detail/{groupId}",
    onBackToGroups: () -> Unit,
    groupViewModel: GroupViewModel,
    postViewModel: PostViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = "group_detail/{groupId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getInt("groupId") ?: 0

            GroupDetailScreen(
                groupId = groupId,
                groupViewModel = groupViewModel,
                postViewModel = postViewModel,
                onBackClick = onBackToGroups,
                onPostClick = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }

        composable(
            route = "post_detail/{postId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            val postViewModel: PostViewModel = viewModel()

            PostDetailScreen(
                postId = postId,
                postViewModel = postViewModel,
                onBackClick = { navController.popBackStack() },
                onCommentClick = { commentId ->
                    // Handle comment click if needed
                }
            )
        }
    }
}
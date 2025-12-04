package com.example.drawingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.screens.AnalysisGallery
import com.example.drawingapp.screens.AuthScreen
import com.example.drawingapp.screens.CommunityGallery
import com.example.drawingapp.screens.DrawingCanvas
import com.example.drawingapp.screens.Gallery
import com.example.drawingapp.screens.ImageAnalysis
import com.example.drawingapp.screens.MainGallery
import com.example.drawingapp.screens.Settings
import com.example.drawingapp.screens.SplashScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: DrawingAppViewModel,
    startDestination: String="splashScreen"
) {
    NavHost(navController = navController, startDestination = startDestination)
    {
        composable("auth") {
            AuthScreen(navController = navController, viewModel)
        }
        composable("splashScreen") {
            SplashScreen(navController)
        }

        composable("mainMenu") {
            MainGallery(navController, viewModel)
        }

        composable("drawingCanvas/new") {
            // DrawingCanvas should treat null index as "new"
            DrawingCanvas(navController, viewModel, null)
        }

        composable(
            route = "drawingCanvas/{index}",
            arguments = listOf(
                androidx.navigation.navArgument("index") {
                    type = androidx.navigation.NavType.IntType
                    nullable = false
                }
            )
        ) { entry ->
            val idx = entry.arguments?.getInt("index")
            DrawingCanvas(navController, viewModel, idx)
        }

        composable("gallery") {
            Gallery(navController, viewModel)
        }

        composable("communityGallery") {
            CommunityGallery(navController, viewModel)
        }

        composable("analysisGallery") {
            AnalysisGallery(navController, viewModel)
        }

        composable(
            route = "analysisScreen/{index}",
            arguments = listOf(
                androidx.navigation.navArgument("index") {
                    type = androidx.navigation.NavType.IntType
                    nullable = false
                }
            )
        ) { entry ->
            val idx = requireNotNull(entry.arguments?.getInt("index"))
            ImageAnalysis(navController, viewModel, idx)
        }

        composable("settings") {
            Settings(navController)
        }

    }
}
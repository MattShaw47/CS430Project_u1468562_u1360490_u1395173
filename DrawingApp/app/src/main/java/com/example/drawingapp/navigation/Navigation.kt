package com.example.drawingapp.navigation

import android.window.SplashScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.screens.ComponentTestScreen
import com.example.drawingapp.screens.DrawingCanvas
import com.example.drawingapp.screens.Gallery
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
        composable("splashScreen") {
            SplashScreen(navController)
        }

        composable("mainMenu") {
            MainGallery(navController, viewModel)
        }

        composable("drawingCanvas/{imageId}") {
            val imgID = it.arguments?.getInt("imageID")
            DrawingCanvas(navController, viewModel, imgID)
        }

        composable("gallery") {
            Gallery(navController, viewModel)
        }

        composable("settings") {
            Settings(navController, viewModel)
        }

        composable("componentTest") {
            ComponentTestScreen(navController)
        }
    }
}
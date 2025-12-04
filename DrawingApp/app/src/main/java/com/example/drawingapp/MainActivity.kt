package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.navigation.AppNavHost
import com.example.drawingapp.ui.theme.DrawingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                val myNavController = rememberNavController()
                val drawingAppViewModel: DrawingAppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = DrawingAppViewModelFactory(LocalContext.current)
                )
                AppNavHost(myNavController, drawingAppViewModel, "splashScreen")
            }
        }
    }
}
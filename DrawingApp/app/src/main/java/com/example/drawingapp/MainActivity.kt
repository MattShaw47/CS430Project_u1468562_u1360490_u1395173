package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.navigation.AppNavHost
import com.example.drawingapp.ui.theme.DrawingAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines an image.
 */
data class Image(val points: List<Point> = emptyList()) {
    // TODO >> list must be square
}

/**
 * Defines a single point on an image.
 */
data class Point(
    val x: Int,
    val y: Int,
    val color: Color = Color.White,
    val brushType: BrushType = BrushType.CIRCLE
) {}

/**
 * Defines the brush type.
 * Note - May need to add more shapes.
 */
enum class BrushType() {
    LINE, CIRCLE, RECTANGLE
}


class DrawingAppViewModel : ViewModel() {
    // TODO > Add all necessary members to the viewmodel
    // list of images
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                val myNavController = rememberNavController()
                val drawingAppViewModel: DrawingAppViewModel = viewModel()
                AppNavHost(myNavController, drawingAppViewModel)
            }
        }
    }
}
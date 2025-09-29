package com.example.drawingapp.screens

import android.graphics.Canvas
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel

/**
 * Creates the Drawing Canvas.
 * @param navController - manages app navigation.
 * @param viewModel - our current VM state.
 * @param imageID - The index of the currently selected image to edit or -1 if this
 *                  is a new image created.
 */
@Composable
fun DrawingCanvas(
    navController: NavController,
    viewModel: DrawingAppViewModel,
    imageID: Int? = -1
) {
    // TODO > build the drawing canvas UI
    // explicit check on img ID to make sure that it has been passed correctly
    Text("in the drawing Canvas. Our image ID is ->> $imageID")
    Canvas(
        // TODO
    )

}

/**
 * Draws the currently selected Image's points on the canvas.
 */
@Composable
fun DrawPoints(viewModel: DrawingAppViewModel) {

}
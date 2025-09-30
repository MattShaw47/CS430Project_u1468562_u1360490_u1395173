package com.example.drawingapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.BrushType
import com.example.drawingapp.ui.components.ColorPickerDialog
import com.example.drawingapp.ui.components.ShapePickerDialog
import com.example.drawingapp.ui.components.SizeSliderDialog

@Composable
fun ComponentTestScreen(navController: NavController) {
    var showColorPicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }
    var showShapePicker by remember { mutableStateOf(false) }

    var selectedColor by remember { mutableStateOf(Color.Black) }
    var selectedSize by remember { mutableStateOf(10f) }
    var selectedShape by remember { mutableStateOf(BrushType.CIRCLE) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Test UI Components")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Selected Color: $selectedColor")
        Text("Selected Size: ${selectedSize.toInt()}")
        Text("Selected Shape: $selectedShape")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showColorPicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Color Picker")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showSizePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Size Slider")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { showShapePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Shape Picker")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Main Menu")
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = selectedColor,
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showSizePicker) {
        SizeSliderDialog(
            currentSize = selectedSize,
            onSizeSelected = { size ->
                selectedSize = size
                showSizePicker = false
            },
            onDismiss = { showSizePicker = false }
        )
    }

    if (showShapePicker) {
        ShapePickerDialog(
            currentShape = selectedShape,
            onShapeSelected = { shape ->
                selectedShape = shape
                showShapePicker = false
            },
            onDismiss = { showShapePicker = false }
        )
    }
}
package com.example.drawingapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Brush size slider dialog
 * @param currentSize - Current brush size (1-50)
 * @param onSizeSelected - Callback when size is confirmed
 * @param onDismiss - Callback when dialog is dismissed
 */
@Composable
fun SizeSliderDialog(
    currentSize: Float,
    onSizeSelected: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var brushSize by remember { mutableStateOf(currentSize) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Brush Size",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Visual preview of brush size
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 16.dp)
                ) {
                    drawCircle(
                        color = Color.Black,
                        radius = brushSize,
                        center = center
                    )
                }

                Text(
                    text = "Size: ${brushSize.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Slider for size selection
                Slider(
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm button
                Button(
                    onClick = { onSizeSelected(brushSize) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONFIRM")
                }
            }
        }
    }
}
package com.example.drawingapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.drawingapp.BrushType

/**
 * Shape selection dialog
 * @param currentShape - Currently selected shape
 * @param onShapeSelected - Callback when shape is selected
 * @param onDismiss - Callback when dialog is dismissed
 */
@Composable
fun ShapePickerDialog(
    currentShape: BrushType,
    onShapeSelected: (BrushType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedShape by remember { mutableStateOf(currentShape) }

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
                    text = "Select Shape",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Shape options in a single row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShapeOption(
                        shape = BrushType.RECTANGLE,
                        isSelected = selectedShape == BrushType.RECTANGLE,
                        onClick = { selectedShape = BrushType.RECTANGLE }
                    )
                    ShapeOption(
                        shape = BrushType.CIRCLE,
                        isSelected = selectedShape == BrushType.CIRCLE,
                        onClick = { selectedShape = BrushType.CIRCLE }
                    )
                    ShapeOption(
                        shape = BrushType.LINE,
                        isSelected = selectedShape == BrushType.LINE,
                        onClick = { selectedShape = BrushType.LINE }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = { onShapeSelected(selectedShape) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SELECT")
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeOption(
    shape: BrushType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (shape) {
                BrushType.RECTANGLE -> {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(size.width * 0.2f, size.height * 0.2f),
                        size = Size(size.width * 0.6f, size.height * 0.6f),
                        style = Stroke(width = 3f)
                    )
                }
                BrushType.CIRCLE -> {
                    drawCircle(
                        color = Color.Black,
                        radius = size.minDimension * 0.35f,
                        style = Stroke(width = 3f)
                    )
                }
                BrushType.LINE -> {
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width * 0.2f, size.height * 0.8f),
                        end = Offset(size.width * 0.8f, size.height * 0.2f),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}
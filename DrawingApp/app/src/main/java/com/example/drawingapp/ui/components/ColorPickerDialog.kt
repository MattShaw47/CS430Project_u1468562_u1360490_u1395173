package com.example.drawingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
 * Color picker dialog component
 * @param currentColor - The currently selected color
 * @param onColorSelected - Callback when user selects a color
 * @param onDismiss - Callback when user cancels
 */
@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(currentColor) }

    // Predefined color palette matching the design
    val colorPalette = listOf(
        // Row 1 - Grays
        Color.Black, Color(0xFF404040), Color(0xFF808080),
        Color(0xFFB0B0B0), Color(0xFFD3D3D3), Color(0xFFE8E8E8), Color.White,

        // Row 2 - Primary colors
        Color(0xFF8B0000), Color.Red, Color(0xFFFFA500),
        Color.Yellow, Color.Green, Color.Cyan, Color(0xFF4169E1),

        // Row 3 - Pastels
        Color.Magenta, Color(0xFFFFB6C1), Color(0xFF90EE90),
        Color(0xFFADD8E6), Color(0xFFDDA0DD), Color(0xFFB22222), Color(0xFFCD5C5C),

        // Row 4 - Earth tones
        Color(0xFF9ACD32), Color(0xFF5F9EA0), Color(0xFF6495ED),
        Color(0xFF4682B4), Color(0xFF663399), Color(0xFF8B4789), Color(0xFF654321),

        // Row 5 - Deep colors
        Color(0xFFD2691E), Color(0xFF556B2F), Color(0xFF000080),
        Color(0xFF4B0082), Color(0xFF800020), Color(0xFF808000), Color(0xFF8B4513)
    )

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
                    text = "Select Color",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Color grid (7 columns x 5 rows)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorPalette.chunked(7).forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { color ->
                                ColorBox(
                                    color = color,
                                    isSelected = color == selectedColor,
                                    onClick = { selectedColor = color }
                                )
                            }
                        }
                    }
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
                        onClick = { onColorSelected(selectedColor) },
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
private fun ColorBox(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
    )
}
package com.example.drawingapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.model.DrawingImage

@Composable
fun Gallery(navController: NavController, viewModel: DrawingAppViewModel) {
    val drawings by viewModel.drawings.collectAsState()
    val selected by viewModel.selected.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            if (drawings.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No saved images yet")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(drawings) { index, drawing ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            GalleryCell(
                                drawing,
                                index = index,
                                isSelected = index in selected,
                                onToggleSelect = { viewModel.toggleSelected(index) },
                                onEdit = {
                                    viewModel.editDrawing(index)
                                    navController.navigate("drawingCanvas/$index")
                                },
                                onDelete = { viewModel.deleteAt(index) },
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text("Back to Main Menu")
        }
    }
}

@Composable
private fun GalleryCell(
    drawing: DrawingImage,
    index: Int,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        tonalElevation = if (isSelected) 6.dp else 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray)
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEFEFEF))
                    .clickable { onToggleSelect() }
            ) {
                val strokes = drawing.strokeList()
                strokes.forEach { stroke ->
                    val strokeColor = Color(
                        red = android.graphics.Color.red(stroke.argb) / 255f,
                        green = android.graphics.Color.green(stroke.argb) / 255f,
                        blue = android.graphics.Color.blue(stroke.argb) / 255f,
                        alpha = android.graphics.Color.alpha(stroke.argb) / 255f
                    )

                    for (i in 0 until stroke.points.size - 1) {
                        val start = stroke.points[i]
                        val end = stroke.points[i + 1]

                        // TODO >> should not be a constant scaling factor
                        drawLine(
                            color = strokeColor,
                            start = Offset(start.x / 3, start.y / 3),
                            end = Offset(end.x / 3, end.y / 3),
                            strokeWidth = stroke.width / 3,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            if (isSelected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                )
                Text(
                    "Selected",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.White.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }


            // --------------  old code  -------------------

//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .background(Color(0xFFEFEFEF))
//                    .clickable { onToggleSelect() }
//            )
//            Text(
//                text = "Image #$index",
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(8.dp),
//                fontWeight = FontWeight.SemiBold
//            )
//            if (isSelected) {
//                Box(
//                    Modifier
//                        .fillMaxSize()
//                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
//                )
//                Text(
//                    "Selected",
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .background(Color.White.copy(alpha = 0.7f))
//                        .padding(horizontal = 8.dp, vertical = 4.dp),
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.Bold
//                )
//            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

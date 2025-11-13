package com.example.drawingapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel

@Composable
fun ImageAnalysis(
    navController: NavController,
    viewModel: DrawingAppViewModel,
    index: Int
) {
    val drawings by viewModel.drawings.collectAsState()
    val currAnalysis by viewModel.currentAnalysis.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    val errorMsg by viewModel.analysisError.collectAsState()

    val drawingBmp = drawings[index].getBitmap()

    // analyze when screen opens
    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.analyzeDrawing(drawingBmp)
    }

    // stop loading on error
    LaunchedEffect(errorMsg) {
        if (errorMsg != null) isLoading = false
    }

    // stop loading when we get results
    LaunchedEffect(currAnalysis) {
        if (currAnalysis != null) {
            isLoading = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // canvas with image and boxes
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .border(2.dp, Color.Black)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // draw the image
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    val destRect = android.graphics.Rect(
                        0, 0, size.width.toInt(), size.height.toInt()
                    )
                    nativeCanvas.drawBitmap(drawingBmp, null, destRect, null)
                }

                // draw bounding boxes with different colors
                val detections = currAnalysis?.responses?.firstOrNull()
                    ?.localizedObjectAnnotations

                // use different colors for each detection
                val colors = listOf(
                    Color.Red,
                    Color.Blue,
                    Color.Green,
                    Color.Yellow,
                    Color.Cyan
                )

                detections?.forEachIndexed { idx, obj ->
                    val vertices = obj.boundingPoly?.normalizedVertices
                    if (vertices != null && vertices.size >= 4) {
                        val path = Path()

                        for (i in vertices.indices) {
                            val x = (vertices[i].x ?: 0f) * size.width
                            val y = (vertices[i].y ?: 0f) * size.height

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        path.close()

                        // use different color for each box
                        val boxColor = colors[idx % colors.size]

                        // calculate stroke width based on confidence
                        val confidence = obj.score ?: 0.5f
                        val strokeWidth = 4f + (confidence * 8f)

                        // draw outer glow for high confidence boxes (makes them look bigger)
                        if (confidence > 0.8f) {
                            drawPath(
                                path = path,
                                color = boxColor.copy(alpha = 0.3f),
                                style = Stroke(width = strokeWidth + 8f)
                            )
                        }

                        drawPath(
                            path = path,
                            color = boxColor,
                            style = Stroke(width = 4f)
                        )
                    }
                }
            }

            // loading indicator overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Analyzing image...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // results table
        Column(modifier = Modifier.weight(1f)) {
            // header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(12.dp)
            ) {
                Text("Name", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Conf.", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Box Color", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(thickness = 2.dp)

            val results = currAnalysis?.responses?.firstOrNull()
                ?.localizedObjectAnnotations

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (results == null || results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No objects detected", color = Color.Gray)
                }
            } else {
                val colors = listOf(
                    Color.Red to "Red",
                    Color.Blue to "Blue",
                    Color.Green to "Green",
                    Color.Yellow to "Yellow",
                    Color.Cyan to "Cyan"
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(results.size) { i ->
                        val obj = results[i]
                        val boxColor = colors[i % colors.size]

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(obj.name ?: "Unknown", modifier = Modifier.weight(1f))
                            Text(
                                "${((obj.score ?: 0f) * 100).toInt()}%",
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(boxColor.first)
                                        .border(1.dp, Color.Black)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    boxColor.second,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}
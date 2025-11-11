package com.example.drawingapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.data.VisionResponse

@Composable
fun ImageAnalysis(viewModel: DrawingAppViewModel, index: Int) {
    val drawings by viewModel.drawings.collectAsState()
    val currAnalysis by viewModel.currentAnalysis.collectAsState()

    val drawingBmp = drawings[index].getBitmap()
    viewModel.analyzeDrawing(drawingBmp)

    LaunchedEffect(currAnalysis) {
        println(currAnalysis.toString())
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(5.dp)
    ) {
        // canvas box
        Box(
            modifier = Modifier
                .fillMaxHeight(0.6f)
                .fillMaxWidth()
                .padding(15.dp)
                .border(2.dp, Color.Black)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                drawIntoCanvas { canvas ->
                    drawingBmp.let { bmp ->
                        val nativeCanvas = canvas.nativeCanvas
                        val destRect =
                            android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                        nativeCanvas.drawBitmap(bmp, null, destRect, null)
                    }
                }
            }
        }

        // vision response data box
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // name + confidence score
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp)
                        .border(1.dp, color = Color.Black)
                ) {

                }

                // labels + categories
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp)
                        .border(1.dp, color = Color.Black)
                ) {

                }
            }
        }

        // TODO >> set analysis to null when going back to home

    }

}
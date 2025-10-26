package com.example.drawingapp.screens

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.model.DrawingImage
import java.nio.file.Files.size

@Composable
fun MainGallery(
    navController: NavHostController,
    viewModel: DrawingAppViewModel
) {
    val drawings by viewModel.drawings.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            TiledBackdrop(
                drawings = drawings,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.08f),
                            1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate("drawingCanvas/new") },
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .heightIn(min = 56.dp)
                ) {
                    Text("New Canvas")
                }

                // separator of new button from others
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(
                    Modifier
                        .fillMaxWidth(0.72f)
                        .alpha(0.25f),
                    DividerDefaults.Thickness,
                    DividerDefaults.color
                )
                Spacer(Modifier.height(16.dp))

                // The rest of the buttons
                Button(
                    onClick = { navController.navigate("gallery") },
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .heightIn(min = 52.dp)
                ) { Text("Open Gallery") }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .heightIn(min = 52.dp)
                ) { Text("Settings") }

            }
        }
    }
}

/**
 * Background grid:
 * - If photos is empty: all neutral gray tiles.
 * - If not: first N tiles show the photos; remaining tiles are gray placeholders.
 */
@Composable
private fun TiledBackdrop(
    drawings: List<DrawingImage>,
    modifier: Modifier = Modifier,
    minTileSizeDp: Int = 120,
    totalTileCount: Int = 36
) {
    val slots: List<DrawingImage?> =
        List(totalTileCount) { idx -> drawings.getOrNull(idx) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minTileSizeDp.dp),
        modifier = modifier.alpha(1f),
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(slots) { maybeDrawing ->
            if (maybeDrawing == null) {
                PlaceholderTile()
            } else {
                PhotoTile(drawing = maybeDrawing)
            }
        }
    }
}

@Composable
private fun PlaceholderTile() {
    // tiling
    Box(
        Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFE6E7EB),
                        Color(0xFFDADBE0)
                    )
                )
            )
    )
}

@Composable
private fun PhotoTile(drawing: DrawingImage) {
    Canvas(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    ) {
        val tileW = size.width
        val tileH = size.height
        drawIntoCanvas { canvas ->
            drawing.getBitmap().let { bmp ->
                val nativeCanvas = canvas.nativeCanvas
                val destRect = android.graphics.Rect(0, 0, tileW.toInt(), tileH.toInt())
                nativeCanvas.drawBitmap(bmp, null, destRect, null)
            }
        }
    }
}

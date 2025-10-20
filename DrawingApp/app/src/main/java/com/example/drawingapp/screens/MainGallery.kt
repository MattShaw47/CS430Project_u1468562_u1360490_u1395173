package com.example.drawingapp.screens

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.drawingapp.DrawingAppViewModel

@Composable
fun MainGallery(
    navController: NavHostController,
    viewModel: DrawingAppViewModel
) {
    val backgroundPhotos by viewModel.backgroundPhotoUris.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            TiledBackdrop(
                photos = backgroundPhotos,
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

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { navController.navigate("componentTest") },
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .heightIn(min = 52.dp)
                ) { Text("Test Components") }
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
    photos: List<Uri>,
    modifier: Modifier = Modifier,
    minTileSizeDp: Int = 120,
    totalTileCount: Int = 36
) {
    // list containing first 36 uris, and nulls filling empty spaces if less than 36 uris
    val slots: List<Uri?> = List(totalTileCount) { idx ->
        photos.getOrNull(idx)
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minTileSizeDp.dp),
        modifier = modifier.alpha(1f), // keep vivid; veil handled above
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(slots) { maybeUri ->
            if (maybeUri == null) {
                PlaceholderTile()
            } else {
                PhotoTile(maybeUri)
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
private fun PhotoTile(uri: Uri) {
    Image(
        painter = rememberAsyncImagePainter(uri),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    )
}

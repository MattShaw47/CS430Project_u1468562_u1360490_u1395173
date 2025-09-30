package com.example.drawingapp.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    val backgroundPhotos: List<Uri> = viewModel.backgroundPhotoUris

    Surface(modifier = Modifier.fillMaxSize()) {
        Box {
            // for tiled picture background
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.18f)
            ) {
                items(backgroundPhotos) { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { navController.navigate("gallery") }, modifier = Modifier.fillMaxWidth(0.6f)) {
                    Text("Open Gallery")
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate("drawingCanvas/new") }, modifier = Modifier.fillMaxWidth(0.6f)) {
                    Text("New Canvas")
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate("settings") }, modifier = Modifier.fillMaxWidth(0.6f)) {
                    Text("Settings")
                }
            }
        }
    }
}
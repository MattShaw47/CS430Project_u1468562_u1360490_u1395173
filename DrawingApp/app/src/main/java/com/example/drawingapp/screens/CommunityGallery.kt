package com.example.drawingapp.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.model.DrawingImage

/**
 * Allows users to view all images that have been stored in the Cloud, Shared with others, and been
 * shared with the current user.
 */
@Composable
fun CommunityGallery(navController: NavController, viewModel: DrawingAppViewModel) {

    val options: List<String> = listOf<String>("saved", "shared", "received")
    val cloudSelection by viewModel.cloudSelection.collectAsState()
    val cloudImages = viewModel.displayedCloudDrawings.collectAsState().value
    val receivedSenders by viewModel.receivedSenders.collectAsState()

    androidx.compose.runtime.LaunchedEffect(cloudSelection) {
        viewModel.getCloudImages(cloudSelection)
    }

    Column(Modifier.fillMaxSize()) {
        DropDownOptions(
            cloudSelection,
            viewModel,
            options
        )

        Box(Modifier.weight(1f)) {
            if (cloudImages.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No images under this selection")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(cloudImages) { index, bmp ->
                        val senderEmail =
                            if (cloudSelection == "received") receivedSenders.getOrNull(index)
                            else null

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            BmpCell(
                                bmp,
                                senderEmail,
                                onImport = {
                                    val newImage = DrawingImage(1024)
                                    newImage.setBitmap(bitmap = bmp)
                                    viewModel.addDrawing(newImage)
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Back to Main Menu")
            }
        }
    }
}

/**
 * Displays the drop down menu for cloud selection group.
 */
@Composable
fun DropDownOptions(
    cloudSelection: String,
    viewModel: DrawingAppViewModel,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
            .fillMaxWidth()
            .padding(30.dp, 45.dp, 30.dp, 20.dp)
            .background(color = Color.LightGray),
        ) {

        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            TextButton(onClick = { expanded = true }) {
                Text(cloudSelection, fontSize = 15.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }

            TextButton(onClick = { viewModel.getCloudImages(cloudSelection)}) {
                Text("refresh", fontSize = 15.sp)
                Icon(Icons.Default.Refresh, contentDescription = "refresh current selection.")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        viewModel.setCloudSelection(options[index])
                        expanded = false
                        viewModel.getCloudImages(options[index])
                    }
                )
            }

        }

    }
}

/**
 * Represents an image cell.
 */
@Composable
private fun BmpCell(
    bitmap: Bitmap,
    senderEmail: String? = null,
    onImport: () -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Box(
            Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // draw image
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEFEFEF))
            ) {
                drawIntoCanvas { canvas ->

                    val nativeCanvas = canvas.nativeCanvas
                    val destRect = android.graphics.Rect(
                        0, 0, size.width.toInt(), size.height.toInt()
                    )
                    nativeCanvas.drawBitmap(bitmap, null, destRect, null)
                }
            }

            // sender email at the top for received images
            if (!senderEmail.isNullOrBlank()) {
                Text(
                    text = senderEmail,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalIconButton(onClick = onImport, modifier = Modifier.size(35.dp)) {
                    Icon(Icons.Filled.Download, contentDescription = "Import and edit")
                }
            }

        }
    }
}


private fun isTopRegionDark(bitmap: Bitmap): Boolean {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) return false

    val sampleHeight = minOf((height * 0.2f).toInt(), 40).coerceAtLeast(1)

    var sumLuma = 0.0
    var count = 0

    val stepX = maxOf(width / 40, 1)
    val stepY = maxOf(sampleHeight / 10, 1)

    for (y in 0 until sampleHeight step stepY) {
        for (x in 0 until width step stepX) {
            val pixel = bitmap.getPixel(x, y)
            val r = ((pixel shr 16) and 0xFF) / 255.0
            val g = ((pixel shr 8) and 0xFF) / 255.0
            val b = (pixel and 0xFF) / 255.0

            // basic luminance
            val luma = 0.299 * r + 0.587 * g + 0.114 * b
            sumLuma += luma
            count++
        }
    }

    val avg = if (count > 0) sumLuma / count else 1.0
    // treat "dark" as below midpoint so we use white text
    return avg < 0.3
}
package com.example.drawingapp.screens

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.model.DrawingImage
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.example.drawingapp.screens.GalleryCell

@Composable
fun Gallery(navController: NavController, viewModel: DrawingAppViewModel) {
    val drawings by viewModel.drawings.collectAsState()
    val selected by viewModel.selected.collectAsState()
    val ids by viewModel.ids.collectAsState()
    val sharedIds by viewModel.sharedIds.collectAsState()
    val context = LocalContext.current
    val cloudSelection by viewModel.cloudSelection.collectAsState()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var indexForDialog by remember { mutableIntStateOf(-1) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            bitmap?.let { bmp ->
                val newImage = DrawingImage(1024)
                newImage.setBitmap(bitmap)
                viewModel.addDrawing(newImage)
            }

            val newIndex = viewModel.drawings.value.lastIndex
            navController.navigate("analysisScreen/$newIndex")
        }
    }

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
                        val id = ids.getOrNull(index)
                        val isShared = id != null && sharedIds.contains(id)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            GalleryCell(
                                drawing = drawing,
                                index = index,
                                isSelected = index in selected,
                                isShared = isShared,
                                onToggleSelect = { viewModel.toggleSelected(index) },
                                onEdit = {
                                    viewModel.editDrawing(index)
                                    navController.navigate("drawingCanvas/$index")
                                },
                                onToggleCloudShare = {
                                    viewModel.toggleShare(context, index)
                                    viewModel.getCloudImages(cloudSelection)
                                },
                                onShareExternal = {
                                    showConfirmDialog = true
                                    indexForDialog = index
                                },
                                onDelete = { viewModel.deleteAt(index) }
                            )
                        }
                    }
                }
            }
        }

        // shows the confirm dialog for sharing across user email
        if (showConfirmDialog) {
            ConfirmDialog(
                onConfirm = {
                    viewModel.getCloudImages(cloudSelection)
                    showConfirmDialog = false
                    showEmailDialog = true
                },
                onSkip = {
                    shareImage(viewModel, drawings[indexForDialog], context, indexForDialog)
                    showConfirmDialog = false
                    indexForDialog = -1
                },
                onDismiss = {
                    showConfirmDialog = false
                    indexForDialog = -1
                }
            )
        }

        // displays email entry pane if confirmed alert pane
        if (showEmailDialog && indexForDialog > -1) {
            val isValidEmail by viewModel.isValidEmail.collectAsState()
            var email by remember { mutableStateOf("") }

            ShowEmailInputPane(
                email,
                onEmailChange = {
                    email = it
                    viewModel.validateEmail(email)
                },
                onConfirm = {
                    if (isValidEmail == true) {
                        viewModel.shareWithUser(
                            context,
                            drawings[indexForDialog],
                            indexForDialog,
                            email
                        )
                        showEmailDialog = false
                    }
                },
                onDismiss = {
                    showEmailDialog = false
                    viewModel.resetIsValidEmail()
                },
                isValidEmail
            )
        }

        // Bulk delete bar if multiple are selected
        if (selected.size >= 2) {
            val selectedIndices = remember(selected) {
                selected.toList().sortedDescending()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = {
                        selectedIndices.forEach { idx -> viewModel.deleteAt(idx) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete selected")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete selected")
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
        )
        {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Back to Main Menu")
            }
            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Import image")
                Text("Import Image")
            }
        }
    }
}

@Composable
fun GalleryCell(
    drawing: DrawingImage,
    index: Int,
    isSelected: Boolean,
    isShared: Boolean,
    onToggleSelect: () -> Unit,
    onEdit: () -> Unit,
    onToggleCloudShare: () -> Unit,
    onShareExternal: () -> Unit,
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

                drawIntoCanvas { canvas ->
                    drawing.getBitmap().let { bmp ->
                        val nativeCanvas = canvas.nativeCanvas
                        val destRect =
                            android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                        nativeCanvas.drawBitmap(bmp, null, destRect, null)
                    }
                }
            }

            if (isSelected) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                )
                AssistChip(
                    onClick = onToggleSelect,
                    label = { Text("Selected") },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
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
                // Cloud share / unshare
                FilledTonalIconButton(
                    onClick = onToggleCloudShare,
                    modifier = Modifier.size(35.dp)
                ) {
                    if (isShared) {
                        Icon(
                            Icons.Filled.CloudDone,
                            contentDescription = "Remove from cloud"
                        )
                    } else {
                        Icon(
                            Icons.Filled.CloudUpload,
                            contentDescription = "Share to cloud"
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))

                // External share
                FilledTonalIconButton(onClick = onShareExternal, modifier = Modifier.size(35.dp)) {
                    Icon(Icons.Filled.Share, contentDescription = "Share")
                }
                Spacer(Modifier.width(4.dp))

                FilledTonalIconButton(onClick = onEdit, modifier = Modifier.size(35.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                Spacer(Modifier.width(4.dp))

                FilledTonalIconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.size(35.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    onConfirm: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = { onDismiss },
        text = { Text("Would you like to send this image to another user?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("CONFIRM") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onSkip) { Text("SKIP") }
                TextButton(onClick = onDismiss) { Text("CANCEL") }
            }
        }
    )
}

@Composable
private fun ShowEmailInputPane(
    email: String,
    onEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isValidEmail: Boolean?
) {

    var inputEmail by remember { mutableStateOf(email) }

    AlertDialog(
        onDismissRequest = {},
        confirmButton = { TextButton(onClick = onConfirm) { Text("SUBMIT") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } },
        title = { Text("Enter User Email") },
        text = {
            Column {
                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { newValue ->
                        inputEmail = newValue
                        onEmailChange(newValue)
                    },
                    colors = if (isValidEmail == false) OutlinedTextFieldDefaults.colors(
                        errorLabelColor = Color.Red
                    ) else OutlinedTextFieldDefaults.colors(
                        errorLabelColor = Color.Green
                    )
                )

                if (isValidEmail == false) {
                    Row(modifier = Modifier.padding(5.dp)) {
                        Text(color = Color.Red, text = "Must enter a valid email...")
                    }
                }
            }
        }
    )
}

private fun shareImage(
    viewModel: DrawingAppViewModel,
    drawing: DrawingImage,
    context: Context,
    imageID: Int
) {
    val bitmap = drawing.getBitmap()
    val uri = viewModel.shareBitmap(bitmap)
    uri?.let {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, it)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Drawing"))
    }
}
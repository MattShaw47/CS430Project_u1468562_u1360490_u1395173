package com.example.drawingapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.drawingapp.BrushType
import com.example.drawingapp.DrawingAppViewModel
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.ui.components.ColorPickerDialog
import com.example.drawingapp.ui.components.ShapePickerDialog
import com.example.drawingapp.ui.components.SizeSliderDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.*

/**
 * Creates the Drawing Canvas.
 * @param navController - manages app navigation.
 * @param viewModel - our current VM state.
 * @param imageIndex - The index of the currently selected image to edit, or null for new image.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingCanvas(
    navController: NavController,
    viewModel: DrawingAppViewModel,
    imageIndex: Int? = null
) {
    // pen property state
    val currentColor by viewModel.selectedColor.collectAsState()
    val currentBrushSize by viewModel.selectedSize.collectAsState()
    val currentShape by viewModel.selectedBrushType.collectAsState()

    // Dialog visibility states
    var showColorPicker by remember { mutableStateOf(false) }
    var showSizePicker by remember { mutableStateOf(false) }
    var showShapePicker by remember { mutableStateOf(false) }
    val drawingImage by viewModel.activeDrawing.collectAsState()

    LaunchedEffect(imageIndex) {
        if (imageIndex != null) {
            viewModel.editDrawing(imageIndex)
        } else {
            viewModel.startNewDrawing()
        }
    }

    // Current stroke being drawn
    val currentStroke = remember { mutableStateListOf<Point>() }
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragCurrent by remember { mutableStateOf<Offset?>(null) }

    fun buildRectanglePoints(a: Offset, b: Offset): List<Point> {
        val left = min(a.x, b.x)
        val right = max(a.x, b.x)
        val top = min(a.y, b.y)
        val bottom = max(a.y, b.y)
        return listOf(
            Point(left, top), Point(right, top),
            Point(right, top), Point(right, bottom),
            Point(right, bottom), Point(left, bottom),
            Point(left, bottom), Point(left, top)
        )
    }

    // Ellipse (circle if bounds are square) approximated by N segments
    fun buildEllipsePoints(a: Offset, b: Offset, segments: Int = 48): List<Point> {
        val cx = (a.x + b.x) / 2f
        val cy = (a.y + b.y) / 2f
        val rx = abs(b.x - a.x) / 2f
        val ry = abs(b.y - a.y) / 2f
        val n = max(12, segments)
        val pts = ArrayList<Point>(n + 1)
        for (k in 0..n) {
            val t = (2.0 * Math.PI * k / n).toFloat()
            val x = cx + rx * cos(t)
            val y = cy + ry * sin(t)
            pts.add(Point(x, y))
        }
        return pts
    }

    // Utility: current Color -> ARGB int
    fun colorToArgbInt(c: Color): Int =
        android.graphics.Color.argb(
            (c.alpha * 255).toInt(),
            (c.red * 255).toInt(),
            (c.green * 255).toInt(),
            (c.blue * 255).toInt()
        )


    // Force recomposition when strokes change
    var redrawTrigger by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (imageIndex != null) "Edit Drawing #$imageIndex" else "New Drawing")
                },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.startNewDrawing()
                        viewModel.resetPenProperties()
                        navController.popBackStack()
                    }) {
                        Text("Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            drawingImage.save()
                            if (imageIndex == null) {
                                viewModel.insertActive()
                            } else {
                                viewModel.updateActiveAt(imageIndex)
                            }
                            viewModel.resetPenProperties()
                            navController.popBackStack()
                        }
                    ) {
                        Text("Save")
                    }
                }
            )
        },
        bottomBar = {
            DrawingToolbar(
                currentColor = currentColor,
                currentSize = currentBrushSize,
                currentShape = currentShape,
                onColorClick = { showColorPicker = true },
                onSizeClick = { showSizePicker = true },
                onShapeClick = { showShapePicker = true },
                onUndo = {
                    if (drawingImage.undo()) {
                        redrawTrigger++
                    }
                },
                onRedo = {
                    if (drawingImage.redo()) {
                        redrawTrigger++
                    }
                },
                onClear = {
                    drawingImage.clear()
                    redrawTrigger++
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main drawing canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .pointerInput(currentColor, currentBrushSize, currentShape) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStroke.clear()
                                when (currentShape) {
                                    BrushType.FREEHAND -> {
                                        currentStroke.add(Point(offset.x, offset.y))
                                    }

                                    BrushType.LINE -> {
                                        dragStart = offset
                                        dragCurrent = offset
                                        currentStroke.add(Point(offset.x, offset.y))
                                        currentStroke.add(Point(offset.x, offset.y))
                                    }

                                    BrushType.RECTANGLE, BrushType.CIRCLE -> {
                                        dragStart = offset
                                        dragCurrent = offset
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                when (currentShape) {
                                    BrushType.FREEHAND -> {
                                        val p = change.position
                                        currentStroke.add(Point(p.x, p.y))
                                    }

                                    BrushType.LINE -> {
                                        dragCurrent = change.position
                                        val s = dragStart!!;
                                        val e = dragCurrent!!
                                        currentStroke.clear()
                                        currentStroke.add(Point(s.x, s.y))
                                        currentStroke.add(Point(e.x, e.y))
                                    }

                                    BrushType.RECTANGLE -> {
                                        dragCurrent = change.position
                                        currentStroke.clear()
                                        buildRectanglePoints(
                                            dragStart!!,
                                            dragCurrent!!
                                        ).forEach { currentStroke.add(it) }
                                    }

                                    BrushType.CIRCLE -> {
                                        dragCurrent = change.position
                                        currentStroke.clear()
                                        buildEllipsePoints(
                                            dragStart!!,
                                            dragCurrent!!,
                                            48
                                        ).forEach { currentStroke.add(it) }
                                    }
                                }
                            },
                            onDragEnd = {
                                val pts = currentStroke.toList()
                                if (pts.size >= 2) {
                                    drawingImage.addStroke(
                                        Stroke(
                                            points = pts,
                                            width = currentBrushSize,
                                            argb = colorToArgbInt(currentColor)
                                        )
                                    )
                                }
                                currentStroke.clear()
                                dragStart = null
                                dragCurrent = null
                                redrawTrigger++
                            }
                        )
                    }
            ) {
                // draw the bitmap if existing
                if (drawingImage.importedBitmap != null) {
                    drawIntoCanvas { canvas ->
                        drawingImage.importedBitmap?.let { bmp ->
                            val nativeCanvas = canvas.nativeCanvas
                            val destRect = android.graphics.Rect(0, 0, size.width.toInt(), size.height.toInt())
                            nativeCanvas.drawBitmap(bmp, null, destRect, null)
                        }
                    }
                }

                // Draw all saved strokes
                val __rt = redrawTrigger
                val strokes = drawingImage.strokeList()

                strokes.forEach { stroke ->
                    // Convert ARGB Int back to Color
                    val strokeColor = Color(
                        red = android.graphics.Color.red(stroke.argb) / 255f,
                        green = android.graphics.Color.green(stroke.argb) / 255f,
                        blue = android.graphics.Color.blue(stroke.argb) / 255f,
                        alpha = android.graphics.Color.alpha(stroke.argb) / 255f
                    )

                    for (i in 0 until stroke.points.size - 1) {
                        val start = stroke.points[i]
                        val end = stroke.points[i + 1]

                        drawLine(
                            color = strokeColor,
                            start = Offset(start.x, start.y),
                            end = Offset(end.x, end.y),
                            strokeWidth = stroke.width,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw current stroke being drawn
                if (currentStroke.size >= 2) {
                    for (i in 0 until currentStroke.size - 1) {
                        val start = currentStroke[i]
                        val end = currentStroke[i + 1]

                        drawLine(
                            color = currentColor,
                            start = Offset(start.x, start.y),
                            end = Offset(end.x, end.y),
                            strokeWidth = currentBrushSize,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }

    // Color picker dialog
    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = currentColor,
            onColorSelected = { color ->
                viewModel.setColor(color)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    // Size slider dialog
    if (showSizePicker) {
        SizeSliderDialog(
            currentSize = currentBrushSize,
            currentColor,
            onSizeSelected = { size ->
                viewModel.setSize(size)
                showSizePicker = false
            },
            onDismiss = { showSizePicker = false }
        )
    }

    // Shape picker dialog
    if (showShapePicker) {
        ShapePickerDialog(
            currentShape = currentShape,
            onShapeSelected = { shape ->
                viewModel.setShape(shape)
                showShapePicker = false
            },
            onDismiss = { showShapePicker = false }
        )
    }
}

/**
 * Bottom toolbar with drawing tools
 */
@Composable
private fun DrawingToolbar(
    currentColor: Color,
    currentSize: Float,
    currentShape: BrushType,
    onColorClick: () -> Unit,
    onSizeClick: () -> Unit,
    onShapeClick: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top row: Undo, Redo, Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = onUndo) {
                    Text("Undo")
                }
                OutlinedButton(onClick = onRedo) {
                    Text("Redo")
                }
                OutlinedButton(onClick = onClear) {
                    Text("Clear")
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Bottom row: Color, Size, Shape
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(currentColor, MaterialTheme.shapes.medium)
                    )
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = onColorClick) {
                        Text("Color")
                    }
                }

                // Size button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${currentSize.toInt()}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    TextButton(onClick = onSizeClick) {
                        Text("Size")
                    }
                }

                // Shape button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (currentShape) {
                            BrushType.CIRCLE -> "○"
                            BrushType.RECTANGLE -> "▢"
                            BrushType.LINE -> "/"
                            BrushType.FREEHAND -> "⌇"
                        },
                        style = MaterialTheme.typography.headlineSmall
                    )
                    TextButton(onClick = onShapeClick) {
                        Text("Shape")
                    }
                }
            }
        }
    }
}
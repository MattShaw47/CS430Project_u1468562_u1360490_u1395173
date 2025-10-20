package com.example.drawingapp

import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.screens.DrawingCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class BrushType() {
    LINE, CIRCLE, RECTANGLE, FREEHAND
}

class DrawingAppViewModel : ViewModel() {
    private val _selected = MutableStateFlow<Set<Int>>(emptySet())
    val selected: StateFlow<Set<Int>> = _selected

    // gallery storage
    private val _drawings = MutableStateFlow<List<DrawingImage>>(emptyList())
    val drawings: StateFlow<List<DrawingImage>> = _drawings

    // Currently selected drawing
    private val _activeDrawing = MutableStateFlow<DrawingImage>(DrawingImage(1024))
    val activeDrawing: StateFlow<DrawingImage> = _activeDrawing

    // For main menu background
    private val _backgroundPhotoUris = MutableStateFlow<List<Uri>>(emptyList())
    val backgroundPhotoUris: StateFlow<List<Uri>> = _backgroundPhotoUris

    private val _selectedColor = MutableStateFlow<Color>(Color.Black)
    val selectedColor: StateFlow<Color> = _selectedColor

    private val _selectedSize = MutableStateFlow<Float>(10F)
    val selectedSize: StateFlow<Float> = _selectedSize

    private val _selectedBrushType = MutableStateFlow<BrushType>(BrushType.FREEHAND)
    val selectedBrushType: StateFlow<BrushType> = _selectedBrushType

    // VM state updating
    fun addDrawing(img: DrawingImage) {
        _drawings.value = _drawings.value + img
    }

    fun toggleSelected(index: Int) {
        _selected.value = _selected.value.toMutableSet().also {
            if (!it.add(index)) it.remove(index)
        }
    }

    fun selectDrawing(imgID: Int) {
        if (imgID in drawings.value.indices) {
            _activeDrawing.value = _drawings.value[imgID]
        }
    }

    fun startNewDrawing() {
        _activeDrawing.value = DrawingImage(1024)
    }

    fun editDrawing(imgID: Int) {
        if (imgID in _drawings.value.indices) {
            _activeDrawing.value = _drawings.value[imgID].cloneDeep()
        }
    }

    fun removeAt(index: Int) {
        val current = _drawings.value
        if (index !in current.indices) return

        _drawings.value = current.toMutableList().also { it.removeAt(index) }
        resetActiveDrawing(true)

        // shift/clean selection indices
        val newSel = buildSet {
            for (i in _selected.value) {
                when {
                    i < index -> add(i)
                    i > index -> add(i - 1)
                }
            }
        }
        _selected.value = newSel
    }

    fun saveActiveDrawing(imgID: Int?) {
        if (imgID != null && imgID in drawings.value.indices) {
            val currentDrawings = drawings.value.toMutableList()
            currentDrawings[imgID] = activeDrawing.value.cloneDeep()
            _drawings.value = currentDrawings
        }

        resetActiveDrawing(true)
    }

    fun resetActiveDrawing(toCleanState: Boolean, imgID: Int? = null) {
        if (toCleanState) {
            _activeDrawing.value = DrawingImage(1024)
        } else {
            if (imgID != null && imgID in drawings.value.indices) {
                // clonedeep to avoid pass by ref
                _activeDrawing.value = _drawings.value[imgID].cloneDeep()
            } else {
                _activeDrawing.value = DrawingImage(1024)
            }
        }
    }

    fun setActiveDrawing(img: DrawingImage) {
        _activeDrawing.value = img
    }

    fun setBackgroundPhotos(uris: List<Uri>) {
        _backgroundPhotoUris.value = uris
    }

    // brush properties
    fun setColor(newColor: Color) {
        _selectedColor.value = newColor
    }

    fun setSize(size: Float) {
        _selectedSize.value = size;
    }

    fun setShape(shape: BrushType) {
        _selectedBrushType.value = shape;
    }

    fun resetPenProperties() {
        setColor(Color.Black)
        setSize(10F)
        setShape(BrushType.FREEHAND)
    }
}
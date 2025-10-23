package com.example.drawingapp

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.copy
import com.example.drawingapp.data.DrawingDataSource
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.screens.DrawingCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BrushType() {
    LINE, CIRCLE, RECTANGLE, FREEHAND
}

class DrawingAppViewModel(
    private val repository: DrawingDataSource
) : ViewModel() {

    // Public gallery list
    val drawings: StateFlow<List<DrawingImage>>

    // Parallel list of ids that aligns with drawings by index
    private val _ids = MutableStateFlow<List<Long>>(emptyList())
    val ids: StateFlow<List<Long>> = _ids
    private val _selected = MutableStateFlow<Set<Int>>(emptySet())
    val selected: StateFlow<Set<Int>> = _selected

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

    init {
        // Collect repository stream with IDs and split it into two flows
        drawings = repository.allDrawingsWithIds
            .map { pairs ->
                _ids.value = pairs.map { it.first }
                pairs.map { it.second }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

    // VM state updating
    fun startNewDrawing() {
        _activeDrawing.value = DrawingImage(1024)
    }

    fun addDrawing(newDrawing: DrawingImage) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDrawing(newDrawing)
        }
    }

    fun editDrawing(index: Int) {
        val current = drawings.value
        if (index in current.indices) {
            _activeDrawing.value = current[index].cloneDeep()
        }
    }

    fun insertActive() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertDrawing(_activeDrawing.value.cloneDeep())
            _activeDrawing.value = DrawingImage(1024)
        }
    }

    fun updateActiveAt(index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return
        val id = idList[index]
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDrawing(id, _activeDrawing.value.cloneDeep())
            _activeDrawing.value = DrawingImage(1024)
        }
    }

    fun deleteAt(index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDrawing(idList[index])
            // shift selection indices locally
            val oldSel = _selected.value
            _selected.value = oldSel.mapNotNull {
                when {
                    it == index -> null
                    it > index -> it - 1
                    else -> it
                }
            }.toSet()
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) { repository.deleteAllDrawings() }
    }

    // share helper
    fun shareBitmap(bitmap: Bitmap) = repository.shareDrawing(bitmap)

    // selection & brush props
    fun toggleSelected(index: Int) {
        _selected.value = _selected.value.toMutableSet().also { if (!it.add(index)) it.remove(index) }
    }

    fun setColor(newColor: Color) { _selectedColor.value = newColor }
    fun setSize(size: Float) { _selectedSize.value = size }
    fun setShape(shape: BrushType) { _selectedBrushType.value = shape }

    fun resetPenProperties() {
        setColor(Color.Black); setSize(10F); setShape(BrushType.FREEHAND)
    }
}
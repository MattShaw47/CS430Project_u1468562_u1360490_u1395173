package com.example.drawingapp

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.util.copy
import com.example.drawingapp.data.CloudDrawingRepository
import com.example.drawingapp.data.DrawingDataSource
import com.example.drawingapp.data.DrawingRepository
import com.example.drawingapp.data.VisionResponse
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.screens.DrawingCanvas
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.widget.Toast
import android.content.Context
import kotlinx.coroutines.withContext

enum class BrushType() {
    LINE, CIRCLE, RECTANGLE, FREEHAND
}

class DrawingAppViewModel(
    private val repository: DrawingDataSource,
    private val bg: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {


    private val cloudRepo = CloudDrawingRepository()

    // Set of local IDs that currently have a cloud copy
    private val _sharedIds = MutableStateFlow<Set<Long>>(emptySet())
    val sharedIds: StateFlow<Set<Long>> = _sharedIds

    // Public gallery list
    val drawings: StateFlow<List<DrawingImage>>

    // holds the most recent google vision image analysis
    private val _currentAnalysis = MutableStateFlow<VisionResponse?>(null)
    val currentAnalysis: StateFlow<VisionResponse?> = _currentAnalysis

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

    private val _analysisError = MutableStateFlow<String?>(null)
    val analysisError: StateFlow<String?> = _analysisError

    init {
        drawings = repository.allDrawingsWithIds
            .map { pairs ->
                _ids.value = pairs.map { it.first }
                pairs.map { it.second }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }

    fun refreshSharedIdsIfSignedIn() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch(bg) {
            try {
                _sharedIds.value = cloudRepo.getSharedIds()
            } catch (e: Exception) {
                // TODO catch error
            }
        }
    }

    fun toggleShare(context: Context, index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return

        val id = idList[index]
        val drawing = drawings.value.getOrNull(index) ?: return

        // Make sure user is logged in
        if (Firebase.auth.currentUser == null) return

        viewModelScope.launch(bg) {
            try {
                if (id in _sharedIds.value) {
                    cloudRepo.unshareDrawing(id)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Drawing removed from cloud", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    cloudRepo.uploadDrawing(id, drawing)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Upload successful!", Toast.LENGTH_SHORT).show()
                    }
                }
                _sharedIds.value = cloudRepo.getSharedIds()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun analyzeDrawing(drawing: Bitmap) {
        viewModelScope.launch {
            _analysisError.value = null
            _currentAnalysis.value = null
            try {
                val resp = repository.sendVisionRequest(drawing)
                _currentAnalysis.value = resp
            } catch (e: Exception) {
                _currentAnalysis.value = null
                _analysisError.value = e.message ?: "Vision request failed"
            }
        }
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

    fun insertActive() = viewModelScope.launch(bg) {
        repository.insertDrawing(_activeDrawing.value.cloneDeep())
        _activeDrawing.value = DrawingImage(1024)
    }

    fun updateActiveAt(index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return
        val id = idList[index]
        viewModelScope.launch(bg) {
            repository.updateDrawing(id, _activeDrawing.value.cloneDeep())
            _activeDrawing.value = DrawingImage(1024)
        }
    }

    fun deleteAt(index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return

        val id = idList[index]

        // shift selection synchronously first
        _selected.value = _selected.value.mapNotNull {
            when {
                it == index -> null
                it > index -> it - 1
                else -> it
            }
        }.toSet()

        viewModelScope.launch(bg) {
            try {
                // Attempt to remove cloud copy if user is signed in
                cloudRepo.unshareDrawing(id)
                _sharedIds.value = _sharedIds.value - id
            } catch (e: Exception) {

            }

            // delete local
            repository.deleteDrawing(id)
        }
    }

    fun clearAll() = viewModelScope.launch(bg) {
        repository.deleteAllDrawings()
    }

    // share helper
    fun shareBitmap(bitmap: Bitmap) = repository.shareDrawing(bitmap)

    // selection & brush props
    fun toggleSelected(index: Int) {
        _selected.value =
            _selected.value.toMutableSet().also { if (!it.add(index)) it.remove(index) }
    }

    fun setColor(newColor: Color) {
        _selectedColor.value = newColor
    }

    fun setSize(size: Float) {
        _selectedSize.value = size
    }

    fun setShape(shape: BrushType) {
        _selectedBrushType.value = shape
    }

    fun resetPenProperties() {
        setColor(Color.Black); setSize(10F); setShape(BrushType.FREEHAND)
    }
}
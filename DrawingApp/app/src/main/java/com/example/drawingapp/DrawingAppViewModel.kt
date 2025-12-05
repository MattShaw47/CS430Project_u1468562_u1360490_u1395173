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
import com.google.firebase.firestore.firestore
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

    //holds a list of bitmap objects from cloud storage based on selection (saved, shared, received)
    private val _displayedCloudDrawings = MutableStateFlow<List<Bitmap>>(emptyList())
    val displayedCloudDrawings: StateFlow<List<Bitmap>> = _displayedCloudDrawings

    // parallel list of sender emails for "received"
    private val _receivedSenders = MutableStateFlow<List<String>>(emptyList())
    val receivedSenders: StateFlow<List<String>> = _receivedSenders

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

    /**
     * Holds the current cloud selection type for displayed Community Gallery images
     */
    private val _cloudSelection = MutableStateFlow<String>("saved")
    val cloudSelection: StateFlow<String> = _cloudSelection

    /**
     * stores and watches the current state flow when validating shared emails.
     */
    private val _isValidEmail = MutableStateFlow<Boolean?>(null)
    val isValidEmail: StateFlow<Boolean?> = _isValidEmail

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

        // initialize the default cloud viewing list
        getCloudImages("saved")
    }

    /**
     * Returns the cloud images that fall under the selection type for current user.
     *  - param strictly must be 'saved', 'received', or 'sent'.
     */
    fun getCloudImages(selectionType: String) {
        viewModelScope.launch(bg) {
            if (selectionType == "received") {
                val (bitmaps, senders) = cloudRepo.getReceivedImagesWithSenders()
                _displayedCloudDrawings.value = bitmaps
                _receivedSenders.value = senders
            } else {
                _receivedSenders.value = emptyList()
                _displayedCloudDrawings.value = cloudRepo.getImagesFromStorage(selectionType)
            }
        }
    }

    /**
     * Resets current selection type and refreshes list with defaults.
     */
    fun resetCloudDrawingsAndSelection() {
        this._cloudSelection.value = "saved"
        _receivedSenders.value = emptyList()
        getCloudImages("saved")
    }

    /**
     * Refresh and get all the up to date shared image IDs.
     */
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

    /**
     * Save selected image to the Firebase storage under the current user's images/uid/... path.
     *      - If the image already exists in the DB, remove it.
     */
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
                        Toast.makeText(context, "Drawing removed from cloud", Toast.LENGTH_SHORT)
                            .show()
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

    /**
     * Shares a drawing with the existing user with passed in email address.
     */
    fun shareWithUser(context: Context, drawing: DrawingImage, index: Int, email: String) {
        if (Firebase.auth.currentUser == null) return

        viewModelScope.launch(bg) {
            if (index !in ids.value.indices) return@launch
            val id = ids.value[index]

            cloudRepo.shareDrawingWithUser(id, drawing, email)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Successfully shared image with $email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Asynchronous function that checks whether tha passed in email is associated with
     * a currently authenticated user.
     */
    fun validateEmail(email: String) {
        viewModelScope.launch(bg) {
             _isValidEmail.value = cloudRepo.checkForValidEmail(email)
        }
    }

    /**
     * Analyzes a selected drawing for object definitions via Google Vision request.
     */
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

    /**
     * Inserts the active image into the DB.
     */
    fun insertActive() = viewModelScope.launch(bg) {
        repository.insertDrawing(_activeDrawing.value.cloneDeep())
        _activeDrawing.value = DrawingImage(1024)
    }

    /**
     * Updates the currently active image.
     */
    fun updateActiveAt(index: Int) {
        val idList = ids.value
        if (index !in idList.indices) return
        val id = idList[index]
        viewModelScope.launch(bg) {
            repository.updateDrawing(id, _activeDrawing.value.cloneDeep())
            _activeDrawing.value = DrawingImage(1024)
        }
    }

    /**
     * Removes the image from the user's local DB.
     *      - If image has been saved to cloud --> remove from Firebase storage.
     */
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

    /**
     * Sets the color of thr brush.
     */
    fun setColor(newColor: Color) {
        _selectedColor.value = newColor
    }

    /**
     * Sets the size of the brush.
     */
    fun setSize(size: Float) {
        _selectedSize.value = size
    }

    /**
     * Sets the shape of the current brush type.
     */
    fun setShape(shape: BrushType) {
        _selectedBrushType.value = shape
    }

    /**
     * Reset validEmail to clean state.
     */
    fun resetIsValidEmail() {
        this._isValidEmail.value = null
    }

    /**
     * Reset pen properties to default state.
     */
    fun resetPenProperties() {
        setColor(Color.Black); setSize(10F); setShape(BrushType.FREEHAND)
    }

    /**
     * Sets the new cloud selection for displayed images.
     */
    fun setCloudSelection(sel: String) {
        this._cloudSelection.value = sel
    }
}
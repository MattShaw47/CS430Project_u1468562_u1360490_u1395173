package com.example.drawingapp

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.drawingapp.model.DrawingImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrawingAppViewModel : ViewModel() {
    // gallery storage
    private val _drawings = MutableStateFlow<List<DrawingImage>>(emptyList())
    val drawings: StateFlow<List<DrawingImage>> = _drawings

    // Currently selected drawing
    private val _activeDrawing = MutableStateFlow<DrawingImage?>(null)
    val activeDrawing: StateFlow<DrawingImage?> = _activeDrawing

    // For main menu background
    private val _backgroundPhotoUris = MutableStateFlow<List<Uri>>(emptyList())
    val backgroundPhotoUris: List<Uri> get() = _backgroundPhotoUris.value

    fun setActiveDrawing(img: DrawingImage) {
        _activeDrawing.value = img
    }

    fun addDrawing(img: DrawingImage) {
        _drawings.value = _drawings.value + img
    }

    fun setBackgroundPhotos(uris: List<Uri>) {
        _backgroundPhotoUris.value = uris
    }
}
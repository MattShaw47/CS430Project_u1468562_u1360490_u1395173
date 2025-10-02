package com.example.drawingapp

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.drawingapp.model.DrawingImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DrawingAppViewModel : ViewModel() {

    private val _selected = MutableStateFlow<Set<Int>>(emptySet())
    val selected: StateFlow<Set<Int>> = _selected

    // gallery storage
    private val _drawings = MutableStateFlow<List<DrawingImage>>(emptyList())
    val drawings: StateFlow<List<DrawingImage>> = _drawings

    // Currently selected drawing
    private val _activeDrawing = MutableStateFlow<DrawingImage?>(null)
    val activeDrawing: StateFlow<DrawingImage?> = _activeDrawing

    // For main menu background
    private val _backgroundPhotoUris = MutableStateFlow<List<Uri>>(emptyList())
    val backgroundPhotoUris: StateFlow<List<Uri>> = _backgroundPhotoUris

    fun setActiveDrawing(img: DrawingImage) {
        _activeDrawing.value = img
    }

    fun addDrawing(img: DrawingImage) {
        _drawings.value = _drawings.value + img
    }

    fun setBackgroundPhotos(uris: List<Uri>) {
        _backgroundPhotoUris.value = uris
    }

    fun toggleSelected(index: Int) {
        _selected.value = _selected.value.toMutableSet().also {
            if (!it.add(index)) it.remove(index)
        }
    }

    fun removeAt(index: Int) {
        val current = _drawings.value
        if (index !in current.indices) return

        _drawings.value = current.toMutableList().also { it.removeAt(index) }

        if (_activeDrawing.value == current[index]) _activeDrawing.value = null

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

    fun selectDrawing(index: Int) {
        _activeDrawing.value = _drawings.value.getOrNull(index)
    }

}
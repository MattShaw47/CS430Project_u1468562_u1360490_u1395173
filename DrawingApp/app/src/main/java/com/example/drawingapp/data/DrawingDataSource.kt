package com.example.drawingapp.data

import android.graphics.Bitmap
import android.net.Uri
import com.example.drawingapp.model.DrawingImage
import kotlinx.coroutines.flow.Flow

interface DrawingDataSource {
    val allDrawingsWithIds: Flow<List<Pair<Long, DrawingImage>>>
    suspend fun insertDrawing(drawingImage: DrawingImage): Long
    suspend fun updateDrawing(id: Long, drawingImage: DrawingImage)
    suspend fun deleteDrawing(id: Long)
    suspend fun deleteAllDrawings()
    fun shareDrawing(bitmap: Bitmap): Uri?
}
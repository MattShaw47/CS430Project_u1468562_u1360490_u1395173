package com.example.drawingapp.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.drawingapp.model.DrawingImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

class DrawingRepository private constructor(context: Context) {
    private val drawingDao: DrawingDao = DrawingDatabase.getDatabase(context).drawingDao()
    private val appContext = context.applicationContext

    val allDrawings: Flow<List<DrawingImage>> = drawingDao.getAllDrawings().map { entities ->
        entities.map { entity -> entityToDrawingImage(entity) }
    }

    suspend fun insertDrawing(drawingImage: DrawingImage): Long {
        val entity = drawingImageToEntity(drawingImage)
        return drawingDao.insertDrawing(entity)
    }

    suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {
        val entity = drawingImageToEntity(drawingImage, id)
        drawingDao.updateDrawing(entity)
    }

    suspend fun deleteDrawing(id: Long) {
        val entity = drawingDao.getDrawingById(id)
        entity?.let { drawingDao.deleteDrawing(it) }
    }

    suspend fun deleteAllDrawings() {
        drawingDao.deleteAllDrawings()
    }

    suspend fun getDrawingById(id: Long): DrawingImage? {
        return drawingDao.getDrawingById(id)?.let { entityToDrawingImage(it) }
    }

    suspend fun getDrawingCount(): Int {
        return drawingDao.getDrawingCount()
    }

    // Share drawing as image
    fun shareDrawing(bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(appContext.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_drawing_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawingImageToEntity(drawingImage: DrawingImage, id: Long = 0): DrawingEntity {
        return DrawingEntity(
            id = id,
            strokes = drawingImage.strokeList(),
            size = drawingImage.size,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun entityToDrawingImage(entity: DrawingEntity): DrawingImage {
        val drawingImage = DrawingImage(entity.size)
        entity.strokes.forEach { stroke ->
            drawingImage.addStroke(stroke)
        }
        drawingImage.save()
        return drawingImage
    }

    companion object {
        @Volatile
        private var INSTANCE: DrawingRepository? = null

        fun getInstance(context: Context): DrawingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DrawingRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
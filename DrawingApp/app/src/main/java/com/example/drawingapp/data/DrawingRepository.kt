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

// Repository pattern - single source of truth for drawing data
class DrawingRepository private constructor(context: Context) : DrawingDataSource {
    private val drawingDao: DrawingDao = DrawingDatabase.getDatabase(context).drawingDao()
    private val appContext = context.applicationContext

    // Get all drawings from database
    val allDrawings: Flow<List<DrawingImage>> = drawingDao.getAllDrawings().map { entities ->
        entities.map { entity -> convertToDrawingImage(entity) }
    }

    override val allDrawingsWithIds: Flow<List<Pair<Long, DrawingImage>>> =
        drawingDao.getAllDrawings().map { entities ->
            entities.map { e -> e.id to convertToDrawingImage(e) }
        }

    override suspend fun insertDrawing(drawingImage: DrawingImage): Long {
        val entity = convertToEntity(drawingImage)
        return drawingDao.insertDrawing(entity)
    }

    override suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {
        val entity = convertToEntity(drawingImage, id)
        drawingDao.updateDrawing(entity)
    }

    override suspend fun deleteDrawing(id: Long) {
        val entity = drawingDao.getDrawingById(id)
        if (entity != null) {
            drawingDao.deleteDrawing(entity)
        }
    }

    override suspend fun deleteAllDrawings() {
        drawingDao.deleteAllDrawings()
    }

    suspend fun getDrawingById(id: Long): DrawingImage? {
        val entity = drawingDao.getDrawingById(id)
        return if (entity != null) convertToDrawingImage(entity) else null
    }

    suspend fun getDrawingCount(): Int {
        return drawingDao.getDrawingCount()
    }

    override fun shareDrawing(bitmap: Bitmap): Uri? {
        try {
            val cachePath = File(appContext.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_drawing_${System.currentTimeMillis()}.png")

            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()

            return FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Helper methods to convert between entity and model
    private fun convertToEntity(drawingImage: DrawingImage, id: Long = 0): DrawingEntity {
        return DrawingEntity(
            id = id,
            strokes = drawingImage.strokeList(),
            size = drawingImage.size,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun convertToDrawingImage(entity: DrawingEntity): DrawingImage {
        val img = DrawingImage(entity.size)
        for (stroke in entity.strokes) {
            img.addStroke(stroke)
        }
        img.save()
        return img
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
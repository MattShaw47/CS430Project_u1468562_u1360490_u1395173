package com.example.drawingapp.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
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
        return try {
            val file = bitmapToTempFile(appContext, bitmap)
            FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToTempFile(context: Context, bitmap: Bitmap): File {
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val imageFile = File(imagesDir, "image${System.currentTimeMillis()}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return imageFile
    }

    // Helper methods to convert between entity and model
    private fun convertToEntity(drawingImage: DrawingImage, id: Long = 0): DrawingEntity {
        return DrawingEntity(id, drawingImage.getBitmap())
    }

    private fun convertToDrawingImage(entity: DrawingEntity): DrawingImage {
        val tmpImg = DrawingImage(1024)
        tmpImg.setBitmap(entity.bitmap)
        return tmpImg
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
package com.example.drawingapp.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareHelper {

    /**
     * Share a drawing as an image
     */
    fun shareDrawing(context: Context, bitmap: Bitmap) {
        try {
            val uri = saveBitmapToCache(context, bitmap)
            uri?.let {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Drawing"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Save bitmap to cache directory and return URI
     */
    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "shared_drawing_${System.currentTimeMillis()}.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convert DrawingImage to Bitmap for sharing
     */
    fun drawingToBitmap(
        width: Int,
        height: Int,
        strokes: List<com.example.drawingapp.model.Stroke>
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            strokeCap = android.graphics.Paint.Cap.ROUND
            style = android.graphics.Paint.Style.STROKE
        }

        strokes.forEach { stroke ->
            paint.strokeWidth = stroke.width
            paint.color = stroke.argb

            for (i in 0 until stroke.points.size - 1) {
                val start = stroke.points[i]
                val end = stroke.points[i + 1]
                canvas.drawLine(start.x, start.y, end.x, end.y, paint)
            }
        }

        return bitmap
    }
}
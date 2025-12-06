package com.example.drawingapp.model

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.util.ArrayDeque
import androidx.core.graphics.createBitmap

data class Point(val x: Float, val y: Float)
data class Stroke(
    val points: List<Point>,
    val width: Float,
    val argb: Int,
    val id: Long = System.nanoTime()
)

class DrawingImage(size: Int = 1024) {

    init {
        require(size > 0) { "size must be positive" }
    }

    var size: Int = size
        private set

    // Only used to get as a current stroke value onDrag in the canvas before updating the bitmap
    private val strokes = mutableListOf<Stroke>()

    private var bitmap: Bitmap? = null

    private data class Snapshot(val size: Int, val strokes: List<Stroke>, val bitmap: Bitmap?)

    private fun <T> ArrayDeque<T>.popLastOrNull(): T? =
        if (this.isEmpty()) null else this.removeLast()

    private val undo = ArrayDeque<Snapshot>()
    private val redo = ArrayDeque<Snapshot>()

    private var version: Int = 0
    private var lastSavedVersion: Int = -1
    val isChanged: Boolean get() = version != lastSavedVersion

    fun save() {
        lastSavedVersion = version
    }

    /**
     * Adds stroke to our current stroke list.
     */
    fun addStroke(stroke: Stroke) {
        record()
        strokes += stroke
        bumpVersion()
    }

    /**
     * Draws a list of points on our bitmap with color and width properties.
     */
    fun updateBitmap(points: List<Point>, color: Int, width: Float) {
        if (points.size < 2) return

        record()
        val bmp = ensureBitmap()
        val canvas = Canvas(bmp)
        val paint = Paint().apply {
            this.color = color
            this.strokeWidth = width
            this.style = Paint.Style.STROKE
            this.strokeCap = Paint.Cap.ROUND
            this.isAntiAlias = true
        }

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            canvas.drawLine(
                start.x,
                start.y,
                end.x,
                end.y,
                paint
            )
        }

        bumpVersion()
    }

    /**
     * Sets the bitmap to passed in param.
     */
    fun setBitmap(bitmap: Bitmap) {
        val cfg = bitmap.config ?: Bitmap.Config.ARGB_8888
        this.bitmap = bitmap.copy(cfg, true)
    }

    fun getBitmap(): Bitmap = ensureBitmap()

    /**
     * Resets bitmap to its initialized empty state and updates version history.
     */
    fun clearBmp() {
        if (this.bitmap == null) return
        record()
        val newBmp = createBitmap(size, size)
        setBitmap(newBmp)
        bumpVersion()
    }

    /**
     * Undoes the last changes made to the drawing.
     */
    fun undo(): Boolean {
        val snap = undo.popLastOrNull() ?: return false
        redo.addLast(currentSnapshot())
        restore(snap); bumpVersion(); return true
    }

    /**
     * Resets the last 'undo' done to the drawing.
     */
    fun redo(): Boolean {
        val snap = redo.popLastOrNull() ?: return false
        undo.addLast(currentSnapshot())
        restore(snap); bumpVersion(); return true
    }

    fun strokeList(): List<Stroke> = strokes.toList()

    // for comparison testing
    fun snapshot(): Any = currentSnapshot()

    fun cloneDeep(): DrawingImage {
        val copy = DrawingImage(this.size)
        val snap = currentSnapshot()
        copy.restore(snap)
        return copy
    }
    private fun ensureBitmap(): Bitmap {
        val b = bitmap
        if (b != null && !b.isRecycled) return b
        val fresh = createBitmap(size, size)
        bitmap = fresh
        return fresh
    }

    private fun record() {
        undo.addLast(currentSnapshot())
        redo.clear()
    }

    private fun bumpVersion() {
        version++
    }

    /**
     * Gets the current state 'snapshot' of the drawing image to track history.
     */
    private fun currentSnapshot(): Snapshot {
        val bmp = bitmap
        val bmpCopy =
            if (bmp != null && !bmp.isRecycled) {
                val cfg = bmp.config ?: Bitmap.Config.ARGB_8888
                val copy = createBitmap(bmp.width, bmp.height, cfg)
                Canvas(copy).drawBitmap(bmp, 0f, 0f, null)
                copy
            } else {
                null
            }

        return Snapshot(
            size = size,
            strokes = strokes.map { it.copy(points = it.points.map { p -> p.copy() }) },
            bitmap = bmpCopy
        )
    }

    /**
     * Restores a snapshot onto the current state of the image.
     */
    private fun restore(s: Snapshot) {
        size = s.size
        strokes.clear()
        strokes.addAll(s.strokes.map { it.copy(points = it.points.map { p -> p.copy() }) })

        val src = s.bitmap
        bitmap =
            if (src != null && !src.isRecycled) {
                val cfg = src.config ?: Bitmap.Config.ARGB_8888
                val restored = createBitmap(src.width, src.height, cfg)
                Canvas(restored).drawBitmap(src, 0f, 0f, null)
                restored
            } else {
                null
            }
    }
}
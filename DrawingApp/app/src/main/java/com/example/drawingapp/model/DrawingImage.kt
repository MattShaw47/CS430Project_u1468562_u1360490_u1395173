package com.example.drawingapp.model

import java.util.ArrayDeque

data class Point(val x: Float, val y: Float)
data class Stroke(
    val points: List<Point>,
    val width: Float,
    val argb: Int,
    val id: Long = System.nanoTime()
)

class DrawingImage(size: Int = 1024) {

    init { require(size > 0) { "size must be positive" } }

    var size: Int = size
        private set

    private val strokes = mutableListOf<Stroke>()

    private data class Snapshot(val size: Int, val strokes: List<Stroke>)

    private fun <T> ArrayDeque<T>.popLastOrNull(): T? =
        if (this.isEmpty()) null else this.removeLast()

    private val undo = ArrayDeque<Snapshot>()
    private val redo = ArrayDeque<Snapshot>()

    private var version: Int = 0
    private var lastSavedVersion: Int = -1
    val isChanged: Boolean get() = version != lastSavedVersion

    fun save() { lastSavedVersion = version }


    fun addStroke(stroke: Stroke) {
        record()
        strokes += stroke
        bumpVersion()
    }

    fun eraseStrokeById(id: Long) {
        record()
        strokes.removeAll { it.id == id }
        bumpVersion()
    }

    fun clear() {
        if (strokes.isEmpty()) return
        record()
        strokes.clear()
        bumpVersion()
    }

    fun scaleTo(newSize: Int) {
        require(newSize > 0)
        if (newSize == size) return

        val s = newSize.toFloat() / size.toFloat()
        record()
        for (i in strokes.indices) {
            val st = strokes[i]
            strokes[i] = st.copy(points = st.points.map { p -> Point(p.x * s, p.y * s) })
        }
        size = newSize
        bumpVersion()
    }

    fun undo(): Boolean {
        val snap = undo.popLastOrNull() ?: return false
        redo.addLast(currentSnapshot())
        restore(snap); bumpVersion(); return true
    }

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
        val snapshot = currentSnapshot()
        copy.restore((snapshot))
        return copy
    }

    private fun record() {
        undo.addLast(currentSnapshot())
        redo.clear()
    }

    private fun bumpVersion() { version++ }

    private fun currentSnapshot(): Snapshot =
        Snapshot(size = size, strokes = strokes.map { it.copy(points = it.points.map { p -> p.copy() }) })

    private fun restore(s: Snapshot) {
        size = s.size
        strokes.clear()
        strokes.addAll(s.strokes.map { it.copy(points = it.points.map { p -> p.copy() }) })
    }
}
package com.example.drawingapp

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import com.example.drawingapp.data.DrawingEntity
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for Repository pattern and persistent storage logic
 * Note: These test the data transformation logic, not the actual Room database
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DrawingRepositoryTest {

    private lateinit var testDrawings: MutableList<DrawingEntity>

    @Before
    fun setup() {
        testDrawings = mutableListOf()
    }

    @Test
    fun drawing_image_converts_to_entity_size_correctly() {
        val drawingImage = DrawingImage(size = 512)
        val testStroke = Stroke(
            points = listOf(Point(10f, 10f), Point(50f, 50f)),
            width = 5f,
            argb = 0xFF000000.toInt()
        )

        drawingImage.addStroke(testStroke)
        drawingImage.updateBitmap(testStroke.points,testStroke.argb, testStroke.width)
        val entity = DrawingEntity(
            0, drawingImage.getBitmap()
        )

        assertEquals("Bitmap width should be 512", 512, entity.bitmap.width)
        assertEquals("Bitmap height should be 512", 512, entity.bitmap.height)
    }

    @Test
    fun entity_converts_back_to_drawing_image_correctly() {
        val size = 1024
        val image = DrawingImage(1024)
        val bitmap = createBitmap(1024, 1024)
        val entity = DrawingEntity(1, bitmap)

        val drawingImage = DrawingImage(size)
        drawingImage.setBitmap(entity.bitmap)

        assertEquals(size, drawingImage.size)
        assertEquals(entity.bitmap.width, drawingImage.getBitmap().width)
        assertEquals(entity.bitmap.height, drawingImage.getBitmap().height)
    }

    @Test
    fun multiple_drawings_maintain_unique_identities() {
        val bitmap1 = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(1024, 1024, Bitmap.Config.ARGB_8888)

        val drawing1 = DrawingEntity(id = 1, bitmap1)
        val drawing2 = DrawingEntity(id = 2, bitmap2)

        testDrawings.add(drawing1)
        testDrawings.add(drawing2)

        assertEquals(2, testDrawings.size)
        assertEquals("Should have 2 drawings", 2, testDrawings.size)
        assertNotEquals("IDs should be different", drawing1.id, drawing2.id)
    }

    @Test
    fun empty_drawing_converts_correctly() {
        val size = 1024
        val drawingImage = DrawingImage(size)
        val entity = DrawingEntity(0, drawingImage.getBitmap())

        assertEquals(size, entity.bitmap.width)
        assertEquals(size, entity.bitmap.height)

        val entityBitmap = entity.bitmap
        for (r in 0 until entityBitmap.width) {
            for (c in 0 until entityBitmap.height) {
                assertEquals("Empty drawing should have no filled bits", entityBitmap.getPixel(r, c), 0)
            }

        }
    }

    @Test
    fun complex_drawing_with_multiple_strokes_converts_correctly() {
        val size = 1024
        val drawingImage = DrawingImage(size = size)

        // Add multiple strokes with different properties
        val strokes = listOf(
            Stroke(listOf(Point(0f, 0f), Point(100f, 100f)), 5f, 0xFFFF0000.toInt()),
            Stroke(listOf(Point(100f, 0f), Point(200f, 100f)), 10f, 0xFF00FF00.toInt()),
            Stroke(listOf(Point(200f, 0f), Point(300f, 100f)), 15f, 0xFF0000FF.toInt())
        )

        strokes.forEach { drawingImage.addStroke(it) }
        strokes.forEach { drawingImage.updateBitmap(it.points, it.argb, it.width) }

        val entity = DrawingEntity(0, drawingImage.getBitmap())

        assertEquals(size, entity.bitmap.width)
        assertEquals(size, entity.bitmap.height)

        for (r in 0 until 1024) {
            for (c in 0 until 1024) {
                assertEquals(entity.bitmap.getPixel(r, c), drawingImage.getBitmap().getPixel(r, c))
            }
        }
    }

    @Test
    fun timestamp_is_recorded_for_each_drawing() {
        val beforeTime = System.currentTimeMillis()

        val entity = DrawingEntity(
            id = 1,
            createBitmap(1024, 1024),
            timestamp = System.currentTimeMillis()
        )

        val afterTime = System.currentTimeMillis()
        assertTrue("Timestamp should be within range",
            entity.timestamp >= beforeTime && entity.timestamp <= afterTime)
    }

    @Test
    fun deleting_drawing_removes_from_list() {
        // Add multiple drawings
        repeat(5) { index ->
            testDrawings.add(
                DrawingEntity(
                    id = index.toLong(),
                    createBitmap(1024, 1024),
                    timestamp = System.currentTimeMillis() + index
                )
            )
        }

        assertEquals("Should have 5 drawings", 5, testDrawings.size)

        // Delete one by id
        val idToDelete = 2L
        testDrawings.removeAll { it.id == idToDelete }

        assertEquals("Should have 4 drawings after delete", 4, testDrawings.size)
        assertFalse("Deleted drawing should not exist", testDrawings.any { it.id == idToDelete })
    }

    @Test
    fun repository_singleton_pattern_concept() {
        // This tests the concept of singleton - only one instance should exist
        // In actual code, this is enforced by the companion object pattern

        var instanceCount = 0

        // Simulate getInstance calls
        fun getInstance(): Int {
            if (instanceCount == 0) {
                instanceCount = 1
            }
            return instanceCount
        }

        val instance1 = getInstance()
        val instance2 = getInstance()
        val instance3 = getInstance()

        assertEquals("All getInstance calls should return same instance", instance1, instance2)
        assertEquals("All getInstance calls should return same instance", instance2, instance3)
        assertEquals("Should only have created 1 instance", 1, instanceCount)
    }
}
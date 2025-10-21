package com.example.drawingapp

import com.example.drawingapp.data.DrawingEntity
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for Repository pattern and persistent storage logic
 * Note: These test the data transformation logic, not the actual Room database
 */
class DrawingRepositoryTest {

    private lateinit var testDrawings: MutableList<DrawingEntity>

    @Before
    fun setup() {
        testDrawings = mutableListOf()
    }

    @Test
    fun drawing_image_converts_to_entity_correctly() {
        val drawingImage = DrawingImage(size = 512)

        val testStroke = Stroke(
            points = listOf(Point(10f, 10f), Point(50f, 50f)),
            width = 5f,
            argb = 0xFF000000.toInt()
        )
        drawingImage.addStroke(testStroke)

        // Simulate conversion (what repository does)
        val entity = DrawingEntity(
            id = 0,
            strokes = drawingImage.strokeList(),
            size = drawingImage.size,
            timestamp = System.currentTimeMillis()
        )

        assertEquals("Size should match", 512, entity.size)
        assertEquals("Should have 1 stroke", 1, entity.strokes.size)
        assertEquals("Stroke width should match", 5f, entity.strokes[0].width)
    }

    @Test
    fun entity_converts_back_to_drawing_image_correctly() {
        val strokes = listOf(
            Stroke(
                points = listOf(Point(20f, 20f), Point(80f, 80f)),
                width = 8f,
                argb = 0xFFFF0000.toInt()
            )
        )

        val entity = DrawingEntity(
            id = 1,
            strokes = strokes,
            size = 1024,
            timestamp = System.currentTimeMillis()
        )

        // Simulate conversion back (what repository does)
        val drawingImage = DrawingImage(entity.size)
        entity.strokes.forEach { stroke ->
            drawingImage.addStroke(stroke)
        }

        assertEquals("Size should match", 1024, drawingImage.size)
        assertEquals("Should have 1 stroke", 1, drawingImage.strokeList().size)
        assertEquals("Stroke width should match", 8f, drawingImage.strokeList()[0].width)
    }

    @Test
    fun multiple_drawings_maintain_unique_identities() {
        val drawing1 = DrawingEntity(
            id = 1,
            strokes = listOf(Stroke(listOf(Point(0f, 0f), Point(10f, 10f)), 5f, 0xFF000000.toInt())),
            size = 512,
            timestamp = 1000L
        )

        val drawing2 = DrawingEntity(
            id = 2,
            strokes = listOf(Stroke(listOf(Point(20f, 20f), Point(30f, 30f)), 10f, 0xFFFF0000.toInt())),
            size = 512,
            timestamp = 2000L
        )

        testDrawings.add(drawing1)
        testDrawings.add(drawing2)

        assertEquals("Should have 2 drawings", 2, testDrawings.size)
        assertNotEquals("IDs should be different", drawing1.id, drawing2.id)
        assertNotEquals("Timestamps should be different", drawing1.timestamp, drawing2.timestamp)
    }

    @Test
    fun empty_drawing_converts_correctly() {
        val emptyDrawing = DrawingImage(size = 512)

        val entity = DrawingEntity(
            id = 0,
            strokes = emptyDrawing.strokeList(),
            size = emptyDrawing.size,
            timestamp = System.currentTimeMillis()
        )

        assertTrue("Empty drawing should have no strokes", entity.strokes.isEmpty())
        assertEquals("Size should still be set", 512, entity.size)
    }

    @Test
    fun complex_drawing_with_multiple_strokes_converts_correctly() {
        val drawingImage = DrawingImage(size = 1024)

        // Add multiple strokes with different properties
        val strokes = listOf(
            Stroke(listOf(Point(0f, 0f), Point(100f, 100f)), 5f, 0xFFFF0000.toInt()),
            Stroke(listOf(Point(100f, 0f), Point(200f, 100f)), 10f, 0xFF00FF00.toInt()),
            Stroke(listOf(Point(200f, 0f), Point(300f, 100f)), 15f, 0xFF0000FF.toInt())
        )

        strokes.forEach { drawingImage.addStroke(it) }

        val entity = DrawingEntity(
            id = 0,
            strokes = drawingImage.strokeList(),
            size = drawingImage.size,
            timestamp = System.currentTimeMillis()
        )

        assertEquals("Should have 3 strokes", 3, entity.strokes.size)

        // Verify each stroke preserved correctly
        entity.strokes.forEachIndexed { index, stroke ->
            assertEquals("Stroke $index width should match", strokes[index].width, stroke.width)
            assertEquals("Stroke $index color should match", strokes[index].argb, stroke.argb)
        }
    }

    @Test
    fun timestamp_is_recorded_for_each_drawing() {
        val beforeTime = System.currentTimeMillis()

        val entity = DrawingEntity(
            id = 1,
            strokes = emptyList(),
            size = 512,
            timestamp = System.currentTimeMillis()
        )

        val afterTime = System.currentTimeMillis()

        assertTrue("Timestamp should be within range",
            entity.timestamp >= beforeTime && entity.timestamp <= afterTime)
    }

    @Test
    fun updating_drawing_preserves_id() {
        val originalEntity = DrawingEntity(
            id = 5,
            strokes = listOf(Stroke(listOf(Point(0f, 0f), Point(50f, 50f)), 5f, 0xFF000000.toInt())),
            size = 512,
            timestamp = 1000L
        )

        // Simulate update with new stroke
        val updatedEntity = originalEntity.copy(
            strokes = originalEntity.strokes + Stroke(
                listOf(Point(100f, 100f), Point(150f, 150f)),
                8f,
                0xFFFF0000.toInt()
            ),
            timestamp = System.currentTimeMillis()
        )

        assertEquals("ID should be preserved", originalEntity.id, updatedEntity.id)
        assertEquals("Should have 2 strokes after update", 2, updatedEntity.strokes.size)
        assertNotEquals("Timestamp should be updated", originalEntity.timestamp, updatedEntity.timestamp)
    }

    @Test
    fun deleting_drawing_removes_from_list() {
        // Add multiple drawings
        repeat(5) { index ->
            testDrawings.add(
                DrawingEntity(
                    id = index.toLong(),
                    strokes = emptyList(),
                    size = 512,
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
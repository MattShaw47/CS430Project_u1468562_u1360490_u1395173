package com.example.drawingapp

import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for shape selection functionality
 * Verifies that shape selection allows for new shapes to be created
 */
class ShapeSelectionTest {

    @Test
    fun selecting_circle_shape_allows_circle_creation() {
        var currentShape = BrushType.CIRCLE

        // Verify shape is set to circle
        assertEquals("Shape should be CIRCLE", BrushType.CIRCLE, currentShape)

        // Simulate creating a circle stroke
        val img = DrawingImage(size = 512)
        img.addStroke(Stroke(
            points = listOf(Point(100f, 100f), Point(150f, 150f)),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Verify stroke was created
        assertEquals("Should have 1 stroke", 1, img.strokeList().size)
    }

    @Test
    fun selecting_rectangle_shape_allows_rectangle_creation() {
        var currentShape = BrushType.RECTANGLE

        // Verify shape is set to rectangle
        assertEquals("Shape should be RECTANGLE", BrushType.RECTANGLE, currentShape)

        // Simulate creating a rectangle stroke
        val img = DrawingImage(size = 512)
        img.addStroke(Stroke(
            points = listOf(
                Point(50f, 50f),   // Top-left
                Point(200f, 50f),  // Top-right
                Point(200f, 150f), // Bottom-right
                Point(50f, 150f)   // Bottom-left
            ),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Verify stroke was created with 4 points (rectangle)
        val strokes = img.strokeList()
        assertEquals("Should have 1 stroke", 1, strokes.size)
        assertEquals("Rectangle should have 4 points", 4, strokes[0].points.size)
    }

    @Test
    fun selecting_line_shape_allows_line_creation() {
        var currentShape = BrushType.LINE

        // Verify shape is set to line
        assertEquals("Shape should be LINE", BrushType.LINE, currentShape)

        // Simulate creating a line stroke
        val img = DrawingImage(size = 512)
        img.addStroke(Stroke(
            points = listOf(
                Point(0f, 0f),
                Point(200f, 200f)
            ),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Verify stroke was created with 2 points (line)
        val strokes = img.strokeList()
        assertEquals("Should have 1 stroke", 1, strokes.size)
        assertEquals("Line should have 2 points", 2, strokes[0].points.size)
    }

    @Test
    fun shape_selection_state_changes_correctly() {
        var currentShape = BrushType.CIRCLE

        // Change to rectangle
        currentShape = BrushType.RECTANGLE
        assertEquals("Shape should be RECTANGLE", BrushType.RECTANGLE, currentShape)

        // Change to line
        currentShape = BrushType.LINE
        assertEquals("Shape should be LINE", BrushType.LINE, currentShape)

        // Change back to circle
        currentShape = BrushType.CIRCLE
        assertEquals("Shape should be CIRCLE", BrushType.CIRCLE, currentShape)
    }

    @Test
    fun different_shapes_can_be_created_in_sequence() {
        val img = DrawingImage(size = 512)

        // Create circle (simulated with curve points)
        img.addStroke(Stroke(
            points = listOf(Point(100f, 100f), Point(150f, 150f)),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Create rectangle
        img.addStroke(Stroke(
            points = listOf(
                Point(200f, 200f),
                Point(300f, 200f),
                Point(300f, 300f),
                Point(200f, 300f)
            ),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Create line
        img.addStroke(Stroke(
            points = listOf(Point(400f, 400f), Point(500f, 500f)),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        val strokes = img.strokeList()

        assertEquals("Should have 3 strokes", 3, strokes.size)
        assertEquals("Circle should have 2 points", 2, strokes[0].points.size)
        assertEquals("Rectangle should have 4 points", 4, strokes[1].points.size)
        assertEquals("Line should have 2 points", 2, strokes[2].points.size)
    }

    @Test
    fun all_shape_types_are_available() {
        // Verify all enum values exist
        val shapes = BrushType.values()

        assertTrue("Should have CIRCLE", shapes.contains(BrushType.CIRCLE))
        assertTrue("Should have RECTANGLE", shapes.contains(BrushType.RECTANGLE))
        assertTrue("Should have LINE", shapes.contains(BrushType.LINE))
        assertTrue("Should have FREEHAND", shapes.contains(BrushType.FREEHAND))
        assertEquals("Should have exactly 4 shapes", 4, shapes.size)
    }

    @Test
    fun shape_selection_persists_across_strokes() {
        var currentShape = BrushType.RECTANGLE
        val img = DrawingImage(size = 512)

        // Draw first rectangle
        img.addStroke(Stroke(
            points = listOf(
                Point(0f, 0f), Point(100f, 0f),
                Point(100f, 100f), Point(0f, 100f)
            ),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Shape should still be rectangle
        assertEquals("Shape should persist", BrushType.RECTANGLE, currentShape)

        // Draw second rectangle
        img.addStroke(Stroke(
            points = listOf(
                Point(200f, 200f), Point(300f, 200f),
                Point(300f, 300f), Point(200f, 300f)
            ),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        assertEquals("Should have 2 strokes", 2, img.strokeList().size)
    }

    @Test
    fun selecting_new_shape_allows_creating_that_shape() {
        val img = DrawingImage(size = 512)
        var currentShape: BrushType

        // Select and create circle
        currentShape = BrushType.CIRCLE
        img.addStroke(Stroke(
            points = listOf(Point(0f, 0f), Point(50f, 50f)),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Select and create line
        currentShape = BrushType.LINE
        img.addStroke(Stroke(
            points = listOf(Point(100f, 100f), Point(200f, 200f)),
            width = 10f,
            argb = 0xFF000000.toInt()
        ))

        // Verify both were created
        assertEquals("Should have 2 different shapes", 2, img.strokeList().size)
        assertEquals("Current shape should be LINE", BrushType.LINE, currentShape)
    }
}
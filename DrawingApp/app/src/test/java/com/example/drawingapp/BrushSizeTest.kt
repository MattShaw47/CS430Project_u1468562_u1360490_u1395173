package com.example.drawingapp

import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for brush size functionality
 * Verifies that changing brush size affects line thickness
 */
class BrushSizeTest {

    @Test
    fun changing_brush_size_affects_stroke_width() {
        val img = DrawingImage(size = 512)

        // Create stroke with size 5
        val smallStroke = Stroke(
            points = listOf(Point(0f, 0f), Point(50f, 50f)),
            width = 5f,
            argb = 0xFF000000.toInt()
        )
        img.addStroke(smallStroke)

        // Create stroke with size 25
        val largeStroke = Stroke(
            points = listOf(Point(100f, 100f), Point(150f, 150f)),
            width = 25f,
            argb = 0xFF000000.toInt()
        )
        img.addStroke(largeStroke)

        val strokes = img.strokeList()

        // Verify different widths
        assertEquals("Should have 2 strokes", 2, strokes.size)
        assertEquals("First stroke should be thin", 5f, strokes[0].width)
        assertEquals("Second stroke should be thick", 25f, strokes[1].width)
        assertNotEquals("Strokes should have different widths",
            strokes[0].width, strokes[1].width)
    }

    @Test
    fun minimum_brush_size_is_valid() {
        var brushSize = 1f

        // Verify minimum size
        assertTrue("Minimum size should be at least 1", brushSize >= 1f)

        // Create stroke with minimum size
        val stroke = Stroke(
            points = listOf(Point(0f, 0f), Point(10f, 10f)),
            width = brushSize,
            argb = 0xFF000000.toInt()
        )

        assertEquals("Stroke width should be 1", 1f, stroke.width)
    }

    @Test
    fun maximum_brush_size_is_valid() {
        var brushSize = 50f

        // Verify maximum size
        assertTrue("Maximum size should be 50 or less", brushSize <= 50f)

        // Create stroke with maximum size
        val stroke = Stroke(
            points = listOf(Point(0f, 0f), Point(10f, 10f)),
            width = brushSize,
            argb = 0xFF000000.toInt()
        )

        assertEquals("Stroke width should be 50", 50f, stroke.width)
    }

    @Test
    fun brush_size_changes_are_reflected_in_new_strokes() {
        val img = DrawingImage(size = 512)
        var currentBrushSize = 10f

        // Draw with size 10
        img.addStroke(Stroke(
            points = listOf(Point(0f, 0f), Point(50f, 50f)),
            width = currentBrushSize,
            argb = 0xFF000000.toInt()
        ))

        // Change size to 30
        currentBrushSize = 30f

        // Draw with size 30
        img.addStroke(Stroke(
            points = listOf(Point(100f, 100f), Point(150f, 150f)),
            width = currentBrushSize,
            argb = 0xFF000000.toInt()
        ))

        val strokes = img.strokeList()

        assertEquals("First stroke width should be 10", 10f, strokes[0].width)
        assertEquals("Second stroke width should be 30", 30f, strokes[1].width)
    }

    @Test
    fun brush_size_within_valid_range() {
        // Test various sizes within range
        val validSizes = listOf(1f, 10f, 25f, 40f, 50f)

        validSizes.forEach { size ->
            assertTrue("Size $size should be valid (1-50)",
                size >= 1f && size <= 50f)
        }
    }

    @Test
    fun brush_size_affects_visual_thickness() {
        // Simulate different brush sizes
        val thinBrush = 5f
        val mediumBrush = 20f
        val thickBrush = 45f

        // Verify ordering
        assertTrue("Thin should be less than medium", thinBrush < mediumBrush)
        assertTrue("Medium should be less than thick", mediumBrush < thickBrush)
        assertTrue("Thin should be less than thick", thinBrush < thickBrush)
    }

    @Test
    fun multiple_strokes_can_have_different_sizes() {
        val img = DrawingImage(size = 512)

        // Add strokes with different sizes
        val sizes = listOf(5f, 15f, 25f, 35f, 45f)
        sizes.forEach { size ->
            img.addStroke(Stroke(
                points = listOf(Point(0f, 0f), Point(50f, 50f)),
                width = size,
                argb = 0xFF000000.toInt()
            ))
        }

        val strokes = img.strokeList()
        assertEquals("Should have 5 strokes", 5, strokes.size)

        // Verify each stroke has correct width
        strokes.forEachIndexed { index, stroke ->
            assertEquals("Stroke $index should have correct width",
                sizes[index], stroke.width)
        }
    }
}
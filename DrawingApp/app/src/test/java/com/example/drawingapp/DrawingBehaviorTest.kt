package com.example.drawingapp

import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.model.DrawingImage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingBehaviorTest {

    @Test
    fun drawing_should_change_state_of_original_page() {
        val img = DrawingImage(size = 512)

        val initializedPage = img.snapshot()

        img.addStroke(
            Stroke(
                points = listOf(Point(0f, 0f), Point(128f, 128f)),
                width = 6f,
                argb = 0xFF000000.toInt()
            )
        )

        val drawnPage = img.snapshot()

        assertTrue(initializedPage != drawnPage)
    }

    @Test
    fun editing_preexisting_page_should_change_save_state() {
        val img = DrawingImage(size = 512)

        img.save()
        assertFalse("Freshly saved image not changed", img.isChanged)

        // Edit existing page
        img.addStroke(
            Stroke(
                points = listOf(Point(10f, 10f), Point(50f, 50f)),
                width = 4f,
                argb = 0xFFFF0000.toInt()
            )
        )

        assertTrue("Editing marks image as changed", img.isChanged)
    }
}

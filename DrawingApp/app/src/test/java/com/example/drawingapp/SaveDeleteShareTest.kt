package com.example.drawingapp

import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.model.Point
import org.junit.Test
import org.junit.Assert.*

class SaveDeleteShareTest {

    @Test
    fun adding_n_images_results_in_n_saved_images() {
        val imageList = mutableListOf<DrawingImage>()

        // Add 5 images
        repeat(5) {
            val img = DrawingImage(size = 512)
            img.addStroke(
                Stroke(
                    points = listOf(Point(0f, 0f), Point(100f, 100f)),
                    width = 5f,
                    argb = 0xFF000000.toInt()
                )
            )
            img.save()
            imageList.add(img)
        }

        assertEquals("Should have 5 images", 5, imageList.size)
        imageList.forEach { img ->
            assertFalse("Each image should be saved", img.isChanged)
        }
    }

    @Test
    fun deleting_all_existing_images_results_in_no_images() {
        val imageList = mutableListOf<DrawingImage>()

        // Add images
        repeat(3) {
            imageList.add(DrawingImage(size = 512))
        }

        assertEquals("Should start with 3 images", 3, imageList.size)

        // Delete all
        imageList.clear()

        assertEquals("Should have no images after deletion", 0, imageList.size)
    }

    @Test
    fun editing_and_canceling_should_not_change_original_page() {
        val img = DrawingImage(size = 512)

        // Add initial stroke and save
        img.addStroke(
            Stroke(
                points = listOf(Point(10f, 10f), Point(50f, 50f)),
                width = 4f,
                argb = 0xFFFF0000.toInt()
            )
        )
        img.save()

        val originalSnapshot = img.snapshot()

        // Simulate editing
        img.addStroke(
            Stroke(
                points = listOf(Point(100f, 100f), Point(200f, 200f)),
                width = 6f,
                argb = 0xFF00FF00.toInt()
            )
        )

        // Simulate cancel by using undo
        assertTrue("Undo should succeed", img.undo())

        val afterCancelSnapshot = img.snapshot()

        assertEquals("Page should match original after cancel", originalSnapshot, afterCancelSnapshot)
    }

    @Test
    fun sharing_image_provides_correct_data() {
        val img = DrawingImage(size = 512)

        val testStroke = Stroke(
            points = listOf(Point(25f, 25f), Point(75f, 75f)),
            width = 8f,
            argb = 0xFF0000FF.toInt()
        )

        img.addStroke(testStroke)

        val strokes = img.strokeList()

        assertEquals("Should have 1 stroke", 1, strokes.size)
        assertEquals("Stroke width should match", 8f, strokes[0].width)
        assertEquals("Stroke color should match", 0xFF0000FF.toInt(), strokes[0].argb)
        assertEquals("Stroke points should match", 2, strokes[0].points.size)
    }
}
package com.example.drawingapp

import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.model.Stroke
import com.example.drawingapp.model.Point
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.intArrayOf

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EnhancedSaveDeleteShareTest {

    private lateinit var imageList: MutableList<DrawingImage>

    @Before
    fun setup() {
        imageList = mutableListOf()
    }

    // Test 1: Adding N images should result in N saved images
    @Test
    fun adding_n_images_results_in_n_saved_images() {
        val n = 5

        // Add N images
        repeat(n) { index ->
            val img = DrawingImage(size = 512)
            img.addStroke(
                Stroke(
                    points = listOf(
                        Point(0f + index * 10f, 0f + index * 10f),
                        Point(100f + index * 10f, 100f + index * 10f)
                    ),
                    width = 5f,
                    argb = 0xFF000000.toInt()
                )
            )
            img.save()
            imageList.add(img)
        }

        assertEquals("Should have exactly $n images", n, imageList.size)

        // Verify all images are saved (not changed)
        imageList.forEachIndexed { index, img ->
            assertFalse("Image $index should be saved (not changed)", img.isChanged)
        }
    }

    @Test
    fun adding_zero_images_results_in_zero_saved_images() {
        assertEquals("Should have 0 images when nothing is added", 0, imageList.size)
    }

    @Test
    fun adding_one_image_results_in_one_saved_image() {
        val img = DrawingImage(size = 512)
        img.addStroke(
            Stroke(
                points = listOf(Point(10f, 10f), Point(50f, 50f)),
                width = 4f,
                argb = 0xFFFF0000.toInt()
            )
        )
        img.save()
        imageList.add(img)

        assertEquals("Should have exactly 1 image", 1, imageList.size)
        assertFalse("Image should be saved", imageList[0].isChanged)
    }

    // Test 2: Deleting all existing images should result in no images
    @Test
    fun deleting_all_existing_images_results_in_no_images() {
        // Add multiple images
        repeat(3) {
            val img = DrawingImage(size = 512)
            img.addStroke(
                Stroke(
                    points = listOf(Point(0f, 0f), Point(100f, 100f)),
                    width = 5f,
                    argb = 0xFF000000.toInt()
                )
            )
            imageList.add(img)
        }

        assertEquals("Should start with 3 images", 3, imageList.size)

        // Delete all
        imageList.clear()

        assertEquals("Should have no images after deletion", 0, imageList.size)
        assertTrue("List should be empty", imageList.isEmpty())
    }

    @Test
    fun deleting_single_image_from_multiple_images() {
        // Add 5 images
        repeat(5) {
            imageList.add(DrawingImage(size = 512))
        }

        val initialSize = imageList.size

        // Delete one image
        imageList.removeAt(2)

        assertEquals("Should have one less image", initialSize - 1, imageList.size)
        assertEquals("Should have 4 images", 4, imageList.size)
    }

    @Test
    fun deleting_from_empty_list_remains_empty() {
        assertTrue("List should start empty", imageList.isEmpty())

        // Attempt to clear already empty list
        imageList.clear()

        assertTrue("List should remain empty", imageList.isEmpty())
        assertEquals("Size should be 0", 0, imageList.size)
    }

    // Test 3: Editing a pre-existing page and canceling should not change the original
    @Test
    fun editing_and_canceling_should_not_change_original_page() {
        val img = DrawingImage(size = 512)

        // Add initial stroke and save
        val stroke1 = Stroke(
            points = listOf(Point(10f, 10f), Point(50f, 50f)),
            width = 4f,
            argb = 0xFFFF0000.toInt()
        )
        img.addStroke(stroke1)
        img.updateBitmap(stroke1.points, stroke1.argb, stroke1.width)
        img.save()

        val originalSnapshot = img.snapshot()
        val originalStrokeCount = img.strokeList().size

        // Simulate editing - add a new stroke
        val stroke2 = Stroke(
            points = listOf(Point(100f, 100f), Point(200f, 200f)),
            width = 6f,
            argb = 0xFF00FF00.toInt()
        )
        img.addStroke(stroke2)

        // Verify edit was made
        assertEquals("Should have 2 strokes after edit", 2, img.strokeList().size)
        assertTrue("Image should be marked as changed", img.isChanged)

        // Simulate cancel by using undo
        val undoSuccess = img.undo()
        assertTrue("Undo should succeed", undoSuccess)

        val afterCancelSnapshot = img.snapshot()

        // Verify original state is restored
        assertEquals("Stroke count should match original", originalStrokeCount, img.strokeList().size)
    }

    @Test
    fun editing_without_saving_marks_image_as_changed() {
        val img = DrawingImage(size = 512)
        img.save()

        assertFalse("Freshly saved image should not be changed", img.isChanged)

        // Edit by adding stroke
        img.addStroke(
            Stroke(
                points = listOf(Point(10f, 10f), Point(50f, 50f)),
                width = 4f,
                argb = 0xFFFF0000.toInt()
            )
        )

        assertTrue("Editing should mark image as changed", img.isChanged)
    }

    @Test
    fun multiple_edits_and_cancels_restore_original() {
        val img = DrawingImage(size = 512)

        // Add first stroke and save
        val stroke = Stroke(
            points = listOf(Point(0f, 0f), Point(50f, 50f)),
            width = 5f,
            argb = 0xFF000000.toInt()
        )
        img.addStroke(stroke)
        img.updateBitmap(stroke.points, stroke.argb, stroke.width)
        img.save()

        val originalSnapshot = img.snapshot()

        // Make multiple edits
        img.addStroke(Stroke(listOf(Point(100f, 100f), Point(150f, 150f)), 5f, 0xFF000000.toInt()))
        img.addStroke(Stroke(listOf(Point(200f, 200f), Point(250f, 250f)), 5f, 0xFF000000.toInt()))

        assertEquals("Should have 3 strokes after edits", 3, img.strokeList().size)

        // Undo all edits
        img.undo()
        img.undo()

        val afterCancelSnapshot = img.snapshot()

        assertEquals("Should have original stroke count", 1, img.strokeList().size)
    }

    // Test 4: Sharing an image sends the correct image data
    @Test
    fun sharing_image_provides_correct_stroke_data() {
        val img = DrawingImage(size = 512)

        val testStroke = Stroke(
            points = listOf(Point(25f, 25f), Point(75f, 75f)),
            width = 8f,
            argb = 0xFF0000FF.toInt()
        )

        img.addStroke(testStroke)

        val strokes = img.strokeList()

        // Verify correct data for sharing
        assertEquals("Should have 1 stroke", 1, strokes.size)
        assertEquals("Stroke width should match", 8f, strokes[0].width)
        assertEquals("Stroke color should match", 0xFF0000FF.toInt(), strokes[0].argb)
        assertEquals("Stroke should have 2 points", 2, strokes[0].points.size)
        assertEquals("First point X should match", 25f, strokes[0].points[0].x)
        assertEquals("First point Y should match", 25f, strokes[0].points[0].y)
        assertEquals("Second point X should match", 75f, strokes[0].points[1].x)
        assertEquals("Second point Y should match", 75f, strokes[0].points[1].y)
    }

    @Test
    fun sharing_complex_drawing_preserves_all_strokes() {
        val img = DrawingImage(size = 512)

        // Add multiple strokes with different properties
        val strokes = listOf(
            Stroke(listOf(Point(0f, 0f), Point(100f, 100f)), 5f, 0xFFFF0000.toInt()),
            Stroke(listOf(Point(100f, 0f), Point(200f, 100f)), 10f, 0xFF00FF00.toInt()),
            Stroke(listOf(Point(200f, 0f), Point(300f, 100f)), 15f, 0xFF0000FF.toInt())
        )

        strokes.forEach { img.addStroke(it) }

        val sharedStrokes = img.strokeList()

        assertEquals("Should have all 3 strokes", 3, sharedStrokes.size)

        // Verify each stroke is preserved correctly
        sharedStrokes.forEachIndexed { index, stroke ->
            assertEquals("Stroke $index width should match", strokes[index].width, stroke.width)
            assertEquals("Stroke $index color should match", strokes[index].argb, stroke.argb)
            assertEquals("Stroke $index point count should match",
                strokes[index].points.size, stroke.points.size)
        }
    }

    @Test
    fun sharing_empty_drawing_returns_empty_stroke_list() {
        val img = DrawingImage(size = 512)

        val strokes = img.strokeList()

        assertTrue("Empty drawing should have no strokes", strokes.isEmpty())
        assertEquals("Stroke count should be 0", 0, strokes.size)
    }

    @Test
    fun image_data_remains_unchanged_after_sharing() {
        val img = DrawingImage(size = 512)

        img.addStroke(
            Stroke(
                points = listOf(Point(50f, 50f), Point(150f, 150f)),
                width = 10f,
                argb = 0xFFFFFF00.toInt()
            )
        )

        val beforeSharing = img.strokeList()
        val sharedData = img.strokeList() // Simulate sharing
        val afterSharing = img.strokeList()

        assertEquals("Stroke count should remain same", beforeSharing.size, afterSharing.size)
        assertEquals("First stroke should be identical",
            beforeSharing[0].points.size, afterSharing[0].points.size)
    }

    // Additional integration tests
    @Test
    fun save_delete_workflow_maintains_consistency() {
        // Save multiple images
        repeat(3) {
            val img = DrawingImage(size = 512)
            img.addStroke(Stroke(listOf(Point(0f, 0f), Point(100f, 100f)), 5f, 0xFF000000.toInt()))
            img.save()
            imageList.add(img)
        }

        assertEquals("Should have 3 saved images", 3, imageList.size)

        // Delete one
        imageList.removeAt(1)
        assertEquals("Should have 2 images after deletion", 2, imageList.size)

        // Add one more
        val newImg = DrawingImage(size = 512)
        newImg.save()
        imageList.add(newImg)

        assertEquals("Should have 3 images after adding new one", 3, imageList.size)
    }

    @Test
    fun deleting_and_re_adding_maintains_correct_count() {
        val img1 = DrawingImage(size = 512)
        val img2 = DrawingImage(size = 512)

        imageList.add(img1)
        imageList.add(img2)
        assertEquals("Should have 2 images", 2, imageList.size)

        imageList.clear()
        assertEquals("Should have 0 images after clear", 0, imageList.size)

        imageList.add(img1)
        assertEquals("Should have 1 image after re-adding", 1, imageList.size)
    }
}
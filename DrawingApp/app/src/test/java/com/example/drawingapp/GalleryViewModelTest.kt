package com.example.drawingapp

import org.junit.Assert.*
import org.junit.Test
import com.example.drawingapp.model.DrawingImage

class GalleryViewModelTest {

    @Test
    fun number_of_saved_pages_equals_gallery_pages() {
        val vm = DrawingAppViewModel()
        repeat(5) { vm.addDrawing(DrawingImage(size = 512)) }
        val list = vm.drawings.value
        assertEquals("Should have 5 images", 5, list.size)
    }

    @Test
    fun deleting_a_saved_image_reduces_count_by_one() {
        val vm = DrawingAppViewModel()
        repeat(3) { vm.addDrawing(DrawingImage(size = 512)) }
        val before = vm.drawings.value.size
        val removeIndex = 1
        vm.removeAt(removeIndex)
        val after = vm.drawings.value.size
        assertEquals("Should have one less after deletion", before - 1, after)
    }

    @Test
    fun changing_state_of_gallery_image_reflects_in_vm() {
        val vm = DrawingAppViewModel()
        repeat(2) { vm.addDrawing(DrawingImage(size = 512)) }

        assertTrue(vm.selected.value.isEmpty())

        vm.toggleSelected(1)
        assertTrue(1 in vm.selected.value)

        vm.toggleSelected(1)
        assertTrue(vm.selected.value.isEmpty())
    }

    @Test
    fun selection_shifts_after_delete() {
        val vm = DrawingAppViewModel()
        repeat(3) { vm.addDrawing(DrawingImage(512)) }
        vm.toggleSelected(2)
        vm.removeAt(1)
        assertTrue(1 in vm.selected.value)
        assertTrue(2 !in vm.selected.value)
    }
}

package com.example.drawingapp

import android.graphics.Bitmap
import android.net.Uri
import com.example.drawingapp.data.DrawingDataSource
import org.junit.Assert.*
import org.junit.Test
import com.example.drawingapp.model.DrawingImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before


private class FakeDrawingRepository : DrawingDataSource {
    private val counter = AtomicLong(0L)
    private val store = mutableListOf<Pair<Long, DrawingImage>>()
    override val allDrawingsWithIds = MutableStateFlow<List<Pair<Long, DrawingImage>>>(emptyList())

    override suspend fun insertDrawing(drawingImage: DrawingImage): Long {
        val id = counter.incrementAndGet()
        store.add(id to drawingImage.cloneDeep())
        allDrawingsWithIds.update { store.toList() }
        return id
    }

    override suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {
        val idx = store.indexOfFirst { it.first == id }
        if (idx >= 0) {
            store[idx] = id to drawingImage.cloneDeep()
            allDrawingsWithIds.update { store.toList() }
        }
    }

    override suspend fun deleteDrawing(id: Long) {
        store.removeAll { it.first == id }
        allDrawingsWithIds.update { store.toList() }
    }

    override suspend fun deleteAllDrawings() {
        store.clear()
        allDrawingsWithIds.update { emptyList() }
    }

    override fun shareDrawing(bitmap: Bitmap): Uri? {
        TODO("Not yet implemented")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun number_of_saved_pages_equals_gallery_pages() = runTest {
        val testBg = StandardTestDispatcher(testScheduler)    // <-- controlled
        val vm = DrawingAppViewModel(FakeDrawingRepository(), testBg)

        repeat(5) {
            vm.startNewDrawing()
            vm.insertActive()
        }
        advanceUntilIdle()    // flush insert coroutines on testBg
        assertEquals(5, vm.drawings.value.size)
    }

    @Test
    fun deleting_a_saved_image_reduces_count_by_one() = runTest {
        val testBg = StandardTestDispatcher(testScheduler)
        val vm = DrawingAppViewModel(FakeDrawingRepository(), testBg)

        repeat(3) { vm.startNewDrawing(); vm.insertActive() }
        advanceUntilIdle()

        val before = vm.drawings.value.size
        vm.deleteAt(1)
        advanceUntilIdle()

        val after = vm.drawings.value.size
        assertEquals(before - 1, after)
    }

    @Test
    fun changing_state_of_gallery_image_reflects_in_vm() = runTest {
        val vm = DrawingAppViewModel(FakeDrawingRepository())
        repeat(2) {
            vm.startNewDrawing(); vm.insertActive()
        }
        advanceUntilIdle()

        assertTrue(vm.selected.value.isEmpty())

        vm.toggleSelected(1)
        assertTrue(1 in vm.selected.value)

        vm.toggleSelected(1)
        assertTrue(vm.selected.value.isEmpty())
    }

    @Test
    fun selection_shifts_after_delete() = runTest {
        val vm = DrawingAppViewModel(FakeDrawingRepository())
        repeat(3) {
            vm.startNewDrawing(); vm.insertActive()
        }
        advanceUntilIdle()

        vm.toggleSelected(2)
        vm.deleteAt(1)
        advanceUntilIdle()

        assertTrue(1 in vm.selected.value)
        assertTrue(2 !in vm.selected.value)
    }
}

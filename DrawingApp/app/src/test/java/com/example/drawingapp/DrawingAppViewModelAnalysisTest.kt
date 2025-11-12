package com.example.drawingapp

import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.drawingapp.data.DrawingDataSource
import com.example.drawingapp.data.VisionResponse
import com.example.drawingapp.model.DrawingImage
import com.example.drawingapp.DrawingAppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicLong

private class FakeRepoSuccess : DrawingDataSource {
    private val counter = AtomicLong(0L)
    private val store = mutableListOf<Pair<Long, DrawingImage>>()
    override val allDrawingsWithIds = MutableStateFlow<List<Pair<Long, DrawingImage>>>(emptyList())

    override suspend fun insertDrawing(drawingImage: DrawingImage): Long {
        val id = counter.incrementAndGet()
        store.add(id to drawingImage)
        allDrawingsWithIds.update { store.toList() }
        return id
    }
    override suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {}
    override suspend fun deleteDrawing(id: Long) {}
    override suspend fun deleteAllDrawings() {}
    override fun shareDrawing(bitmap: Bitmap): Uri? = null
    override suspend fun sendVisionRequest(imageBmp: Bitmap): VisionResponse {
        return VisionResponse(responses = listOf())
    }
}

private class FakeRepoError : DrawingDataSource {
    override val allDrawingsWithIds = MutableStateFlow<List<Pair<Long, DrawingImage>>>(emptyList())
    override suspend fun insertDrawing(drawingImage: DrawingImage) = 1L
    override suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {}
    override suspend fun deleteDrawing(id: Long) {}
    override suspend fun deleteAllDrawings() {}
    override fun shareDrawing(bitmap: Bitmap): Uri? = null
    override suspend fun sendVisionRequest(imageBmp: Bitmap): VisionResponse {
        throw RuntimeException("Mock HTTP 500")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = android.app.Application::class, sdk = [30])
class DrawingAppViewModelAnalysisTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun analyze_success_sets_currentAnalysis_and_no_error() = runTest {
        ApplicationProvider.getApplicationContext<android.content.Context>()

        val vm = DrawingAppViewModel(FakeRepoSuccess(), dispatcher)
        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)

        vm.analyzeDrawing(bmp)
        advanceUntilIdle()

        assertNotNull(vm.currentAnalysis.value)
        assertNull(vm.analysisError.value)
    }

    @Test
    fun analyze_failure_sets_error_and_clears_currentAnalysis() = runTest {
        ApplicationProvider.getApplicationContext<android.content.Context>()

        val vm = DrawingAppViewModel(FakeRepoError(), dispatcher)
        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)

        vm.analyzeDrawing(bmp)
        advanceUntilIdle()

        assertNull(vm.currentAnalysis.value)
        assertEquals("Mock HTTP 500", vm.analysisError.value)
    }
}

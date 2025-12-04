package com.example.drawingapp.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import com.example.drawingapp.BuildConfig
import com.example.drawingapp.model.DrawingImage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class VisionResponse(val responses: List<Response>? = null)

@Serializable
data class Response(
    @SerialName("localizedObjectAnnotations")
    val localizedObjectAnnotations: List<LocalizedObjectAnnotation>? = null,
    @SerialName("labelAnnotations")
    val labelAnnotations: List<EntityAnnotation>? = null,
    @SerialName("textAnnotations")
    val textAnnotations: List<EntityAnnotation>? = null
)

@Serializable
data class LocalizedObjectAnnotation(
    val mid: String? = null,
    val name: String? = null,
    val score: Float? = null,
    @SerialName("boundingPoly")
    val boundingPoly: BoundingPoly? = null
)

@Serializable
data class BoundingPoly(
    @SerialName("normalizedVertices")
    val normalizedVertices: List<NormalizedVertex>? = null)

@Serializable
data class NormalizedVertex(val x: Float?, val y: Float?)

@Serializable
data class EntityAnnotation(
    val description: String? = null,
    val score: Float? = null
)

// Repository pattern - single source of truth for drawing data
class DrawingRepository private constructor(context: Context) : DrawingDataSource {

    private val client = HttpClient(Android)
    {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    /**
     * Sends our current image to Google Cloud Vision for analysis and sends the
     * response data to the UI.
     */
    override suspend fun sendVisionRequest(imageBmp: Bitmap): VisionResponse {
        val key = BuildConfig.VISION_API_KEY
        val requestBody = getRequestBody(imageBmp)

        val response: VisionResponse =
            client.post("https://vision.googleapis.com/v1/images:annotate") {
                url { parameters.append("key", key) }
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body()

        return response
    }

    private fun getRequestBody(img: Bitmap): JsonObject {
        val imgAsBase64 = getBmpAsBase64(img)
        val body: JsonObject = buildJsonObject {
            put("requests", buildJsonArray {
                add(buildJsonObject {
                    put("image", buildJsonObject {
                        put("content", JsonPrimitive(imgAsBase64))
                    })
                    put("features", buildJsonArray {
                        add(buildJsonObject {
                            put("type", JsonPrimitive("OBJECT_LOCALIZATION"))
                            put("maxResults", JsonPrimitive(10))
                        })
                        add(buildJsonObject {
                            put("type", JsonPrimitive("LABEL_DETECTION"))
                            put("maxResults", JsonPrimitive(10))
                        })
                        add(buildJsonObject {
                            put("type", JsonPrimitive("TEXT_DETECTION"))
                        })
                    })
                })
            })
        }

        return body
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun getBmpAsBase64(bmp: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val byteArray = outputStream.toByteArray()
        val b64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return b64String
    }

    private val drawingDao: DrawingDao = DrawingDatabase.getDatabase(context).drawingDao()
    private val appContext = context.applicationContext

    // Get all drawings from database
    val allDrawings: Flow<List<DrawingImage>> = drawingDao.getAllDrawings().map { entities ->
        entities.map { entity -> convertToDrawingImage(entity) }
    }

    override val allDrawingsWithIds: Flow<List<Pair<Long, DrawingImage>>> =
        drawingDao.getAllDrawings().map { entities ->
            entities.map { e -> e.id to convertToDrawingImage(e) }
        }

    override suspend fun insertDrawing(drawingImage: DrawingImage): Long {
        val entity = convertToEntity(drawingImage)
        return drawingDao.insertDrawing(entity)
    }

    override suspend fun updateDrawing(id: Long, drawingImage: DrawingImage) {
        val entity = convertToEntity(drawingImage, id)
        drawingDao.updateDrawing(entity)
    }

    override suspend fun deleteDrawing(id: Long) {
        val entity = drawingDao.getDrawingById(id)
        if (entity != null) {
            drawingDao.deleteDrawing(entity)
        }
    }

    override suspend fun deleteAllDrawings() {
        drawingDao.deleteAllDrawings()
    }

    suspend fun getDrawingById(id: Long): DrawingImage? {
        val entity = drawingDao.getDrawingById(id)
        return if (entity != null) convertToDrawingImage(entity) else null
    }

    suspend fun getDrawingCount(): Int {
        return drawingDao.getDrawingCount()
    }

    override fun shareDrawing(bitmap: Bitmap): Uri? {
        return try {
            val file = bitmapToTempFile(appContext, bitmap)
            FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToTempFile(context: Context, bitmap: Bitmap): File {
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        val imageFile = File(imagesDir, "image${System.currentTimeMillis()}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return imageFile
    }

    // Helper methods to convert between entity and model
    private fun convertToEntity(drawingImage: DrawingImage, id: Long = 0): DrawingEntity {
        return DrawingEntity(id, drawingImage.getBitmap())
    }

    private fun convertToDrawingImage(entity: DrawingEntity): DrawingImage {
        val tmpImg = DrawingImage(1024)
        tmpImg.setBitmap(entity.bitmap)
        return tmpImg
    }

    companion object {
        @Volatile
        private var INSTANCE: DrawingRepository? = null

        fun getInstance(context: Context): DrawingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = DrawingRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
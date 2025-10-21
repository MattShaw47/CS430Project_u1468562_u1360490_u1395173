package com.example.drawingapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.drawingapp.model.Point
import com.example.drawingapp.model.Stroke
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "drawings")
@TypeConverters(Converters::class)
data class DrawingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val strokes: List<Stroke>,
    val size: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStrokeList(strokes: List<Stroke>): String {
        return gson.toJson(strokes)
    }

    @TypeConverter
    fun toStrokeList(strokesString: String): List<Stroke> {
        val type = object : TypeToken<List<Stroke>>() {}.type
        return gson.fromJson(strokesString, type)
    }
}
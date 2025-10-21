package com.example.drawingapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drawingapp.data.DrawingRepository

class DrawingAppViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = DrawingRepository.getInstance(context)
        @Suppress("UNCHECKED_CAST")
        return DrawingAppViewModel(repo) as T
    }
}
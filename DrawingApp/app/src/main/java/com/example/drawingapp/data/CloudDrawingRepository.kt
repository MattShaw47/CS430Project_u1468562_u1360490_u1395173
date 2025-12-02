package com.example.drawingapp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.drawingapp.model.DrawingImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class CloudDrawingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun requireUid(): String {
        val uid = Firebase.auth.currentUser?.uid
        require(uid != null) { "User must be signed in to use cloud drawings." }
        return uid
    }

    private fun drawingsCollection(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("drawings")

    suspend fun uploadDrawing(localId: Long, drawing: DrawingImage) {
        // TODO
    }

    suspend fun unshareDrawing(localId: Long) {
        // TODO
    }

    suspend fun getSharedIds(): Set<Long> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptySet()

        val snapshot = drawingsCollection(uid)
            .get()
            .await()

        return snapshot.documents
            .mapNotNull { it.getLong("id") }
            .toSet()
    }
}

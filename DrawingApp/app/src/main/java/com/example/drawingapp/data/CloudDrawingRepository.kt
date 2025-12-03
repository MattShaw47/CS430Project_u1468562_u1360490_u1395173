package com.example.drawingapp.data

import android.graphics.Bitmap
import com.example.drawingapp.model.DrawingImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class CloudDrawingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    // Helper to make sure we don't try to upload if logged out
    private fun getUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")
    }

    private fun getDrawingsCollection(uid: String) =
        db.collection("users").document(uid).collection("drawings")

    suspend fun uploadDrawing(localId: Long, drawing: DrawingImage) {
        val uid = getUserId()

        // 1. Prepare image data (compress bitmap to PNG)
        val bmp = drawing.getBitmap()
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        // 2. Upload to Firebase Storage
        // Path: images/{userId}/{drawingId}.png
        val storageRef = storage.reference.child("images/$uid/$localId.png")
        storageRef.putBytes(data).await()

        // 3. Get the download URL so we can save it to the DB
        val downloadUrl = storageRef.downloadUrl.await().toString()

        // 4. Update Firestore so the app knows this ID is shared
        val drawingData = hashMapOf(
            "id" to localId,
            "url" to downloadUrl,
            "uploadedAt" to System.currentTimeMillis()
        )

        getDrawingsCollection(uid).document(localId.toString())
            .set(drawingData)
            .await()
    }

    suspend fun unshareDrawing(localId: Long) {
        val uid = getUserId()

        // Remove the record from Firestore
        getDrawingsCollection(uid).document(localId.toString())
            .delete()
            .await()

        // Try to remove the file from Storage
        try {
            val storageRef = storage.reference.child("images/$uid/$localId.png")
            storageRef.delete().await()
        } catch (e: Exception) {
            // If the file is already gone or not found, just ignore it
            e.printStackTrace()
        }
    }

    suspend fun getSharedIds(): Set<Long> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptySet()

        // Get all documents in the user's 'drawings' collection
        val snapshot = getDrawingsCollection(uid).get().await()

        // Return a set of the IDs
        return snapshot.documents
            .mapNotNull { it.getLong("id") }
            .toSet()
    }
}
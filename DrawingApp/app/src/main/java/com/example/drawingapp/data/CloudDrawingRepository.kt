package com.example.drawingapp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.example.drawingapp.model.DrawingImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class CloudDrawingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {

    // Helper to make sure we don't try to upload if logged out
    private fun getUserId(): String {
        return Firebase.auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in")
    }

    /**
     * Gets all saved drawing UIDs for the passed in user.
     */
    private fun getSavedDrawings(uid: String) =
        db.collection("users").document(uid).collection("saved")

    /**
     * Gets all shared drawing UIDs for the passed in user.
     */
    private fun getSharedDrawings(uid: String) =
        db.collection("users").document(uid).collection("shared")

    /**
     * Gets all received drawing UIDs for the passed in user.
     */
    private fun getReceivedDrawings(uid: String) =
        db.collection("users").document(uid).collection("received")

    suspend fun getReceivedImagesWithSenders(): Pair<List<Bitmap>, List<String>> {
        if (Firebase.auth.currentUser == null) return emptyList<Bitmap>() to emptyList()

        val bitmaps = mutableListOf<Bitmap>()
        val senders = mutableListOf<String>()

        return try {
            val uid = getUserId()
            val snapshot = db.collection("users")
                .document(uid)
                .collection("received")
                .get()
                .await()

            val maxBytes = 2L * 1024 * 1024

            for (doc in snapshot.documents) {
                val id = doc.id // e.g. "123.png"
                val storageRef = storage.reference.child("images/$uid/$id")
                val bytes = storageRef.getBytes(maxBytes).await()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: continue

                bitmaps.add(bmp)
                val senderEmail = doc.getString("senderEmail") ?: ""
                senders.add(senderEmail)
            }

            bitmaps to senders
        } catch (e: Exception) {
            emptyList<Bitmap>() to emptyList()
        }
    }

    /**
     * Gets the UID's of all images that have been saved to the cloud as a set.
     */
    suspend fun getSharedIds(): Set<Long> {
        val uid = Firebase.auth.currentUser?.uid ?: return emptySet()

        // Get all documents in the user's 'drawings' collection
        val snapshot = getSavedDrawings(uid).get().await()

        // Return a set of the IDs
        return snapshot.documents
            .mapNotNull { it.getLong("id") }
            .toSet()
    }

    /**
     * Uploads an image to the cloud.
     */
    suspend fun uploadDrawing(localId: Long, drawing: DrawingImage) {
        val uid = getUserId()
        val downloadUrl = addDrawingToStorage(localId, drawing, uid)

        // 4. Update Firestore so the app knows this ID is shared
        val drawingData = hashMapOf(
            "id" to localId,
            "url" to downloadUrl,
            "uploadedAt" to System.currentTimeMillis()
        )

        getSavedDrawings(uid).document("$localId.png").set(drawingData).await()
    }

    /**
     * Removes an uploaded cloud image from the users /images/UID/... folder.
     */
    suspend fun unshareDrawing(localId: Long) {
        val uid = getUserId()
        val docId = "$localId.png"

        // get the saved drawing collection for curr user and delete in firestore
        getSavedDrawings(uid).document(localId.toString()).delete().await()

        // Try to remove the file from Storage
        try {
            val storageRef = storage.reference.child("images/$uid/$docId")
            storageRef.delete().await()
        } catch (e: Exception) {
            // If the file is already gone or not found, just ignore it
            e.printStackTrace()
        }
    }

    /**
     * Shares and stores a drawing for existing recipient under the passed in email.
     */
    suspend fun shareDrawingWithUser(localId: Long, drawing: DrawingImage, userEmail: String) {
        val currUid = getUserId()
        val senderEmail = Firebase.auth.currentUser?.email

        try {
            val snapshot = db.collection("users").whereEqualTo("email", userEmail).get().await()

            if (snapshot.isEmpty) {
                throw Exception("No user exists with the email: $userEmail")
            }

            // get and store
            val recipientUid = snapshot.documents[0].id
            val currUserDownloadUrl = addDrawingToStorage(localId, drawing, currUid)
            val recipientDownloadUrl = addDrawingToStorage(localId, drawing, recipientUid)

            val recipientDrawingData = hashMapOf(
                "id" to localId,
                "url" to recipientDownloadUrl,
                "senderId" to currUid,
                "senderEmail" to senderEmail,
                "sentAt" to System.currentTimeMillis()
            )

            val senderDrawingData = hashMapOf(
                "id" to localId,
                "url" to currUserDownloadUrl,
                "sentAt" to System.currentTimeMillis()
            )

            // add new firestore data
            db.collection("users").document(recipientUid)
                .collection("received").document("$localId.png")
                .set(recipientDrawingData).await()

            db.collection("users").document(currUid)
                .collection("shared").document("$localId.png")
                .set(senderDrawingData).await()
        } catch (e: Exception) {
            println(e.toString())
        }
    }


    /**
     * Get all images of a certain storage type based on the selection param
     *  - ie. 'saved', 'shared', or 'received'
     */
    suspend fun getImagesFromStorage(selectionGroup: String): List<Bitmap>  {

        if (selectionGroup != "saved" && selectionGroup != "shared" && selectionGroup != "received") return emptyList()
        if (Firebase.auth.currentUser == null) return emptyList()

        val bitmaps = mutableListOf<Bitmap>()

        try {
            val uid = getUserId()
            val snapshot = db.collection("users").document(uid).collection(selectionGroup).get().await()
            val imgIds = snapshot.documents.map { it.id }
            val maxBytes = 2L * 1024 * 1024
            for (id in imgIds) {
                val storageRef = storage.reference.child("images/$uid/$id")
                val bytes = storageRef.getBytes(maxBytes).await()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return emptyList()
                bmp.asImageBitmap()
                bitmaps.add(bmp)
            }

        } catch (e: Exception) {
            return emptyList()
        }

        return bitmaps
    }

    /**
     * Queries the firestore DB for all existing user emails and compares it against the parameter for existence.
     * @returns true if the passed email exists in the DB and false otherwise.
     */
    public suspend fun checkForValidEmail(email: String): Boolean {
        val snapshot = db.collection("users").whereEqualTo("email", email).get().await()
        val emails = snapshot.documents.mapNotNull { it.getString("email") }
        return emails.isNotEmpty()
    }

    /**
     * Helper function to add drawing data to Firebase storage for specified UID.
     * @return the download url from storage.
     */
    private suspend fun addDrawingToStorage(localId: Long, drawing: DrawingImage, uid: String): String {
        // 1. Prepare image data (compress bitmap to PNG)
        val bmp = drawing.getBitmap()
        val outputStr = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStr)
        val data = outputStr.toByteArray()

        // 2. Upload to Firebase Storage
        val storageRef = storage.reference.child("images/$uid/$localId.png")
        storageRef.putBytes(data).await()

        // 3. Get the download URL so we can save it to the DB
        return storageRef.downloadUrl.await().toString()
    }
}
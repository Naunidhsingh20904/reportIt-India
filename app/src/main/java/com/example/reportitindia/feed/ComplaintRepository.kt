package com.example.reportitindia.feed

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ComplaintRepository {

    // This is our connection to the Firestore database
    // FirebaseFirestore.getInstance() gives us the database object
    private val db = FirebaseFirestore.getInstance()

    // This function fetches all complaints from Firestore
    // suspend means it runs asynchronously without freezing the app
    suspend fun getComplaints(): Result<List<Complaint>> {
        return try {
            // Step 1: Go to "complaints" collection
            // Step 2: Order by timestamp so newest complaints appear first
            // Step 3: .get() actually fetches the data
            // Step 4: .await() waits for Firebase to respond without freezing UI
            val snapshot = db.collection("complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            // snapshot.documents is a list of all documents we got back
            // we convert each document into a Complaint object
            val complaints = snapshot.documents.mapNotNull { document ->
                // toObject() converts the JSON document into our Complaint data class
                // the ?: return@mapNotNull skips documents that fail to convert
                document.toObject(Complaint::class.java)?.copy(id = document.id)
            }

            Result.success(complaints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // This function saves a new complaint to Firestore
    suspend fun postComplaint(complaint: Complaint): Result<Unit> {
        return try {
            // .add() creates a new document with auto generated ID
            db.collection("complaints")
                .add(complaint)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
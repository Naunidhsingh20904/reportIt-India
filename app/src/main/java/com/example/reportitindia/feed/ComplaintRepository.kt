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
            val snapshot = db.collection("complaints")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            android.util.Log.d("Repository", "Documents found: ${snapshot.documents.size}")

            val complaints = snapshot.documents.mapNotNull { document ->
                android.util.Log.d("Repository", "Document data: ${document.data}")
                document.toObject(Complaint::class.java)?.copy(id = document.id)
            }

            android.util.Log.d("Repository", "Complaints parsed: ${complaints.size}")
            Result.success(complaints)
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Error: ${e.message}")
            Result.failure(e)
        }
    }
    // This function saves a new complaint to Firestore
    suspend fun postComplaint(complaint: Complaint): Result<Unit> {
        return try {
            android.util.Log.d("Repository", "Saving complaint: ${complaint.title}")
            db.collection("complaints")
                .add(complaint)
                .await()
            android.util.Log.d("Repository", "Complaint saved successfully!")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Save error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getComplaintById(id: String): Result<Complaint> {
        return try {
            val document = db.collection("complaints")
                .document(id)
                .get()
                .await()
            val complaint = document.toObject(Complaint::class.java)?.copy(id = document.id)
                ?: return Result.failure(Exception("Complaint not found"))
            Result.success(complaint)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upvoteComplaint(id: String, userId: String): Result<Unit> {
        return try {
            val ref = db.collection("complaints").document(id)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                val currentVotes = snapshot.getLong("votes") ?: 0
                val upvotedBy = snapshot.get("upvotedBy") as? List<String> ?: emptyList()

                // Only upvote if user hasn't already
                if (!upvotedBy.contains(userId)) {
                    transaction.update(ref, "votes", currentVotes + 1)
                    transaction.update(ref, "upvotedBy", upvotedBy + userId)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downvoteComplaint(id: String, userId: String): Result<Unit> {
        return try {
            val ref = db.collection("complaints").document(id)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                val currentVotes = snapshot.getLong("votes") ?: 0
                val upvotedBy = snapshot.get("upvotedBy") as? List<String> ?: emptyList()

                // Only downvote if user has already upvoted
                if (upvotedBy.contains(userId)) {
                    transaction.update(ref, "votes", maxOf(0, currentVotes - 1))
                    transaction.update(ref, "upvotedBy", upvotedBy - userId)
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}

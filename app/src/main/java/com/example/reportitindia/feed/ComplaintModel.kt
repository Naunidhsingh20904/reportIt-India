package com.example.reportitindia.feed

import com.google.firebase.firestore.PropertyName

data class Complaint(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "",
    val votes: Int = 0,
    val authorName: String = "",
    @get:PropertyName("isAnonymous")
    @set:PropertyName("isAnonymous")
    var isAnonymous: Boolean = false,
    val status: String = "",
    val timestamp: Long = 0L,
    val upvotedBy: List<String> = emptyList()
)
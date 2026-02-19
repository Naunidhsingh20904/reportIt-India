package com.example.reportitindia.feed

// This is the blueprint for a single complaint
// Every complaint in the app will have exactly these fields
data class Complaint(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "",
    val votes: Int = 0,
    val authorName: String = "",
    val timestamp: Long = 0L
)
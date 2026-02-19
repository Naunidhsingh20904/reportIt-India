package com.example.reportitindia.feed

data class Complaint(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: String = "",
    val votes: Int = 0,
    val authorName: String = "",
    val isAnonymous: Boolean = false,
    val status: String = "",
    val timestamp: Long = 0L
)
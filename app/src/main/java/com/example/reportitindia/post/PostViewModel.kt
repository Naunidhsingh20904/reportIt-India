package com.example.reportitindia.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportitindia.feed.Complaint
import com.example.reportitindia.feed.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val message: String) : PostState()
}

class PostViewModel : ViewModel() {

    private val repository = ComplaintRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<PostState>(PostState.Idle)
    val state: StateFlow<PostState> = _state

    fun submitComplaint(
        title: String,
        description: String,
        location: String,
        category: String,
        severity: Float,
        isAnonymous: Boolean
    ) {
        viewModelScope.launch {
            _state.value = PostState.Loading

            // Get current logged in user
            val currentUser = auth.currentUser

            val complaint = Complaint(
                title = title,
                description = description,
                location = location,
                category = category,
                votes = 0,
                authorName = if (isAnonymous) "Anonymous" else (currentUser?.displayName ?: "User"),
                isAnonymous = isAnonymous,
                status = "SUBMITTED",
                timestamp = System.currentTimeMillis()
            )

            val result = repository.postComplaint(complaint)
            _state.value = if (result.isSuccess) {
                PostState.Success
            } else {
                PostState.Error("Failed to submit. Try again.")
            }
        }
    }
}
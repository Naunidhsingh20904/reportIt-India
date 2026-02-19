package com.example.reportitindia.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.reportitindia.feed.Complaint
import com.example.reportitindia.feed.ComplaintRepository


class FeedViewModel : ViewModel() {

    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val complaints: List<Complaint>) : FeedState()
        data class Error(val message: String) : FeedState()
    }

    private val _state = MutableStateFlow<FeedState>(FeedState.Loading)
    val state: StateFlow<FeedState> = _state

    // Repository is our connection to Firebase
    private val repository = ComplaintRepository()

    init {
        loadComplaints()
    }

    fun loadComplaints() {
        viewModelScope.launch {
            _state.value = FeedState.Loading
            val result = repository.getComplaints()
            _state.value = if (result.isSuccess) {
                FeedState.Success(result.getOrNull() ?: emptyList())
            } else {
                FeedState.Error("Failed to load complaints. Check your connection.")
            }
        }
    }
}
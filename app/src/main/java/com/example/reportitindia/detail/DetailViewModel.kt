package com.example.reportitindia.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportitindia.feed.Complaint
import com.example.reportitindia.feed.ComplaintRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DetailState {
    object Loading : DetailState()
    data class Success(val complaint: Complaint) : DetailState()
    data class Error(val message: String) : DetailState()
}

class DetailViewModel : ViewModel() {

    private val repository = ComplaintRepository()

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state

    fun loadComplaint(id: String) {
        viewModelScope.launch {
            _state.value = DetailState.Loading
            val result = repository.getComplaintById(id)
            _state.value = if (result.isSuccess) {
                DetailState.Success(result.getOrNull()!!)
            } else {
                DetailState.Error("Failed to load complaint")
            }
        }
    }

    fun upvoteComplaint(id: String) {
        viewModelScope.launch {
            repository.upvoteComplaint(id)
            // Reload complaint to show updated count
            loadComplaint(id)
        }
    }

    fun downvoteComplaint(id: String) {
        viewModelScope.launch {
            repository.downvoteComplaint(id)
            // Reload complaint to show updated count
            loadComplaint(id)
        }
    }
}
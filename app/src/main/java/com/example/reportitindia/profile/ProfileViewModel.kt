package com.example.reportitindia.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reportitindia.feed.Complaint
import com.example.reportitindia.feed.ComplaintRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileData(
    val userName: String = "",
    val userEmail: String = "",
    val complaintsPosted: Int = 0,
    val myComplaints: List<Complaint> = emptyList()
)

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val data: ProfileData) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val repository = ComplaintRepository()

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val state: StateFlow<ProfileState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            val currentUser = auth.currentUser

            if (currentUser == null) {
                _state.value = ProfileState.Error("Not logged in")
                return@launch
            }

            val userName = when {
                !currentUser.displayName.isNullOrEmpty() -> currentUser.displayName!!
                !currentUser.email.isNullOrEmpty() -> currentUser.email!!.substringBefore("@")
                else -> "User"
            }

            val userEmail = currentUser.email ?: ""

            // Fetch real complaints by this user
            val result = repository.getComplaints()
            val allComplaints = result.getOrNull() ?: emptyList()

            // Filter complaints by this user's name
            val myComplaints = allComplaints.filter {
                it.authorName == userName
            }

            _state.value = ProfileState.Success(
                ProfileData(
                    userName = userName,
                    userEmail = userEmail,
                    complaintsPosted = myComplaints.size,
                    myComplaints = myComplaints
                )
            )
        }
    }
}
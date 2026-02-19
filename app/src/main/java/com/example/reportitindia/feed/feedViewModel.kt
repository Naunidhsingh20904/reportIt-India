package com.example.reportitindia.feed


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FeedViewModel : ViewModel() {

    sealed class FeedState {
        object Loading : FeedState()
        data class Success(val complaints: List<Complaint>) : FeedState()
        data class Error(val message: String) : FeedState()
    }

    private val _state = MutableStateFlow<FeedState>(FeedState.Loading)
    val state: StateFlow<FeedState> = _state

    init {
        loadFakeFeed()
    }

    private fun loadFakeFeed() {
        _state.value = FeedState.Success(
            listOf(
                Complaint(
                    id = "1",
                    title = "Large pothole on MG Road",
                    description = "Dangerous pothole causing accidents near the main junction.",
                    location = "MG Road, Bangalore",
                    category = "Roads",
                    votes = 142,
                    authorName = "Rahul M"
                ),
                Complaint(
                    id = "2",
                    title = "Garbage not collected for 2 weeks",
                    description = "The garbage truck has not come to our street for 2 weeks.",
                    location = "Koramangala, Bangalore",
                    category = "Sanitation",
                    votes = 89,
                    authorName = "Priya S"
                ),
                Complaint(
                    id = "3",
                    title = "Broken streetlight near school",
                    description = "Streetlight outside DAV School has been broken for a month.",
                    location = "Jayanagar, Bangalore",
                    category = "Electricity",
                    votes = 67,
                    authorName = "Amit K"
                )
            )
        )
    }
}
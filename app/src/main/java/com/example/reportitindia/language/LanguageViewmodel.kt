package com.example.reportitindia.language

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI State for Language Dropdown
 */
data class DropdownUiState(
    val isExpanded: Boolean = false,
    val selected: String = "Select Language"
)

/**
 * ViewModel for Language Selection
 */
class DropdownViewModel : ViewModel() {

    // Private mutable state
    private val _state = MutableStateFlow(DropdownUiState())

    // Public immutable state
    val state: StateFlow<DropdownUiState> = _state.asStateFlow()

    // List of Indian languages
    val options = listOf(
        "English",
        "Hindi",
        "Marathi",
        "Tamil",
        "Telugu",
        "Gujarati",
        "Kannada",
        "Malayalam",
        "Odia",
        "Punjabi",
        "Bengali",
        "Urdu",
        "Nepali",
        "Assamese",
        "Bhojpuri"
    )

    /**
     * Toggle dropdown open/close
     */
    fun toggle() {
        _state.value = _state.value.copy(
            isExpanded = !_state.value.isExpanded
        )
    }

    /**
     * Select a language and close dropdown
     */
    fun select(option: String) {
        _state.value = _state.value.copy(
            selected = option,
            isExpanded = false
        )
    }
}
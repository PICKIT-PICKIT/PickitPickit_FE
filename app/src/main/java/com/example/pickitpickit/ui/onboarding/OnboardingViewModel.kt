package com.example.pickitpickit.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingState(
    val nickname: String = "",
    val selectedProfileId: Int? = null,
    val selectedTags: Set<String> = emptySet()
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    fun updateNickname(nickname: String) {
        if (nickname.length <= 10) {
            _uiState.update { it.copy(nickname = nickname) }
        }
    }

    fun generateRandomNickname() {
        val randomNicknames = listOf("신나는 꼬북이", "명랑한 라이언", "행복한 피카츄", "졸린 잠만보", "뛰어난 푸바오")
        _uiState.update { it.copy(nickname = randomNicknames.random()) }
    }

    fun selectProfileImage(imageId: Int) {
        _uiState.update { it.copy(selectedProfileId = imageId) }
    }

    fun toggleTag(tag: String) {
        _uiState.update { currentState ->
            val tags = currentState.selectedTags.toMutableSet()
            if (tags.contains(tag)) {
                tags.remove(tag)
            } else {
                tags.add(tag)
            }
            currentState.copy(selectedTags = tags)
        }
    }
}

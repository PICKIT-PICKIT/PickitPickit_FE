package com.example.pickitpickit.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.core.model.DefaultProfileImageResponse
import com.example.pickitpickit.core.model.InterestTagResponse
import com.example.pickitpickit.core.network.OnboardingRepository
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPageState(
    val nickname: String = "",
    val profileImageUrl: String? = null,
    val profileImageType: String = "DEFAULT",
    val selectedTags: List<InterestTagResponse> = emptyList(),
    val userId: Long = 0,
    val kakaoProfileImageUrl: String? = null,
    val kakaoEmail: String = "",
    
    // Default Images and tags for profile editing
    val defaultProfileImages: List<DefaultProfileImageResponse> = emptyList(),
    val backendDefaultProfileImages: List<DefaultProfileImageResponse> = emptyList(),
    val availableTags: List<InterestTagResponse> = emptyList(),
    
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // UI Local State for Profile Edit Form
    val editNickname: String = "",
    val editProfileImageUrl: String? = null,
    val editProfileImageType: String = "DEFAULT",
    val editSelectedTagIds: Set<Long> = emptySet(),
    val customTagInput: String = ""
)

class MyPageViewModel : ViewModel() {
    private val repository = OnboardingRepository()

    private val _uiState = MutableStateFlow(MyPageState())
    val uiState: StateFlow<MyPageState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 6종 고품질 로컬 Unsplash 기본 이미지 리스트 정의 (OnboardingViewModel과 통일)
            val defaultUnsplashImages = listOf(
                DefaultProfileImageResponse("1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("4", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("5", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("6", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=300&q=80")
            )

            val status = repository.getOnboardingStatus()
            val imagesOption = repository.getProfileImages()
            var tags = repository.getInterestTags()

            if (tags.isEmpty()) {
                tags = listOf(
                    InterestTagResponse(1L, "포켓몬"),
                    InterestTagResponse(2L, "디즈니"),
                    InterestTagResponse(3L, "원피스"),
                    InterestTagResponse(4L, "산리오"),
                    InterestTagResponse(5L, "마블"),
                    InterestTagResponse(6L, "BT21"),
                    InterestTagResponse(7L, "짱구"),
                    InterestTagResponse(8L, "팬텀"),
                    InterestTagResponse(9L, "귀멸의칼날"),
                    InterestTagResponse(10L, "나루토"),
                    InterestTagResponse(11L, "카카오"),
                    InterestTagResponse(12L, "지브리"),
                    InterestTagResponse(13L, "메이플"),
                    InterestTagResponse(14L, "스누피"),
                    InterestTagResponse(15L, "드래곤볼")
                )
            }

            if (status != null) {
                val finalImagesOption = com.example.pickitpickit.core.model.ProfileImageOptionsResponse(
                    kakaoProfileImageUrl = imagesOption?.kakaoProfileImageUrl ?: status.kakaoProfileImageUrl,
                    defaultImages = defaultUnsplashImages
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        nickname = status.nickname ?: "",
                        profileImageUrl = status.profileImageUrl ?: (finalImagesOption.defaultImages.firstOrNull()?.imageUrl),
                        profileImageType = status.profileImageType ?: "DEFAULT",
                        selectedTags = status.selectedTags,
                        userId = status.userId,
                        kakaoProfileImageUrl = finalImagesOption.kakaoProfileImageUrl,
                        defaultProfileImages = finalImagesOption.defaultImages,
                        backendDefaultProfileImages = imagesOption?.defaultImages ?: emptyList(),
                        availableTags = tags,
                        isLoading = false
                    )
                }
                resetEditState()
                
                // 카카오 SDK를 이용한 실제 사용자 이메일 정보 동적 조회
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        Log.e("KAKAO_ME", "카카오 사용자 정보 조회 실패 ❌", error)
                    } else if (user != null) {
                        Log.i("KAKAO_ME", "카카오 사용자 정보 조회 성공 🏆 | email = ${user.kakaoAccount?.email}")
                    }
                    val email = if (error == null && user != null) {
                        user.kakaoAccount?.email ?: ""
                    } else {
                        ""
                    }
                    _uiState.update { it.copy(kakaoEmail = email) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "유저 정보를 불러올 수 없습니다.") }
            }
        }
    }

    fun resetEditState() {
        _uiState.update { currentState ->
            currentState.copy(
                editNickname = currentState.nickname,
                editProfileImageUrl = currentState.profileImageUrl,
                editProfileImageType = currentState.profileImageType,
                editSelectedTagIds = currentState.selectedTags.map { it.id }.toSet(),
                customTagInput = ""
            )
        }
    }

    fun updateEditNickname(name: String) {
        if (name.length <= 10) {
            _uiState.update { it.copy(editNickname = name) }
        }
    }

    fun selectEditProfileImage(type: String, url: String) {
        _uiState.update { it.copy(editProfileImageUrl = url, editProfileImageType = type) }
    }

    fun toggleEditTag(tagId: Long) {
        _uiState.update { currentState ->
            val updated = currentState.editSelectedTagIds.toMutableSet()
            if (updated.contains(tagId)) {
                updated.remove(tagId)
            } else {
                updated.add(tagId)
            }
            currentState.copy(editSelectedTagIds = updated)
        }
    }

    fun addCustomTag(tagName: String) {
        val trimmed = tagName.trim()
        if (trimmed.isEmpty() || trimmed.length > 10) return
        
        // Find if tag already exists in availableTags, if so toggle it. Otherwise add dynamically.
        val existingTag = _uiState.value.availableTags.find { it.name == trimmed }
        if (existingTag != null) {
            _uiState.update { currentState ->
                val updated = currentState.editSelectedTagIds.toMutableSet()
                updated.add(existingTag.id)
                currentState.copy(editSelectedTagIds = updated, customTagInput = "")
            }
        } else {
            // Dynamically assign an ID
            val newId = (_uiState.value.availableTags.map { it.id }.maxOrNull() ?: 0L) + 100L
            val newTag = InterestTagResponse(newId, trimmed)
            _uiState.update { currentState ->
                val updatedTags = currentState.availableTags + newTag
                val updatedSelected = currentState.editSelectedTagIds + newId
                currentState.copy(
                    availableTags = updatedTags,
                    editSelectedTagIds = updatedSelected,
                    customTagInput = ""
                )
            }
        }
    }

    fun removeSelectedTag(tagId: Long) {
        _uiState.update { currentState ->
            val updated = currentState.editSelectedTagIds.toMutableSet()
            updated.remove(tagId)
            currentState.copy(editSelectedTagIds = updated)
        }
    }

    fun updateCustomTagInput(input: String) {
        if (input.length <= 10) {
            _uiState.update { it.copy(customTagInput = input) }
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val finalNickname = _uiState.value.editNickname.trim()
        if (finalNickname.length < 2) {
            _uiState.update { it.copy(errorMessage = "닉네임은 2자 이상 입력해주세요.") }
            return
        }

        val finalUrl = _uiState.value.editProfileImageUrl
        val type = _uiState.value.editProfileImageType
        if (finalUrl.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "프로필 이미지를 선택해 주세요.") }
            return
        }

        // Map UI image to backend relative path (using index)
        val backendUrl = if (type == "DEFAULT") {
            val unsplashList = _uiState.value.defaultProfileImages
            val backendList = _uiState.value.backendDefaultProfileImages
            val index = unsplashList.indexOfFirst { it.imageUrl == finalUrl }
            if (index in 0 until backendList.size) {
                backendList[index].imageUrl
            } else {
                finalUrl
            }
        } else if (type == "KAKAO") {
            if (finalUrl.startsWith("https://k.kakaocdn.net")) {
                finalUrl.replace("https://k.kakaocdn.net", "http://k.kakaocdn.net")
            } else {
                finalUrl
            }
        } else {
            finalUrl
        }

        val tagIds = _uiState.value.editSelectedTagIds.toList()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 1. Update Nickname
            val nicknameError = repository.updateNickname(finalNickname)
            if (nicknameError != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = nicknameError) }
                return@launch
            }

            // 2. Update Profile Image
            val imageSuccess = repository.updateProfileImage(type, backendUrl)
            if (!imageSuccess) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "프로필 이미지 저장에 실패했습니다.") }
                return@launch
            }

            // 3. Update Tags
            val tagsError = repository.updateInterestTags(tagIds)
            if (tagsError != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = tagsError) }
                return@launch
            }

            // Reload profile data to synchronize
            loadUserProfile()
            onSuccess()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

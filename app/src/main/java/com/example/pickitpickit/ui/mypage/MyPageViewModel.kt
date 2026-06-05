package com.example.pickitpickit.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.DefaultProfileImageResponse
import com.example.pickitpickit.core.model.InterestTagResponse
import com.example.pickitpickit.core.model.MyPageProfileUpdateRequest
import com.example.pickitpickit.core.network.UserRepository
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
    val reviewCount: Int = 0,
    val bragCount: Int = 0,
    
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
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(MyPageState())
    val uiState: StateFlow<MyPageState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val profile = userRepository.getProfile()
            if (profile != null) {
                // Map MyPageInterestTag to InterestTagResponse
                val selectedTags = profile.interestTags.filter { it.selected }.map { InterestTagResponse(it.id, it.name) }
                val availableTags = profile.interestTags.map { InterestTagResponse(it.id, it.name) }
                
                // 6종 고품질 로컬 Unsplash 기본 이미지 리스트 정의 (온보딩 및 기존 설정과 호환)
                val defaultUnsplashImages = listOf(
                    DefaultProfileImageResponse("1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80"),
                    DefaultProfileImageResponse("2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"),
                    DefaultProfileImageResponse("3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80"),
                    DefaultProfileImageResponse("4", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80"),
                    DefaultProfileImageResponse("5", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80"),
                    DefaultProfileImageResponse("6", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=300&q=80")
                )

                // Map MyPageDefaultImage to DefaultProfileImageResponse with fallback
                val defaultImages = defaultUnsplashImages


                val backendDefaultImages = if (!profile.defaultProfileImages.isNullOrEmpty()) {
                    profile.defaultProfileImages.map { DefaultProfileImageResponse(it.id, it.imageUrl) }
                } else {
                    listOf(
                        "/images/default1.png",
                        "/images/default2.png",
                        "/images/default3.png",
                        "/images/default4.png",
                        "/images/default5.png",
                        "/images/default6.png"
                    ).mapIndexed { idx, url -> DefaultProfileImageResponse((idx + 1).toString(), url) }
                }

                val resolvedProfileImageUrl = if (profile.profileImageType == "DEFAULT") {
                    val serverUrlOrId = profile.profileImageUrl
                    val index = backendDefaultImages.indexOfFirst { it.imageUrl == serverUrlOrId || it.id == serverUrlOrId }
                    if (index in 0 until defaultImages.size) {
                        defaultImages[index].imageUrl
                    } else {
                        profile.profileImageUrl ?: (defaultImages.firstOrNull()?.imageUrl)
                    }
                } else {
                    profile.profileImageUrl ?: profile.kakaoProfileImageUrl ?: (defaultImages.firstOrNull()?.imageUrl)
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        nickname = profile.nickname,
                        profileImageUrl = resolvedProfileImageUrl,
                        profileImageType = profile.profileImageType,
                        selectedTags = selectedTags,
                        userId = profile.userId,
                        kakaoProfileImageUrl = profile.kakaoProfileImageUrl,
                        defaultProfileImages = defaultImages,
                        backendDefaultProfileImages = backendDefaultImages,
                        availableTags = availableTags,
                        reviewCount = profile.reviewCount,
                        bragCount = profile.bragCount,
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
            
            val request = MyPageProfileUpdateRequest(
                nickname = finalNickname,
                profileImageType = type,
                profileImageUrl = backendUrl,
                interestTagIds = tagIds
            )
            
            val updateError = userRepository.updateProfile(request)
            if (updateError != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = updateError) }
            } else {
                // Reload profile data to synchronize
                loadUserProfile()
                onSuccess()
            }
        }
    }

    /**
     * 회원 탈퇴 처리 및 로컬 데이터 초기화
     */
    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val deleteError = userRepository.deleteAccount()
            if (deleteError != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = deleteError) }
            } else {
                // 로컬 세션 토큰 및 설정 캐시 전면 초기화
                GlobalApplication.userPreferences.clearTokens()
                GlobalApplication.userPreferences.clearAll()
                _uiState.update { MyPageState() } // 상태 리셋
                onSuccess()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

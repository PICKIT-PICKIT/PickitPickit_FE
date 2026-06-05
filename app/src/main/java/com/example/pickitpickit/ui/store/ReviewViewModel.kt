package com.example.pickitpickit.ui.store

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.ReviewRequest
import com.example.pickitpickit.core.model.StoreReviewListResponse
import com.example.pickitpickit.core.network.ReviewRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReviewViewModel(private val storeId: Long) : ViewModel() {

    private val repository = ReviewRepository()

    private val _reviewState = MutableStateFlow<StoreReviewListResponse?>(null)
    val reviewState: StateFlow<StoreReviewListResponse?> = _reviewState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _submitResult = MutableSharedFlow<String?>()
    val submitResult: SharedFlow<String?> = _submitResult.asSharedFlow()

    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    init {
        loadReviews()
        loadUserId()
    }

    private fun loadUserId() {
        viewModelScope.launch {
            _currentUserId.value = GlobalApplication.userPreferences.getUserId().first()
        }
    }

    fun loadReviews() {
        viewModelScope.launch {
            _isLoading.update { true }
            val response = repository.getStoreReviews(storeId)
            _reviewState.update { response }
            _isLoading.update { false }
        }
    }

    fun submitReview(rating: Double, difficulty: Int, content: String?) {
        viewModelScope.launch {
            _isLoading.update { true }
            val userId = GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val request = ReviewRequest(
                userId = userId,
                storeId = storeId,
                rating = rating,
                difficulty = difficulty,
                content = content
            )
            val errorMsg = repository.postReview(request)
            if (errorMsg == null) {
                _submitResult.emit("CREATE_SUCCESS")
                // 작성 성공 시 목록 다시 로드
                loadReviews()
            } else {
                _submitResult.emit(errorMsg)
            }
            _isLoading.update { false }
        }
    }

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            _isLoading.update { true }
            val userId = GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val errorMsg = repository.deleteReview(reviewId, userId)
            if (errorMsg == null) {
                _submitResult.emit("DELETE_SUCCESS")
                loadReviews()
            } else {
                _submitResult.emit(errorMsg)
            }
            _isLoading.update { false }
        }
    }

    class Factory(private val storeId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReviewViewModel(storeId) as T
        }
    }
}

package com.example.pickitpickit.ui.store

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.ReviewRequest
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.model.StoreReviewListResponse
import com.example.pickitpickit.core.model.BragDto
import com.example.pickitpickit.core.model.BragRequest
import com.example.pickitpickit.core.model.BragPatchRequest
import com.example.pickitpickit.core.network.ReviewRepository
import com.example.pickitpickit.core.network.StoreRepository
import com.example.pickitpickit.core.network.BragRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────
// UI 상태
// ──────────────────────────────────────────────────────────────

sealed class StoreDetailUiState {
    object Loading : StoreDetailUiState()
    data class Success(
        val detail: StoreDetailResponse,
        val reviewData: StoreReviewListResponse?,
        val bragData: List<BragDto>?
    ) : StoreDetailUiState()
    data class Error(val message: String) : StoreDetailUiState()
}

// ──────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────

class StoreDetailViewModel(
    private val storeId: Int,
    private val userLatitude: Double? = null,
    private val userLongitude: Double? = null
) : ViewModel() {

    private val repository = StoreRepository()
    private val reviewRepository = ReviewRepository()
    private val bragRepository = BragRepository()

    private val _uiState = MutableStateFlow<StoreDetailUiState>(StoreDetailUiState.Loading)
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    // 리뷰 작성 결과 흐름 (성공 시 "", 실패 시 구체적인 에러 메시지 전달)
    private val _submitResult = MutableSharedFlow<String?>()
    val submitResult: SharedFlow<String?> = _submitResult.asSharedFlow()

    // 리뷰 작성 중 상태
    private val _isReviewSubmitting = MutableStateFlow(false)
    val isReviewSubmitting: StateFlow<Boolean> = _isReviewSubmitting.asStateFlow()

    // 자랑하기 작성 결과 흐름
    private val _bragSubmitResult = MutableSharedFlow<String?>()
    val bragSubmitResult: SharedFlow<String?> = _bragSubmitResult.asSharedFlow()

    // 자랑하기 작성 중 상태
    private val _isBragSubmitting = MutableStateFlow(false)
    val isBragSubmitting: StateFlow<Boolean> = _isBragSubmitting.asStateFlow()

    // 내 리뷰 식별을 위한 사용자 고유 ID 흐름
    private val _currentUserId = MutableStateFlow<Long?>(null)
    val currentUserId: StateFlow<Long?> = _currentUserId.asStateFlow()

    // 리뷰 가이드 문구 흐름
    private val _writeGuide = MutableStateFlow<com.example.pickitpickit.core.model.ReviewWriteGuideResponse?>(null)
    val writeGuide: StateFlow<com.example.pickitpickit.core.model.ReviewWriteGuideResponse?> = _writeGuide.asStateFlow()

    init {
        loadStoreDetail()
        loadCurrentUserId()
        loadWriteGuide()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            _currentUserId.value = GlobalApplication.userPreferences.getUserId().first()
        }
    }

    private fun loadWriteGuide() {
        viewModelScope.launch {
            val guide = reviewRepository.getReviewWriteGuide()
            _writeGuide.update { guide }
        }
    }


    fun loadStoreDetail() {
        viewModelScope.launch {
            _uiState.update { StoreDetailUiState.Loading }

            Log.i("STORE_DETAIL_VM", "매장 상세 조회 시작: storeId=$storeId, lat=$userLatitude, lng=$userLongitude")

            // 매장 상세 정보와 리뷰 정보 병렬/순차 조회
            val detail = repository.getStoreDetail(
                storeId = storeId.toLong(),
                lat = userLatitude,
                lng = userLongitude
            )

            val reviews = reviewRepository.getStoreReviews(storeId.toLong())
            val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }

            if (detail != null) {
                Log.i("STORE_DETAIL_VM", "매장 상세 및 리뷰/자랑글 조회 성공: ${detail.store.name}, 상품 ${detail.productCount}개, 리뷰 ${reviews?.reviewCount ?: 0}개, 자랑글 ${brags?.size ?: 0}개")
                _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
            } else {
                Log.e("STORE_DETAIL_VM", "매장 상세 조회 실패: storeId=$storeId")
                _uiState.update { StoreDetailUiState.Error("매장 정보를 불러오지 못했어요.\n잠시 후 다시 시도해 주세요.") }
            }
        }
    }

    fun submitReview(rating: Double, difficulty: Int, content: String?) {
        viewModelScope.launch {
            _isReviewSubmitting.update { true }
            val userId = GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val request = ReviewRequest(
                userId = userId,
                storeId = storeId.toLong(),
                rating = rating,
                difficulty = difficulty,
                content = content
            )
            val errorMsg = reviewRepository.postReview(request)
            if (errorMsg == null) {
                _submitResult.emit("CREATE_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _submitResult.emit(errorMsg)
            }
            _isReviewSubmitting.update { false }
        }
    }

    fun editReview(reviewId: Long, rating: Double, difficulty: Int, content: String?) {
        viewModelScope.launch {
            _isReviewSubmitting.update { true }
            val userId = _currentUserId.value ?: GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val request = com.example.pickitpickit.core.model.ReviewPatchRequest(
                userId = userId,
                rating = rating,
                difficulty = difficulty,
                content = content
            )
            val errorMsg = reviewRepository.updateReview(reviewId, request)
            if (errorMsg == null) {
                _submitResult.emit("EDIT_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _submitResult.emit(errorMsg)
            }
            _isReviewSubmitting.update { false }
        }
    }

    fun removeReview(reviewId: Long) {
        viewModelScope.launch {
            _isReviewSubmitting.update { true }
            val userId = _currentUserId.value ?: GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val errorMsg = reviewRepository.deleteReview(reviewId, userId)
            if (errorMsg == null) {
                _submitResult.emit("DELETE_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _submitResult.emit(errorMsg)
            }
            _isReviewSubmitting.update { false }
        }
    }

    fun submitBrag(spentCost: Int, imageUrl: String, content: String) {
        viewModelScope.launch {
            _isBragSubmitting.update { true }
            val userId = GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val request = BragRequest(
                userId = userId,
                storeId = storeId.toLong(),
                spentCost = spentCost,
                imageUrl = imageUrl,
                content = content
            )
            val errorMsg = bragRepository.postBrag(request)
            if (errorMsg == null) {
                _bragSubmitResult.emit("CREATE_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _bragSubmitResult.emit(errorMsg)
            }
            _isBragSubmitting.update { false }
        }
    }

    fun editBrag(bragId: Long, spentCost: Int, imageUrl: String, content: String) {
        viewModelScope.launch {
            _isBragSubmitting.update { true }
            val userId = _currentUserId.value ?: GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val request = BragPatchRequest(
                userId = userId,
                storeId = storeId.toLong(),
                spentCost = spentCost,
                imageUrl = imageUrl,
                content = content
            )
            val errorMsg = bragRepository.updateBrag(bragId, request)
            if (errorMsg == null) {
                _bragSubmitResult.emit("EDIT_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _bragSubmitResult.emit(errorMsg)
            }
            _isBragSubmitting.update { false }
        }
    }

    fun removeBrag(bragId: Long) {
        viewModelScope.launch {
            _isBragSubmitting.update { true }
            val userId = _currentUserId.value ?: GlobalApplication.userPreferences.getUserId().first() ?: 0L
            val errorMsg = bragRepository.deleteBrag(bragId, userId)
            if (errorMsg == null) {
                _bragSubmitResult.emit("DELETE_SUCCESS")
                val currentState = _uiState.value
                val reviews = reviewRepository.getStoreReviews(storeId.toLong())
                val brags = bragRepository.getAllBrags()?.filter { it.storeId == storeId.toLong() }
                if (currentState is StoreDetailUiState.Success) {
                    _uiState.update { currentState.copy(reviewData = reviews, bragData = brags) }
                } else {
                    val detail = repository.getStoreDetail(
                        storeId = storeId.toLong(),
                        lat = userLatitude,
                        lng = userLongitude
                    )
                    if (detail != null) {
                        _uiState.update { StoreDetailUiState.Success(detail, reviews, brags) }
                    }
                }
            } else {
                _bragSubmitResult.emit(errorMsg)
            }
            _isBragSubmitting.update { false }
        }
    }


    // ──────────────────────────────────────────────────────────────
    // ViewModelProvider.Factory (storeId 주입을 위해 필요)
    // ──────────────────────────────────────────────────────────────

    class Factory(
        private val storeId: Int,
        private val userLatitude: Double? = null,
        private val userLongitude: Double? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StoreDetailViewModel(storeId, userLatitude, userLongitude) as T
        }
    }
}

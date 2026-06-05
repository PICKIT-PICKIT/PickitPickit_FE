package com.example.pickitpickit.ui.mypage
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.ReviewDto
import com.example.pickitpickit.core.network.ReviewRepository
import com.example.pickitpickit.core.network.StoreRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReviewsState(
    val allReviews: List<ReviewDto> = emptyList(),
    val paginatedReviews: List<ReviewDto> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalReviewsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyReviewsViewModel : ViewModel() {
    private val reviewRepository = ReviewRepository()
    private val storeRepository = StoreRepository()

    private val _uiState = MutableStateFlow(MyReviewsState())
    val uiState: StateFlow<MyReviewsState> = _uiState.asStateFlow()

    private val itemsPerPage = 5

    init {
        loadUserReviews()
    }

    fun loadUserReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = GlobalApplication.userPreferences.getUserId().first()
            if (userId == null || userId == 0L) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "로그인이 필요합니다.") }
                return@launch
            }

            val reviews = reviewRepository.getUserReviews()
            if (reviews != null) {
                // storeName이 비어있으면 API를 호출하여 동적으로 채워줍니다.
                val uniqueStoreIds = reviews.filter { it.storeName.isNullOrEmpty() }.map { it.storeId }.distinct()
                val storeNamesMap = if (uniqueStoreIds.isNotEmpty()) {
                    try {
                        coroutineScope {
                            uniqueStoreIds.map { storeId ->
                                async {
                                    val detail = storeRepository.getStoreDetail(storeId)
                                    storeId to (detail?.store?.name ?: "매장 정보 없음")
                                }
                            }.awaitAll().toMap()
                        }
                    } catch (e: Exception) {
                        Log.e("MY_REVIEWS_VM", "가게 정보 조회 중 오류 발생", e)
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }

                val finalReviews = reviews.map { review ->
                    if (review.storeName.isNullOrEmpty()) {
                        review.copy(storeName = storeNamesMap[review.storeId] ?: "매장 정보 없음")
                    } else {
                        review
                    }
                }

                val totalCount = finalReviews.size
                val calculatedTotalPages = maxOf(1, java.lang.Math.ceil(totalCount.toDouble() / itemsPerPage).toInt())
                _uiState.update { currentState ->
                    currentState.copy(
                        allReviews = finalReviews,
                        totalReviewsCount = totalCount,
                        totalPages = calculatedTotalPages,
                        currentPage = minOf(currentState.currentPage, calculatedTotalPages),
                        isLoading = false
                    )
                }
                updatePaginatedList()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "리뷰 목록을 불러오지 못했습니다.") }
            }
        }
    }

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = GlobalApplication.userPreferences.getUserId().first()
            if (userId == null || userId == 0L) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "사용자 정보를 확인할 수 없습니다.") }
                return@launch
            }

            val errorMsg = reviewRepository.deleteReview(reviewId, userId)
            if (errorMsg == null) {
                Log.i("MY_REVIEWS_VM", "리뷰 삭제 성공: reviewId=$reviewId")
                loadUserReviews() // Refresh review list
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    fun setCurrentPage(page: Int) {
        val maxPage = _uiState.value.totalPages
        if (page in 1..maxPage) {
            _uiState.update { it.copy(currentPage = page) }
            updatePaginatedList()
        }
    }

    private fun updatePaginatedList() {
        val currentState = _uiState.value
        val startIndex = (currentState.currentPage - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, currentState.allReviews.size)
        val list = if (startIndex < currentState.allReviews.size) {
            currentState.allReviews.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        _uiState.update { it.copy(paginatedReviews = list) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

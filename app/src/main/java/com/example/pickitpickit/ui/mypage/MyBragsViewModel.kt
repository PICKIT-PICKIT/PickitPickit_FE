package com.example.pickitpickit.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.BragDto
import com.example.pickitpickit.core.network.BragRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyBragsState(
    val allBrags: List<BragDto> = emptyList(),
    val paginatedBrags: List<BragDto> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalBragsCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MyBragsViewModel : ViewModel() {
    private val bragRepository = BragRepository()

    private val _uiState = MutableStateFlow(MyBragsState())
    val uiState: StateFlow<MyBragsState> = _uiState.asStateFlow()

    private val itemsPerPage = 5

    init {
        loadUserBrags()
    }

    fun loadUserBrags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = GlobalApplication.userPreferences.getUserId().first()
            if (userId == null || userId == 0L) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "로그인이 필요합니다.") }
                return@launch
            }

            val brags = bragRepository.getUserBrags()
            if (brags != null) {
                val totalCount = brags.size
                val calculatedTotalPages = maxOf(1, java.lang.Math.ceil(totalCount.toDouble() / itemsPerPage).toInt())
                _uiState.update { currentState ->
                    currentState.copy(
                        allBrags = brags,
                        totalBragsCount = totalCount,
                        totalPages = calculatedTotalPages,
                        currentPage = minOf(currentState.currentPage, calculatedTotalPages),
                        isLoading = false
                    )
                }
                updatePaginatedList()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "게시물 목록을 불러오지 못했습니다.") }
            }
        }
    }

    fun deleteBrag(bragId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = GlobalApplication.userPreferences.getUserId().first()
            if (userId == null || userId == 0L) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "사용자 정보를 확인할 수 없습니다.") }
                return@launch
            }

            val errorMsg = bragRepository.deleteBrag(bragId, userId)
            if (errorMsg == null) {
                Log.i("MY_BRAGS_VM", "자랑글 삭제 성공: bragId=$bragId")
                loadUserBrags() // Refresh brag list
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
        val endIndex = minOf(startIndex + itemsPerPage, currentState.allBrags.size)
        val list = if (startIndex < currentState.allBrags.size) {
            currentState.allBrags.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        _uiState.update { it.copy(paginatedBrags = list) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

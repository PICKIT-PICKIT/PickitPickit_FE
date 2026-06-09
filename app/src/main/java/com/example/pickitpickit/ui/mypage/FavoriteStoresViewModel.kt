package com.example.pickitpickit.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.model.FavoriteStoreResponse
import com.example.pickitpickit.core.network.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteStoresState(
    val allFavorites: List<FavoriteStoreResponse> = emptyList(),
    val paginatedFavorites: List<FavoriteStoreResponse> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalFavoritesCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FavoriteStoresViewModel(
    private val userLatitude: Double?,
    private val userLongitude: Double?
) : ViewModel() {

    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(FavoriteStoresState())
    val uiState: StateFlow<FavoriteStoresState> = _uiState.asStateFlow()

    private val itemsPerPage = 5

    init {
        loadFavoriteStores()
    }

    fun loadFavoriteStores() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userId = GlobalApplication.userPreferences.getUserId().first()
            if (userId == null || userId == 0L) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "로그인이 필요합니다.") }
                return@launch
            }

            val favorites = userRepository.getFavoriteStores(userLatitude, userLongitude)
            if (favorites != null) {
                val totalCount = favorites.size
                val calculatedTotalPages = maxOf(1, java.lang.Math.ceil(totalCount.toDouble() / itemsPerPage).toInt())
                _uiState.update { currentState ->
                    currentState.copy(
                        allFavorites = favorites,
                        totalFavoritesCount = totalCount,
                        totalPages = calculatedTotalPages,
                        currentPage = minOf(currentState.currentPage, calculatedTotalPages),
                        isLoading = false
                    )
                }
                updatePaginatedList()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "관심 매장 목록을 불러오지 못했습니다.") }
            }
        }
    }

    fun deleteFavoriteStore(storeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val errorMsg = userRepository.deleteFavoriteStore(storeId)
            if (errorMsg == null) {
                Log.i("FAVORITE_STORES_VM", "관심 매장 해제 성공: storeId=$storeId")
                loadFavoriteStores()
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
        val endIndex = minOf(startIndex + itemsPerPage, currentState.allFavorites.size)
        val list = if (startIndex < currentState.allFavorites.size) {
            currentState.allFavorites.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        _uiState.update { it.copy(paginatedFavorites = list) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val userLatitude: Double?,
        private val userLongitude: Double?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FavoriteStoresViewModel(userLatitude, userLongitude) as T
        }
    }
}

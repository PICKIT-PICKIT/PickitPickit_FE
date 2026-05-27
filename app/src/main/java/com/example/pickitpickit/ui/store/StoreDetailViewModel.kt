package com.example.pickitpickit.ui.store

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.network.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────
// UI 상태
// ──────────────────────────────────────────────────────────────

sealed class StoreDetailUiState {
    object Loading : StoreDetailUiState()
    data class Success(val detail: StoreDetailResponse) : StoreDetailUiState()
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

    private val _uiState = MutableStateFlow<StoreDetailUiState>(StoreDetailUiState.Loading)
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    init {
        loadStoreDetail()
    }

    fun loadStoreDetail() {
        viewModelScope.launch {
            _uiState.update { StoreDetailUiState.Loading }

            Log.i("STORE_DETAIL_VM", "매장 상세 조회 시작: storeId=$storeId, lat=$userLatitude, lng=$userLongitude")

            val detail = repository.getStoreDetail(
                storeId = storeId.toLong(),
                lat = userLatitude,
                lng = userLongitude
            )

            if (detail != null) {
                Log.i("STORE_DETAIL_VM", "매장 상세 조회 성공: ${detail.store.name}, 상품 ${detail.productCount}개")
                _uiState.update { StoreDetailUiState.Success(detail) }
            } else {
                Log.e("STORE_DETAIL_VM", "매장 상세 조회 실패: storeId=$storeId")
                _uiState.update { StoreDetailUiState.Error("매장 정보를 불러오지 못했어요.\n잠시 후 다시 시도해 주세요.") }
            }
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

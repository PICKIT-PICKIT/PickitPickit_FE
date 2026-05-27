package com.example.pickitpickit.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.GlobalApplication
import com.example.pickitpickit.core.network.SearchRepository
import com.example.pickitpickit.ui.home.StoreItem
import com.example.pickitpickit.ui.home.dummyStores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val searchRepository = SearchRepository()
    private val storeRepository = com.example.pickitpickit.core.network.StoreRepository()
    private val userPreferences = GlobalApplication.userPreferences

    // 사용자 현재 위치 캐싱 (검색 시 정렬 옵션 결합을 위해 보관)
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    // 카테고리 필터 상태
    private val _selectedCategory = MutableStateFlow(MapCategory.ALL)
    val selectedCategory: StateFlow<MapCategory> = _selectedCategory.asStateFlow()

    // 검색어 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 내 주변 추천 Bottom Sheet 표시 여부
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible: StateFlow<Boolean> = _isBottomSheetVisible.asStateFlow()

    // 전체 매장 리스트 (초기값은 dummy이나 API 연동 후 실시간 반영)
    private val _nearbyStores = MutableStateFlow<List<StoreItem>>(dummyStores)
    val nearbyStores: StateFlow<List<StoreItem>> = _nearbyStores.asStateFlow()

    // 최근 검색어 상태 (서버 연동)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _registeredTags = MutableStateFlow<List<String>>(listOf("#원피스", "#포켓몬", "#디즈니"))
    val registeredTags: StateFlow<List<String>> = _registeredTags.asStateFlow()

    init {
        // 뷰모델 생성 시 서버에서 최근 검색어 불러오기
        loadRecentSearches()
    }

    /**
     * 현재 위치 기반 주변 매장 로드
     */
    fun loadNearbyStores(latitude: Double, longitude: Double, type: String = "ALL") {
        currentLatitude = latitude
        currentLongitude = longitude
        viewModelScope.launch {
            // DataStore의 검색 반경 설정을 불러옴
            val radius = userPreferences.searchRadius.firstOrNull() ?: 1000
            Log.i("MAP_VIEWMODEL", "loadNearbyStores API 호출 요청: lat=$latitude, lng=$longitude, radius=$radius, type=$type")
            
            val stores = storeRepository.getNearbyStores(
                lat = latitude,
                lng = longitude,
                radius = radius,
                type = type
            )
            _nearbyStores.value = stores
        }
    }

    /**
     * 서버에서 최근 검색어를 가져옴
     */
    fun loadRecentSearches() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId().firstOrNull()
            if (userId != null) {
                val logs = searchRepository.getRecentSearchLogs(userId)
                _recentSearches.value = logs.map { it.keyword }
            } else {
                Log.w("MAP_VIEWMODEL", "loadRecentSearches: 로그인한 userId가 없음")
            }
        }
    }

    fun setCategory(category: MapCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // 검색어가 있으면 자동으로 결과 시트 표시
        _isBottomSheetVisible.value = query.isNotEmpty()
        
        if (query.isNotBlank()) {
            saveSearchLog(query)
            searchStores(query)
        }
    }

    /**
     * 매장 검색 실행
     */
    fun searchStores(keyword: String) {
        viewModelScope.launch {
            Log.i("MAP_VIEWMODEL", "searchStores API 호출 요청: keyword=$keyword, lat=$currentLatitude, lng=$currentLongitude")
            val stores = storeRepository.searchStores(
                keyword = keyword,
                type = "ALL",
                lat = currentLatitude,
                lng = currentLongitude
            )
            _nearbyStores.value = stores
        }
    }

    /**
     * 검색어 입력 시 서버에 검색 로그 저장
     */
    private fun saveSearchLog(query: String) {
        viewModelScope.launch {
            val userId = userPreferences.getUserId().firstOrNull()
            if (userId != null) {
                val success = searchRepository.saveSearchLog(userId, query)
                if (success) {
                    // 저장 성공 시 최근 검색어 새로고침
                    loadRecentSearches()
                }
            }
        }
    }

    /**
     * 최근 검색어 개별 삭제
     */
    fun deleteRecentSearch(keyword: String) {
        viewModelScope.launch {
            val userId = userPreferences.getUserId().firstOrNull()
            if (userId != null) {
                val success = searchRepository.deleteSearchLog(userId, keyword)
                if (success) {
                    // 삭제 성공 시 리스트 갱신
                    loadRecentSearches()
                }
            }
        }
    }

    /**
     * 최근 검색어 전체 삭제
     */
    fun clearAllRecentSearches() {
        viewModelScope.launch {
            val userId = userPreferences.getUserId().firstOrNull()
            if (userId != null) {
                val success = searchRepository.deleteAllSearchLogs(userId)
                if (success) {
                    // 전체 삭제 성공 시 빈 리스트로 갱신
                    _recentSearches.value = emptyList()
                }
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun showBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    fun hideBottomSheet() {
        _isBottomSheetVisible.value = false
    }

    // 카테고리 + 검색어를 함께 적용한 필터링 결과
    fun getFilteredStores(): List<StoreItem> {
        val query = _searchQuery.value.trim()
        val category = _selectedCategory.value

        return _nearbyStores.value.filter { store ->
            val matchCategory = category == MapCategory.ALL || store.category == category
            val matchQuery = query.isEmpty() ||
                    store.name.contains(query, ignoreCase = true) ||
                    store.address.contains(query, ignoreCase = true) ||
                    store.tags.any { it.contains(query, ignoreCase = true) }
            matchCategory && matchQuery
        }
    }
}

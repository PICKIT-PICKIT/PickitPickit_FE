package com.example.pickitpickit.ui.map

import androidx.lifecycle.ViewModel
import com.example.pickitpickit.ui.home.StoreItem
import com.example.pickitpickit.ui.home.dummyStores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class MapViewModel : ViewModel() {

    // 카테고리 필터 상태
    private val _selectedCategory = MutableStateFlow(MapCategory.ALL)
    val selectedCategory: StateFlow<MapCategory> = _selectedCategory.asStateFlow()

    // 검색어 상태
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 내 주변 추천 Bottom Sheet 표시 여부
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible: StateFlow<Boolean> = _isBottomSheetVisible.asStateFlow()

    // 전체 매장 리스트 (추후 API로 교체)
    private val _nearbyStores = MutableStateFlow<List<StoreItem>>(dummyStores)
    val nearbyStores: StateFlow<List<StoreItem>> = _nearbyStores.asStateFlow()

    // 추후 API 연동 시 사용할 최근 검색어 / 등록 태그 상태 (현재는 더미 데이터)
    private val _recentSearches = MutableStateFlow<List<String>>(listOf("포켓몬", "피카츄", "패트와 매트"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _registeredTags = MutableStateFlow<List<String>>(listOf("#원피스", "#포켓몬", "#디즈니"))
    val registeredTags: StateFlow<List<String>> = _registeredTags.asStateFlow()

    fun setCategory(category: MapCategory) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        // 검색어가 있으면 자동으로 결과 시트 표시
        _isBottomSheetVisible.value = query.isNotEmpty()
        
        // 유효한 검색어일 경우 최근 검색어 목록에 추가
        if (query.isNotBlank()) {
            addRecentSearch(query)
        }
    }

    private fun addRecentSearch(query: String) {
        val currentList = _recentSearches.value.toMutableList()
        currentList.remove(query) // 중복 검색어 제거 후 최상단 배치
        currentList.add(0, query)
        // 최대 10개까지만 유지
        _recentSearches.value = currentList.take(10)
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

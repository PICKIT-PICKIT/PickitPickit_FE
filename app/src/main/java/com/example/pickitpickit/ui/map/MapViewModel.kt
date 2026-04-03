package com.example.pickitpickit.ui.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
    private val _selectedCategory = MutableStateFlow(MapCategory.ALL)
    val selectedCategory: StateFlow<MapCategory> = _selectedCategory.asStateFlow()

    fun setCategory(category: MapCategory) {
        _selectedCategory.value = category
    }
}

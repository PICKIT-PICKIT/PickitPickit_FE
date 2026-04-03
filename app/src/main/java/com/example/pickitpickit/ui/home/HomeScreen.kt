package com.example.pickitpickit.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.pickitpickit.ui.map.MapCategory
import com.example.pickitpickit.ui.map.MapViewModel

@Composable
fun HomeScreen(mapViewModel: MapViewModel) {
    val selectedCategory by mapViewModel.selectedCategory.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "메인 지도 화면")
            Text(text = "현재 필터: ${getCategoryLabel(selectedCategory)}")
        }
    }
}

private fun getCategoryLabel(category: MapCategory): String {
    return when(category) {
        MapCategory.ALL -> "전체"
        MapCategory.CLAW_MACHINE -> "인형뽑기"
        MapCategory.GACHA -> "가챠"
        MapCategory.MIXED -> "복합"
    }
}

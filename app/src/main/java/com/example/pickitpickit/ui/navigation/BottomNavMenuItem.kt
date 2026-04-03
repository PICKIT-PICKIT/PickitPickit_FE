package com.example.pickitpickit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place // Placeholder icon
import androidx.compose.material.icons.filled.Star // Placeholder icon
import androidx.compose.material.icons.filled.ThumbUp // Placeholder icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pickitpickit.ui.map.MapCategory

sealed class BottomNavMenuItem(
    val route: String, 
    val title: String, 
    val icon: ImageVector,
    val mapCategory: MapCategory? = null
) {
    object Home : BottomNavMenuItem("home", "홈", Icons.Default.Home, MapCategory.ALL)
    object ClawMachine : BottomNavMenuItem("home", "인형뽑기", Icons.Default.ThumbUp, MapCategory.CLAW_MACHINE)
    object Gacha : BottomNavMenuItem("home", "가챠", Icons.Default.Star, MapCategory.GACHA)
    object Mixed : BottomNavMenuItem("home", "복합", Icons.Default.Place, MapCategory.MIXED)
    
    // MyPage는 별도의 라우트(Screen)를 가짐
    object MyPage : BottomNavMenuItem("mypage", "마이", Icons.Default.Person)
}

package com.example.pickitpickit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pickitpickit.ui.home.HomeScreen
import com.example.pickitpickit.ui.mypage.MyPageScreen
import com.example.pickitpickit.ui.notifications.NotificationsScreen

@Composable
fun MainNavGraph(
    navController: NavHostController, 
    mapViewModel: com.example.pickitpickit.ui.map.MapViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavMenuItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavMenuItem.Home.route) {
            HomeScreen(mapViewModel = mapViewModel)
        }
        composable(BottomNavMenuItem.MyPage.route) {
            MyPageScreen()
        }
    }
}

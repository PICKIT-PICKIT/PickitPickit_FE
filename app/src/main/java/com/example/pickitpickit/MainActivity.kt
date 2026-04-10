package com.example.pickitpickit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pickitpickit.ui.map.MapViewModel
import androidx.compose.runtime.collectAsState
import com.example.pickitpickit.ui.navigation.BottomNavMenuItem
import com.example.pickitpickit.ui.navigation.MainNavGraph
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pickitpickit.ui.onboarding.OnboardingScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        var isCheckingState by mutableStateOf(true)
        var initialRoute by mutableStateOf("onboarding")
        
        lifecycleScope.launch {
            // 카카오 로그인이 구현되기 전 가짜 상태 점검 딜레이 (1.5초 대기)
            delay(1500)
            
            // TODO: 추후 여기에 카카오 토큰 점검 / 온보딩 완료 여부 로직을 넣어서 분기
            val isUserLoggedInAndOnboarded = false 
            
            initialRoute = if (isUserLoggedInAndOnboarded) {
                "main_app" 
            } else { 
                "onboarding_flow" 
            }
            isCheckingState = false
        }
        
        // 데이터가 전부 로딩될 때까지 스플래시 화면을 유지시킴
        splashScreen.setKeepOnScreenCondition { isCheckingState }

        setContent {
            PickitPickitTheme {
                if (!isCheckingState) {
                    RootApp(startDestination = initialRoute)
                }
            }
        }
    }
}

@Composable
fun RootApp(startDestination: String = "onboarding_flow") {
    val rootNavController = rememberNavController()

    NavHost(
        navController = rootNavController,
        startDestination = startDestination
    ) {
        composable("onboarding_flow") {
            OnboardingScreen(
                onComplete = {
                    rootNavController.navigate("main_app") {
                        popUpTo("onboarding_flow") { inclusive = true }
                    }
                }
            )
        }
        composable("main_app") {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Shared MapViewModel
    val mapViewModel: MapViewModel = viewModel()
    
    // 현재 ViewModel의 카테고리 상태 (탭 하이라이트 등 연동을 위함)
    val currentCategory by mapViewModel.selectedCategory.collectAsState()

    val bottomNavItems = listOf(
        BottomNavMenuItem.Home,
        BottomNavMenuItem.ClawMachine,
        BottomNavMenuItem.Gacha,
        BottomNavMenuItem.Mixed,
        BottomNavMenuItem.MyPage
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    // 선택 여부 판별: 
                    // 마이페이지는 라우트로 판별, 홈/기타탭은 MapCategory로 판별
                    val isSelected = if (item == BottomNavMenuItem.MyPage) {
                        currentRoute == item.route
                    } else {
                        currentRoute == BottomNavMenuItem.Home.route && currentCategory == item.mapCategory
                    }

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            if (item == BottomNavMenuItem.MyPage) {
                                navController.navigate(item.route) {
                                    navController.graph.startDestinationRoute?.let { route ->
                                        popUpTo(route) { saveState = true }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                // 현재 라우트가 Home(Map)이 아니면 Home으로 한 번 이동
                                if (currentRoute != BottomNavMenuItem.Home.route) {
                                    navController.navigate(BottomNavMenuItem.Home.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                // 지도는 유지한 채 카테고리만 변경
                                item.mapCategory?.let { mapViewModel.setCategory(it) }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        MainNavGraph(
            navController = navController,
            mapViewModel = mapViewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainScreenPreview() {
    PickitPickitTheme {
        MainScreen()
    }
}

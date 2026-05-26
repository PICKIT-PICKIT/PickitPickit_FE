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

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.pickitpickit.core.datastore.UserPreferences
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pickitpickit.ui.login.LoginScreen
import com.example.pickitpickit.ui.onboarding.OnboardingScreen
import com.kakao.sdk.user.UserApiClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPreferences = UserPreferences(this)
        var startRoute by mutableStateOf<String?>(null)

        lifecycleScope.launch {
            userPreferences.isOnboardingCompleted.collect { completed ->
                if (startRoute == null) {
                    // 카카오 로그인 세션 확인
                    UserApiClient.instance.me { user, error ->
                        if (error != null) {
                            // 로그인 정보가 없거나 에러 발생 시 로그인 화면으로
                            startRoute = "Login"
                        } else {
                            // 로그인 되어 있는 경우 온보딩 완료 여부에 따라 경로 결정
                            startRoute = if (completed) "Main" else "Onboarding"
                        }
                    }
                }
            }
        }

        // startRoute가 결정될 때까지 스플래시 화면을 유지
        splashScreen.setKeepOnScreenCondition { startRoute == null }

        setContent {
            PickitPickitTheme {
                startRoute?.let { route ->
                    val mapViewModel: MapViewModel = viewModel()
                    RootScreen(
                        startRoute = route,
                        mapViewModel = mapViewModel,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
}

@Composable
fun RootScreen(startRoute: String, mapViewModel: MapViewModel, userPreferences: UserPreferences) {
    val rootNavController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    NavHost(navController = rootNavController, startDestination = startRoute) {
        composable("Login") {
            LoginScreen(
                onLoginSuccess = {
                    rootNavController.navigate("Onboarding") {
                        popUpTo("Login") { inclusive = true }
                    }
                }
            )
        }
        composable("Onboarding") {
            OnboardingScreen(
                onComplete = {
                    coroutineScope.launch {
                        userPreferences.setOnboardingCompleted(true)
                    }
                    rootNavController.navigate("Main") {
                        popUpTo("Onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("Main") {
            MainScreen(mapViewModel = mapViewModel)
        }
    }
}

@Composable
fun MainScreen(mapViewModel: MapViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
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
                                if (currentRoute != BottomNavMenuItem.Home.route) {
                                    navController.navigate(BottomNavMenuItem.Home.route) {
                                        navController.graph.startDestinationRoute?.let { route ->
                                            popUpTo(route) { saveState = true }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
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

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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.rememberCoroutineScope
import com.example.pickitpickit.ui.login.LoginScreen
import com.example.pickitpickit.ui.onboarding.OnboardingScreen
import com.example.pickitpickit.ui.admin.StoreAdminScreen
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
            val context = androidx.compose.ui.platform.LocalContext.current
            LoginScreen(
                onLoginSuccess = { onboardingCompleted ->
                    // 🌟 서버의 온보딩 완료 값(onboardingCompleted) 그대로 100% 신뢰하여 라우팅하는 원래 정석 로직으로 완벽 복원!
                    val destination = if (onboardingCompleted) {
                        "Main"
                    } else {
                        "Onboarding"
                    }
                    rootNavController.navigate(destination) {
                        popUpTo("Login") { inclusive = true }
                    }
                },
                userPreferences = userPreferences
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
                },
                onLogout = {
                    coroutineScope.launch {
                        userPreferences.clearTokens()
                        rootNavController.navigate("Login") {
                            popUpTo("Onboarding") { inclusive = true }
                        }
                    }
                },
                onAdminStoreSelected = { storeId, storeName ->
                    // 관리자 매장 선택 완료 → 매장 관리 화면으로 직접 이동
                    val encodedName = java.net.URLEncoder.encode(storeName, "UTF-8")
                    rootNavController.navigate("store_admin/$storeId?storeName=$encodedName") {
                        popUpTo("Onboarding") { inclusive = true }
                    }
                }
            )
        }
        // 관리자 매장 관리 (Root 레뺌에서 직접 진입)
        composable(
            route = "store_admin/{storeId}?storeName={storeName}",
            arguments = listOf(
                navArgument("storeId") { type = NavType.IntType },
                navArgument("storeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getInt("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: ""
            val decodedName = java.net.URLDecoder.decode(storeName, "UTF-8")
            StoreAdminScreen(
                storeId = storeId,
                storeName = decodedName,
                onBackClick = { rootNavController.popBackStack() }
            )
        }
        composable("Main") {
            val context = androidx.compose.ui.platform.LocalContext.current
            val authRepository = androidx.compose.runtime.remember {
                com.example.pickitpickit.core.network.AuthRepository(userPreferences, context)
            }
            MainScreen(
                mapViewModel = mapViewModel,
                onLogoutClick = {
                    coroutineScope.launch {
                        authRepository.logout { success ->
                            if (success) {
                                rootNavController.navigate("Login") {
                                    popUpTo("Main") { inclusive = true }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(mapViewModel: MapViewModel, onLogoutClick: () -> Unit) {
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
        com.example.pickitpickit.ui.navigation.MainNavGraph(
            navController = navController,
            mapViewModel = mapViewModel,
            onLogoutClick = onLogoutClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

object TestConfig {
    var isForceOnboardingTest = false
}

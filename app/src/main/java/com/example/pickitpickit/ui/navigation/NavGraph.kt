package com.example.pickitpickit.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pickitpickit.ui.home.HomeScreen
import com.example.pickitpickit.ui.mypage.MyPageScreen
import com.example.pickitpickit.ui.mypage.MyReviewsScreen
import com.example.pickitpickit.ui.mypage.MyBragsScreen
import com.example.pickitpickit.ui.store.StoreDetailScreen
import com.example.pickitpickit.ui.store.StoreDetailUiState
import com.example.pickitpickit.ui.store.StoreDetailViewModel
import com.example.pickitpickit.ui.store.ReviewScreen
import com.example.pickitpickit.ui.admin.StoreAdminScreen

@Composable
fun MainNavGraph(
    navController: NavHostController,
    mapViewModel: com.example.pickitpickit.ui.map.MapViewModel,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 현재 위치 값 (StoreDetailViewModel Factory에 전달)
    val userLat by mapViewModel.userLatitude.collectAsState()
    val userLng by mapViewModel.userLongitude.collectAsState()

    NavHost(
        navController = navController,
        startDestination = BottomNavMenuItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavMenuItem.Home.route) {
            HomeScreen(
                mapViewModel = mapViewModel,
                onStoreClick = { storeId ->
                    navController.navigate("store_detail/$storeId")
                }
            )
        }
        composable(BottomNavMenuItem.MyPage.route) {
            MyPageScreen(
                onLogoutClick = onLogoutClick,
                onMyReviewsClick = { navController.navigate("my_reviews") },
                onMyBragsClick = { navController.navigate("my_brags") }
            )
        }
        composable("my_reviews") {
            MyReviewsScreen(onBackClick = { navController.popBackStack() })
        }
        composable("my_brags") {
            MyBragsScreen(onBackClick = { navController.popBackStack() })
        }
        composable(
            route = "store_detail/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getInt("storeId") ?: return@composable

            // ViewModel은 백스택 엔트리 스코프로 생성 → 화면 재진입 시 자동 재활용
            val storeDetailViewModel: StoreDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = StoreDetailViewModel.Factory(
                    storeId = storeId,
                    userLatitude = userLat,
                    userLongitude = userLng
                )
            )

            val uiState by storeDetailViewModel.uiState.collectAsState()

            when (val state = uiState) {
                // ── 로딩 중 ────────────────────────────────────────────
                is StoreDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F7FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF3B6EF8))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                "매장 정보를 불러오는 중이에요...",
                                fontSize = 14.sp,
                                color = Color(0xFF888888)
                            )
                        }
                    }
                }

                // ── 에러 ───────────────────────────────────────────────
                is StoreDetailUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF5F7FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("😢", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                fontSize = 15.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { storeDetailViewModel.loadStoreDetail() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B6EF8))
                            ) {
                                Text("다시 시도", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { navController.popBackStack() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE))
                            ) {
                                Text("돌아가기", fontWeight = FontWeight.Bold, color = Color(0xFF555555))
                            }
                        }
                    }
                }

                // ── 성공 ───────────────────────────────────────────────
                is StoreDetailUiState.Success -> {
                    val isReviewSubmitting by storeDetailViewModel.isReviewSubmitting.collectAsState()
                    val isBragSubmitting by storeDetailViewModel.isBragSubmitting.collectAsState()
                    val currentUserId by storeDetailViewModel.currentUserId.collectAsState()
                    val writeGuide by storeDetailViewModel.writeGuide.collectAsState()

                    StoreDetailScreen(
                        storeDetail = state.detail,
                        reviewData = state.reviewData,
                        bragData = state.bragData,
                        onBackClick = { navController.popBackStack() },
                        onSubmitReview = { rating, difficulty, content ->
                            storeDetailViewModel.submitReview(rating, difficulty, content)
                        },
                        isReviewSubmitting = isReviewSubmitting,
                        submitResultFlow = storeDetailViewModel.submitResult,
                        currentUserId = currentUserId,
                        writeGuide = writeGuide,
                        onEditReview = { reviewId, rating, difficulty, content ->
                            storeDetailViewModel.editReview(reviewId, rating, difficulty, content)
                        },
                        onDeleteReview = { reviewId ->
                            storeDetailViewModel.removeReview(reviewId)
                        },
                        isBragSubmitting = isBragSubmitting,
                        bragSubmitResultFlow = storeDetailViewModel.bragSubmitResult,
                        onSubmitBrag = { spentCost, imageUrl, content ->
                            storeDetailViewModel.submitBrag(spentCost, imageUrl, content)
                        },
                        onDeleteBrag = { bragId ->
                            storeDetailViewModel.removeBrag(bragId)
                        },
                        onEditBrag = { bragId, spentCost, imageUrl, content ->
                            storeDetailViewModel.editBrag(bragId, spentCost, imageUrl, content)
                        }
                    )
                }
            }
        }
        composable(
            route = "review/{storeId}?storeName={storeName}",
            arguments = listOf(
                navArgument("storeId") { type = NavType.LongType },
                navArgument("storeName") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getLong("storeId") ?: return@composable
            val storeName = backStackEntry.arguments?.getString("storeName") ?: "매장"

            ReviewScreen(
                storeId = storeId,
                storeName = storeName,
                onBackClick = { navController.popBackStack() }
            )
        }
        // 업도 : 매장 관리 (StoreAdminScreen)
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
            StoreAdminScreen(
                storeId = storeId,
                storeName = storeName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

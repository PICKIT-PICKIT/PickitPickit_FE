package com.example.pickitpickit.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.FavoriteStoreResponse

@Composable
fun FavoriteStoresScreen(
    onBackClick: () -> Unit,
    onStoreClick: (Long) -> Unit,
    viewModel: FavoriteStoresViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var storeToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFavoriteStores()
    }

    // 에러 다이얼로그
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text("확인", color = Color(0xFF5393FA), fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("알림", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.errorMessage ?: "") }
        )
    }

    // 관심 매장 해제 확인 다이얼로그
    if (storeToDelete != null) {
        AlertDialog(
            onDismissRequest = { storeToDelete = null },
            title = { Text("관심 매장 해제", fontWeight = FontWeight.Bold) },
            text = { Text("이 매장을 관심 매장에서 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        storeToDelete?.let { viewModel.deleteFavoriteStore(it) }
                        storeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { storeToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("취소", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── 상단 그라데이션 타이틀 바 ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2ACAE7), Color(0xFF4D6FDF))
                        )
                    )
                    .statusBarsPadding()
                    .padding(vertical = 16.dp)
            ) {
                // 뒤로 가기 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onBackClick)
                        .padding(start = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로 가기",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "뒤로 가기",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 타이틀 & 서브타이틀
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "내가 저장한 관심 매장",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "총 ${uiState.totalFavoritesCount}개의 매장",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── 매장 목록 영역 ────────────────────────────────────
            if (uiState.paginatedFavorites.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❤️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "저장된 관심 매장이 없습니다.",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.paginatedFavorites) { favorite ->
                        FavoriteStoreCard(
                            favorite = favorite,
                            onStoreClick = onStoreClick,
                            onDeleteClick = { storeToDelete = favorite.store.id }
                        )
                    }
                }
            }

            // ── 하단 페이지네이션 영역 ──────────────────────────────
            if (uiState.totalPages > 1) {
                PaginationBar(
                    currentPage = uiState.currentPage,
                    totalPages = uiState.totalPages,
                    onPageClick = viewModel::setCurrentPage
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // 로딩 바
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5393FA))
            }
        }
    }
}

@Composable
private fun FavoriteStoreCard(
    favorite: FavoriteStoreResponse,
    onStoreClick: (Long) -> Unit,
    onDeleteClick: () -> Unit
) {
    val store = favorite.store
    val categoryLabel = when (store.type.uppercase()) {
        "CLAW" -> "인형뽑기"
        "GACHA" -> "가챠"
        else -> "복합"
    }
    val categoryColor = when (store.type.uppercase()) {
        "CLAW" -> Color(0xFFFF9500)
        "GACHA" -> Color(0xFF34C759)
        else -> Color(0xFF5856D6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .border(1.2.dp, Color(0xFFE3EDFD), RoundedCornerShape(16.dp))
            .clickable { onStoreClick(store.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.TopStart
            ) {
                if (!store.mainImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = store.mainImageUrl,
                        contentDescription = "매장 썸네일",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 카테고리 뱃지
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(categoryColor)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = categoryLabel, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 매장 정보
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = store.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (store.distance > 0) {
                        Text(
                            text = if (store.distance >= 1000) {
                                String.format("%.1fkm", store.distance / 1000.0)
                            } else {
                                "${store.distance}m"
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF3B6EF8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF888888),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = store.address ?: "주소 정보 없음",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!store.businessHours.isNullOrEmpty()) {
                    Text(
                        text = "🕐 ${store.businessHours}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 하트 아이콘 (빨간색 - 클릭 시 해제 다이얼로그)
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "관심 매장 해제",
                    tint = Color.Red,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

package com.example.pickitpickit.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pickitpickit.core.model.ReviewDto

@Composable
fun MyReviewsScreen(
    onBackClick: () -> Unit,
    viewModel: MyReviewsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var reviewToDelete by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUserReviews()
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

    // 리뷰 삭제 확인 다이얼로그
    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text("리뷰 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("작성하신 리뷰를 정말로 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { viewModel.deleteReview(it) }
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { reviewToDelete = null },
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
                        text = "내가 작성한 리뷰",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "총 ${uiState.totalReviewsCount}개의 리뷰",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── 리뷰 목록 영역 ────────────────────────────────────
            if (uiState.paginatedReviews.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "작성하신 리뷰가 없습니다.",
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
                    items(uiState.paginatedReviews) { review ->
                        MyReviewCard(
                            review = review,
                            onDeleteClick = { reviewToDelete = review.reviewId }
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
private fun MyReviewCard(
    review: ReviewDto,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .border(1.2.dp, Color(0xFFE3EDFD), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 상단 헤더: 매장 정보 및 삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF4D6FDF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = review.storeName ?: "매장 정보 없음",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4D6FDF),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_delete),
                        contentDescription = "리뷰 삭제",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 별점 및 시간 표시
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val fullStars = review.rating.toInt()
                    for (i in 1..5) {
                        val isYellow = i <= fullStars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isYellow) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = getRelativeTimeString(review.createdAt),
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 리뷰 한줄평 내용
            Text(
                text = review.content ?: "내용이 없는 리뷰입니다.",
                fontSize = 13.sp,
                color = Color(0xFF333333),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
internal fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable(enabled = currentPage > 1) { onPageClick(currentPage - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 페이지",
                tint = if (currentPage > 1) Color(0xFF111111) else Color(0xFFCCCCCC),
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Page numbers
        for (i in 1..totalPages) {
            val isSelected = i == currentPage
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else Color(0xFFE0E0E0),
                        RoundedCornerShape(8.dp)
                    )
                    .background(if (isSelected) Color(0xFFFFA726) else Color.White)
                    .clickable { onPageClick(i) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = i.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Next button
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                .background(Color.White)
                .clickable(enabled = currentPage < totalPages) { onPageClick(currentPage + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 페이지",
                tint = if (currentPage < totalPages) Color(0xFF111111) else Color(0xFFCCCCCC),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 헬퍼: 상대 시간 포맷팅 유틸리티
// ──────────────────────────────────────────────────────────────
fun getRelativeTimeString(createdAtStr: String): String {
    return try {
        // 날짜 형식 파싱 시도 (Z, T 유무에 대응)
        val cleanedStr = if (createdAtStr.contains(".") && !createdAtStr.endsWith("Z")) {
            createdAtStr.split(".")[0] // 소수점 초 제거
        } else {
            createdAtStr
        }
        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
        val dateTime = java.time.LocalDateTime.parse(cleanedStr.removeSuffix("Z"), formatter)
        
        // KST 시간 기준 보정 (서버가 UTC인 경우 필요하나 기본 Local비교로 동작)
        val now = java.time.LocalDateTime.now()
        val duration = java.time.Duration.between(dateTime, now)
        val seconds = duration.seconds
        
        when {
            seconds < 0 -> "방금 전" // 시간차가 미래인 경우 보정
            seconds < 60 -> "방금 전"
            seconds < 3600 -> "${seconds / 60}분 전"
            seconds < 86400 -> "${seconds / 3600}시간 전"
            seconds < 2592000 -> "${seconds / 86400}일 전"
            else -> createdAtStr.split("T").firstOrNull() ?: createdAtStr
        }
    } catch (e: Exception) {
        try {
            // T 분리 실패 시 단순 앞부분 추출
            createdAtStr.split("T").firstOrNull() ?: createdAtStr
        } catch (ex: Exception) {
            createdAtStr
        }
    }
}

package com.example.pickitpickit.ui.store

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.ReviewDto
import com.example.pickitpickit.core.model.StoreReviewListResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    storeId: Long,
    storeName: String,
    onBackClick: () -> Unit
) {
    val viewModel: ReviewViewModel = viewModel(
        factory = ReviewViewModel.Factory(storeId)
    )

    val reviewState by viewModel.reviewState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showWriteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 리뷰 작성 결과에 따른 다이얼로그 처리 및 토스트 메시지
    LaunchedEffect(Unit) {
        viewModel.submitResult.collect { result ->
            result?.let { msg ->
                if (msg == "CREATE_SUCCESS" || msg.isEmpty()) {
                    Toast.makeText(context, "리뷰가 성공적으로 등록되었습니다!", Toast.LENGTH_SHORT).show()
                    showWriteDialog = false
                } else {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "리뷰",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로 가기",
                            tint = Color(0xFF1A1A2E)
                        )
                    }
                },
                actions = {
                    // 우측 리뷰 작성 버튼
                    Button(
                        onClick = { showWriteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300) // 프리미엄 옐로우/오렌지
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "⭐ 리뷰 작성",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading && reviewState == null) {
                // 첫 로딩 중
                CircularProgressIndicator(
                    color = Color(0xFF3B6EF8),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val data = reviewState
                if (data == null || data.reviews.isEmpty()) {
                    // 빈 상태 UI
                    ReviewEmptyState(
                        storeName = storeName,
                        onWriteClick = { showWriteDialog = true }
                    )
                } else {
                    // 리뷰 목록
                    ReviewListContent(
                        data = data,
                        onWriteClick = { showWriteDialog = true }
                    )
                }
            }
        }
    }

    // 리뷰 작성 모달 다이얼로그
    if (showWriteDialog) {
        ReviewWriteDialog(
            storeName = storeName,
            onDismiss = { showWriteDialog = false },
            onSubmit = { rating, difficulty, content ->
                viewModel.submitReview(rating, difficulty, content)
            },
            isSubmitting = isLoading
        )
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰가 없을 때 빈 상태 UI
// ──────────────────────────────────────────────────────────────
@Composable
private fun ReviewEmptyState(
    storeName: String,
    onWriteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 말풍선 아이콘 컨테이너
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEF2FF)), // 부드러운 보라/블루 톤
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💬",
                        fontSize = 42.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "아직 리뷰가 없어요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "첫 번째 리뷰를 남겨보세요!\n${storeName}에서의 경험을 공유해 주세요.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onWriteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "⭐ 첫 리뷰 작성하기",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰가 존재할 때의 메인 목록 UI
// ──────────────────────────────────────────────────────────────
@Composable
private fun ReviewListContent(
    data: StoreReviewListResponse,
    onWriteClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── 1. 통계 헤더 (별점 요약 + 난이도 요약) ────────────────
        item {
            ReviewSummaryHeader(data = data)
        }

        // ── 2. 리뷰 수 타이틀 ────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "방문자 리뷰",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E7FF))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${data.reviewCount}개",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B6EF8)
                    )
                }
            }
        }

        // ── 3. 리뷰 리스트 ───────────────────────────────────────
        items(data.reviews) { review ->
            ReviewItemCard(review = review)
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰 요약 헤더 (별점 및 난이도 그래프 등)
// ──────────────────────────────────────────────────────────────
@Composable
private fun ReviewSummaryHeader(data: StoreReviewListResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 별점 요약
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "평균 별점",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFCA28),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", data.averageRating),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A2E)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "총 ${data.reviewCount}개의 평가",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            // 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(Color(0xFFE5E7EB))
            )

            // 난이도 요약
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "체감 평균 난이도",
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                val diffLabel = when {
                    data.averageDifficulty <= 1.5 -> "쉬움 😊"
                    data.averageDifficulty <= 2.5 -> "보통 쉬움 🙂"
                    data.averageDifficulty <= 3.5 -> "보통 😐"
                    data.averageDifficulty <= 4.5 -> "어려움 😅"
                    else -> "극악 😱"
                }
                Text(
                    text = diffLabel,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B6EF8)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = String.format("난이도 수치: %.1f", data.averageDifficulty),
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰 개별 카드 UI
// ──────────────────────────────────────────────────────────────
@Composable
private fun ReviewItemCard(review: ReviewDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 작성자 정보 및 별점
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 이미지
                if (!review.authorProfileImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = review.authorProfileImageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 기본 아이콘
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 닉네임 & 날짜
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.authorNickname,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = review.createdAt.split("T").firstOrNull() ?: review.createdAt,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                // 별점 표시
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFCA28),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = String.format("%.1f", review.rating),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 난이도 태그
            review.difficultyLabel?.let { label ->
                val tagColor = when (review.difficulty) {
                    in 1..2 -> Color(0xFF10B981) // 쉬움 (초록)
                    3 -> Color(0xFF3B82F6)      // 보통 (파랑)
                    else -> Color(0xFFEF4444)    // 어려움 (빨강)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "난이도: $label",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tagColor
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 리뷰 내용
            if (!review.content.isNullOrEmpty()) {
                Text(
                    text = review.content,
                    fontSize = 13.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            }

            // 첨부 이미지 (존재할 경우)
            if (!review.imageUrl.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = review.imageUrl,
                    contentDescription = "리뷰 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰 작성 다이얼로그 모달
// ──────────────────────────────────────────────────────────────
@Composable
fun ReviewWriteDialog(
    storeName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Double, difficulty: Int, content: String?) -> Unit,
    isSubmitting: Boolean
) {
    var rating by remember { mutableDoubleStateOf(5.0) }
    var difficulty by remember { mutableIntStateOf(3) } // 보통(3) 기본값
    var content by remember { mutableStateOf("") }

    val contentCharLimit = 200

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // X 닫기 버튼
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("✕", fontSize = 16.sp, color = Color(0xFF9CA3AF))
                    }
                }

                // 타이틀
                Text(
                    text = "⭐ 매장 리뷰 작성",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 서브타이틀
                Text(
                    text = buildString {
                        append(storeName)
                        append("을(를) 방문하셨나요?\n경험을 공유해 주세요!")
                    },
                    fontSize = 13.sp,
                    color = Color(0xFF3B6EF8), // 파란색 강조색
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 1. 별점 선택
                Text(
                    text = "매장은 어떠셨나요?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        val starRating = i.toDouble()
                        val isSelected = starRating <= rating
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "별점 $i",
                            tint = if (isSelected) Color(0xFFFFCA28) else Color(0xFFE5E7EB),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable(enabled = !isSubmitting) {
                                    rating = starRating
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. 난이도 선택 (추가된 프리미엄 기능)
                Text(
                    text = "매장 인형뽑기/가챠 체감 난이도는?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val difficulties = listOf(
                        Triple(1, "쉬움 😊", Color(0xFF10B981)),
                        Triple(3, "보통 😐", Color(0xFF3B82F6)),
                        Triple(5, "어려움 😅", Color(0xFFEF4444))
                    )

                    difficulties.forEach { (value, label, color) ->
                        val isSelected = difficulty == value
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF3F4F6)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) color else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !isSubmitting) {
                                    difficulty = value
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) color else Color(0xFF4B5563)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. 한 줄 평가 입력
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "한 줄 평가 (선택사항)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = content,
                    onValueChange = {
                        if (it.length <= contentCharLimit) {
                            content = it
                        }
                    },
                    placeholder = {
                        Text(
                            text = "다른 사용자들에게 도움이 될 내용을 남겨주세요!",
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1F2937)),
                    maxLines = 4,
                    enabled = !isSubmitting
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 글자 수 표시
                Text(
                    text = "${content.length}/$contentCharLimit",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 취소 버튼
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF6B7280)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text(
                            text = "취소",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 리뷰 등록 버튼
                    Button(
                        onClick = {
                            onSubmit(rating, difficulty, content.ifBlank { null })
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300) // 오렌지/옐로우 강조색
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color(0xFF1A1A2E),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✈ ",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A1A2E)
                                )
                                Text(
                                    text = "리뷰 등록",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A2E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

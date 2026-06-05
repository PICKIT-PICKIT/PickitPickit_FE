package com.example.pickitpickit.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.ProductDto
import com.example.pickitpickit.core.model.ReviewDto
import com.example.pickitpickit.core.model.StoreDetailDto
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.model.StoreReviewListResponse
import com.example.pickitpickit.core.model.TagDto
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.activity.compose.rememberLauncherForActivityResult
import android.util.Log

data class BragItem(
    val id: Long,
    val title: String,
    val description: String?,
    val storeName: String?,
    val imageUrl: String,
    val tags: List<String>,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
    val createdAt: String
)

// ──────────────────────────────────────────────────────────────
// 매장 상세 화면
// ──────────────────────────────────────────────────────────────

@Composable
fun StoreDetailScreen(
    storeDetail: StoreDetailResponse,
    reviewData: StoreReviewListResponse?,
    bragData: List<com.example.pickitpickit.core.model.BragDto>?,
    onBackClick: () -> Unit,
    onSubmitReview: (Double, Int, String?) -> Unit,
    isReviewSubmitting: Boolean,
    submitResultFlow: SharedFlow<String?>,
    currentUserId: Long?,
    writeGuide: com.example.pickitpickit.core.model.ReviewWriteGuideResponse?,
    onEditReview: (Long, Double, Int, String?) -> Unit,
    onDeleteReview: (Long) -> Unit,
    isBragSubmitting: Boolean,
    bragSubmitResultFlow: SharedFlow<String?>,
    onSubmitBrag: (Int, String, String) -> Unit,
    onDeleteBrag: (Long) -> Unit,
    onEditBrag: (Long, Int, String, String) -> Unit
) {
    val store = storeDetail.store
    var showWriteDialog by remember { mutableStateOf(false) }
    var editingReview by remember { mutableStateOf<ReviewDto?>(null) }
    var reviewToDelete by remember { mutableStateOf<Long?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // 자랑하기 팝업 및 삭제 상태 관리
    var showBragWriteDialog by remember { mutableStateOf(false) }
    var bragToDelete by remember { mutableStateOf<Long?>(null) }
    var zoomedImageUri by remember { mutableStateOf<String?>(null) }
    var editingBrag by remember { mutableStateOf<com.example.pickitpickit.core.model.BragDto?>(null) }

    LaunchedEffect(Unit) {
        submitResultFlow.collect { result ->
            result?.let { msg ->
                when (msg) {
                    "CREATE_SUCCESS", "" -> {
                        android.widget.Toast.makeText(context, "리뷰가 성공적으로 등록되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                        showWriteDialog = false
                        editingReview = null
                        reviewToDelete = null
                    }
                    "DELETE_SUCCESS" -> {
                        android.widget.Toast.makeText(context, "리뷰가 성공적으로 삭제되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                        showWriteDialog = false
                        editingReview = null
                        reviewToDelete = null
                    }
                    "EDIT_SUCCESS" -> {
                        // 수정은 클릭 즉시 닫히고 자체 토스트를 보여주므로 여기서는 상태 초기화만 처리합니다.
                        showWriteDialog = false
                        editingReview = null
                        reviewToDelete = null
                    }
                    else -> {
                        // 에러 메시지
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        bragSubmitResultFlow.collect { result ->
            result?.let { msg ->
                when (msg) {
                    "CREATE_SUCCESS", "" -> {
                        android.widget.Toast.makeText(context, "자랑글이 성공적으로 등록되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                        showBragWriteDialog = false
                        bragToDelete = null
                    }
                    "DELETE_SUCCESS" -> {
                        android.widget.Toast.makeText(context, "자랑글이 성공적으로 삭제되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                        bragToDelete = null
                    }
                    "EDIT_SUCCESS" -> {
                        showBragWriteDialog = false
                        editingBrag = null
                        bragToDelete = null
                    }
                    else -> {
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 페이지네이션 상태 (상품 목록 페이지)
    var currentPage by remember { mutableIntStateOf(1) }
    val pageSize = 3
    val totalPages = ((storeDetail.products.size - 1) / pageSize + 1).coerceAtLeast(1)
    val pagedProducts = storeDetail.products
        .drop((currentPage - 1) * pageSize)
        .take(pageSize)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── 1. 헤더 이미지 + 뒤로 가기 ──────────────────────────
        item {
            StoreHeroSection(
                storeName = store.name,
                address = store.address ?: "주소 정보 없음",
                imageUrl = store.mainImageUrl,
                onBackClick = onBackClick
            )
        }

        // ── 2. 통계 카드 4종 ────────────────────────────────────
        item {
            StoreStatCards(
                totalStock = storeDetail.totalStockQuantity,
                productCount = storeDetail.productCount,
                nearExpiredCount = storeDetail.products.count {
                    it.stockStatus == "NEAR_EMPTY" || it.stockStatus == "마감임박"
                },
                emptyCount = storeDetail.products.count {
                    it.stockStatus == "EMPTY" || it.stockStatus == "품절"
                }
            )
        }

        // ── 3. 매장 정보 카드 ────────────────────────────────────
        item {
            StoreInfoCard(
                storeName = store.name,
                latitude = store.latitude,
                longitude = store.longitude,
                hours = store.businessHours ?: "영업시간 정보 없음",
                contact = store.contact ?: "전화번호 정보 없음",
                avgDifficulty = if (storeDetail.products.isNotEmpty()) {
                    storeDetail.products.map { it.difficulty }.average()
                } else null,
                kakaoMapUrl = store.kakaoDetailUrl
            )
        }

        // ── 4. 상품 목록 헤더 ────────────────────────────────────
        item {
            ProductSectionHeader(totalCount = storeDetail.productCount)
        }

        // ── 5. 상품 아이템 목록 or 빈 상태 ──────────────────────
        if (storeDetail.products.isEmpty()) {
            item { ProductEmptyState() }
        } else {
            items(pagedProducts) { product ->
                ProductListItem(product = product)
            }

            // ── 6. 페이지네이션 ──────────────────────────────────
            if (totalPages > 1) {
                item {
                    Pagination(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageClick = { currentPage = it }
                    )
                }
            }
        }

        // ── 7. 팁 박스 ───────────────────────────────────────────
        item {
            TipBox()
        }

        // ── 8. 리뷰 목록 헤더 ─────────────────────────────────────
        item {
            ReviewSectionHeader(
                reviewCount = reviewData?.reviewCount ?: 0,
                onWriteClick = { showWriteDialog = true }
            )
        }

        // ── 8.1. 리뷰 평점 분포 요약 카드 ──────────────────────────
        if (reviewData != null && reviewData.reviews.isNotEmpty()) {
            item {
                ReviewSummaryHeader(data = reviewData)
            }
        }

        // ── 9. 리뷰 리스트 or 빈 상태 ─────────────────────────────
        if (reviewData == null || reviewData.reviews.isEmpty()) {
            item {
                ReviewEmptyCard()
            }
        } else {
            items(reviewData.reviews) { review ->
                ReviewListItemCard(
                    review = review,
                    currentUserId = currentUserId,
                    onEditClick = { editingReview = review },
                    onDeleteClick = { reviewToDelete = review.reviewId }
                )
            }
        }

        // ── 10. 자랑하기 섹션 헤더 ──────────────────────────────────
        item {
            BragSectionHeader(
                bragCount = bragData?.size ?: 0,
                onWriteClick = { showBragWriteDialog = true }
            )
        }

        // ── 11. 자랑하기 리스트 or 빈 상태 ───────────────────────────
        if (bragData.isNullOrEmpty()) {
            item {
                BragEmptyCard(
                    onWriteClick = { showBragWriteDialog = true }
                )
            }
        } else {
            items(bragData) { brag ->
                BragListItemCard(
                    brag = brag,
                    currentUserId = currentUserId,
                    onDeleteClick = {
                        bragToDelete = brag.bragId
                    },
                    onEditClick = {
                        editingBrag = brag
                    },
                    onImageClick = { zoomedImageUri = it }
                )
            }
        }
    }

    if (showWriteDialog || editingReview != null) {
        StoreReviewWriteDialog(
            storeName = store.name,
            reviewToEdit = editingReview,
            writeGuide = writeGuide,
            onDismiss = {
                showWriteDialog = false
                editingReview = null
            },
            onSubmit = { rating, difficulty, content ->
                if (editingReview != null) {
                    onEditReview(editingReview!!.reviewId, rating, difficulty, content)
                    editingReview = null // 팝업 즉시 닫기
                    android.widget.Toast.makeText(context, "리뷰가 성공적으로 수정되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    onSubmitReview(rating, difficulty, content)
                }
            },
            isSubmitting = isReviewSubmitting
        )
    }

    if (showBragWriteDialog || editingBrag != null) {
        StoreBragWriteDialog(
            storeName = store.name,
            bragToEdit = editingBrag,
            onDismiss = {
                showBragWriteDialog = false
                editingBrag = null
            },
            onSubmit = { imageUri, content ->
                if (editingBrag != null) {
                    onEditBrag(editingBrag!!.bragId, 0, imageUri, content)
                    editingBrag = null
                    android.widget.Toast.makeText(context, "자랑글이 성공적으로 수정되었습니다!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    onSubmitBrag(0, imageUri, content)
                }
            },
            isSubmitting = isBragSubmitting
        )
    }

    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = {
                Text(
                    text = "리뷰 삭제",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
            },
            text = {
                Text(
                    text = "작성하신 리뷰를 정말로 삭제하시겠습니까?\n삭제된 리뷰는 복구할 수 없습니다.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        reviewToDelete?.let { onDeleteReview(it) }
                        reviewToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { reviewToDelete = null },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Text("취소", color = Color(0xFF4B5563))
                }
            },
            containerColor = Color.White,
            properties = DialogProperties(usePlatformDefaultWidth = true)
        )
    }

    if (bragToDelete != null) {
        AlertDialog(
            onDismissRequest = { bragToDelete = null },
            title = {
                Text(
                    text = "자랑글 삭제",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
            },
            text = {
                Text(
                    text = "작성하신 자랑글을 정말로 삭제하시겠습니까?\n삭제된 자랑글은 복구할 수 없습니다.",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        bragToDelete?.let { onDeleteBrag(it) }
                        bragToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { bragToDelete = null },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB))
                ) {
                    Text("취소", color = Color(0xFF4B5563))
                }
            },
            containerColor = Color.White,
            properties = DialogProperties(usePlatformDefaultWidth = true)
        )
    }

    if (zoomedImageUri != null) {
        Dialog(
            onDismissRequest = { zoomedImageUri = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { zoomedImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = zoomedImageUri,
                    contentDescription = "자랑 원본 이미지",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { zoomedImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 40.dp, end = 20.dp)
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "닫기",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 헤어로 이미지 섹션
// ──────────────────────────────────────────────────────────────

@Composable
private fun StoreHeroSection(
    storeName: String,
    address: String,
    imageUrl: String?,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // 배경 이미지
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "매장 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF7B3FBF), Color(0xFF4A2B8A), Color(0xFF1A1040))
                        )
                    )
            )
        }

        // 상단 그라데이션 스크림 (뒤로 가기 버튼 가독성)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        // 하단 그라데이션 스크림 (텍스트 가독성)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )

        // 뒤로 가기 버튼
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable { onBackClick() }
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로 가기",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("뒤로 가기", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }

        // 하단 매장명 + 주소
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = storeName,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFFCA28),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = address,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 통계 카드 2×2 그리드
// ──────────────────────────────────────────────────────────────

@Composable
private fun StoreStatCards(
    totalStock: Int,
    productCount: Int,
    nearExpiredCount: Int,
    emptyCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "총 재고",
                value = "$totalStock",
                unit = "개",
                gradient = Brush.linearGradient(listOf(Color(0xFF6B8AF7), Color(0xFF8B5CF6))),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "상품 종류",
                value = "$productCount",
                unit = "종",
                gradient = Brush.linearGradient(listOf(Color(0xFFA78BFA), Color(0xFF7C3AED))),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                label = "마감임박",
                value = "$nearExpiredCount",
                unit = "개",
                gradient = Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEF4444))),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "품절",
                value = "$emptyCount",
                unit = "개",
                gradient = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316))),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 32.sp)
                Spacer(modifier = Modifier.width(3.dp))
                Text(unit, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 매장 정보 카드
// ──────────────────────────────────────────────────────────────

@Composable
private fun StoreInfoCard(
    storeName: String,
    latitude: Double,
    longitude: Double,
    hours: String,
    contact: String,
    avgDifficulty: Double?,
    kakaoMapUrl: String?
) {
    val difficultyLabel = when {
        avgDifficulty == null -> "정보 없음"
        avgDifficulty <= 1.5 -> "매우 쉬움 😊"
        avgDifficulty <= 2.5 -> "보통 😊"
        avgDifficulty <= 3.5 -> "보통 😐"
        avgDifficulty <= 4.5 -> "어려움 😅"
        else -> "극악 😱"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "매장 정보",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1A1A2E)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Spacer(modifier = Modifier.height(14.dp))

            // 영업 시간
            InfoRow(
                icon = "🕐",
                iconBgColor = Color(0xFFE8F5FF),
                label = "영업 시간",
                value = hours
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 전화번호
            InfoRow(
                icon = "📞",
                iconBgColor = Color(0xFFE8FFE8),
                label = "전화번호",
                value = contact
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 평균 난이도
            InfoRow(
                icon = "🎯",
                iconBgColor = Color(0xFFFFF3E0),
                label = "평균 난이도",
                value = difficultyLabel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 길찾기 버튼
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val encodedStoreName = try {
                        java.net.URLEncoder.encode(storeName, "UTF-8")
                    } catch (e: java.io.UnsupportedEncodingException) {
                        storeName
                    }
                    val appScheme = "kakaomap://route?ep=$latitude,$longitude&en=$encodedStoreName&by=CAR"
                    val webUrl = "https://map.kakao.com/link/to/$storeName,$latitude,$longitude"
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(appScheme))
                        context.startActivity(intent)
                    } catch (e: android.content.ActivityNotFoundException) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(webUrl))
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B6EF8)
                )
            ) {
                Text("✈ ", fontSize = 14.sp, color = Color.White)
                Text(
                    "길찾기 (Kakao Maps)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun InfoRow(icon: String, iconBgColor: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF999999))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1A1A2E))
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 상품 목록 섹션 헤더
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProductSectionHeader(totalCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎁", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "상품 목록",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1A2E)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(Color(0xFF3B6EF8))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text("${totalCount}개 상품", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }

    Text(
        "상품을 클릭하면 상세 정보를 확인할 수 있어요!",
        fontSize = 12.sp,
        color = Color(0xFFAAAAAA),
        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp)
    )
}

// ──────────────────────────────────────────────────────────────
// 상품 아이템 카드
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProductListItem(product: ProductDto) {
    val stockStatus = when (product.stockStatus?.uppercase()) {
        "NEAR_EMPTY", "마감임박" -> "마감임박" to Color(0xFFEF4444)
        "EMPTY", "품절" -> "품절" to Color(0xFF9E9E9E)
        "PLENTY", "재고충분" -> "재고충분" to Color(0xFF22C55E)
        else -> "보통" to Color(0xFF3B82F6)
    }

    val difficultyLabel = when {
        product.difficulty <= 1 -> "쉬움" to Color(0xFF22C55E)
        product.difficulty <= 2 -> "보통" to Color(0xFF3B82F6)
        product.difficulty <= 3 -> "어려움" to Color(0xFFF97316)
        else -> "극악" to Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { /* TODO: 상품 상세 클릭 */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 상품 이미지
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.itemName,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                // 재고 상태 뱃지
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(stockStatus.second)
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(stockStatus.first, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 상품명
                Text(
                    text = product.itemName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1A1A2E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 난이도 + 재고 + 가격
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(difficultyLabel.second.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            difficultyLabel.first,
                            color = difficultyLabel.second,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "재고 ${product.stockQuantity}개",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "소형 ${product.price / 10000}만 ${product.price % 10000}원".let {
                            if (product.price < 10000) "소형 ${product.price}원" else it
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 태그
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    product.tags.take(3).forEach { tag ->
                        Text(
                            text = "#${tag.name}",
                            fontSize = 11.sp,
                            color = Color(0xFF3B6EF8),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEEF3FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 페이지네이션
// ──────────────────────────────────────────────────────────────

@Composable
private fun Pagination(currentPage: Int, totalPages: Int, onPageClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이전 버튼
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (currentPage > 1) Color(0xFF3B6EF8) else Color(0xFFE0E0E0))
                .clickable(enabled = currentPage > 1) { onPageClick(currentPage - 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 페이지 번호들
        val pagesToShow = (1..totalPages).toList().take(5)
        pagesToShow.forEach { page ->
            val isSelected = page == currentPage
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF3B6EF8) else Color.White)
                    .border(1.dp, if (isSelected) Color(0xFF3B6EF8) else Color(0xFFDDDDDD), CircleShape)
                    .clickable { onPageClick(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$page",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else Color(0xFF555555)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        // 다음 버튼
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (currentPage < totalPages) Color(0xFF3B6EF8) else Color(0xFFE0E0E0))
                .clickable(enabled = currentPage < totalPages) { onPageClick(currentPage + 1) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 상품 없음 빈 상태
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProductEmptyState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎰", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "아직 등록된 상품이 없어요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "상품 정보가 곧 업데이트될 예정이에요!\n조금만 기다려 주세요 🙏",
                fontSize = 13.sp,
                color = Color(0xFF999999),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 팁 박스
// ──────────────────────────────────────────────────────────────

@Composable
private fun TipBox() {
    val tips = listOf(
        "상품 클릭으로 상세 정보를 확인하세요!",
        "마감임박 상품은 3개 이하로 재고가 적으니 서두르세요!",
        "난이도 쉬움 상품은 초보자도 쉽게 도전할 수 있어요!",
        "재고는 실시간 업데이트되니 방문 전 꼭 확인하세요!"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.dp).let {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD600))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💡", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("꿀팁!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFB45309))
            }
            Spacer(modifier = Modifier.height(10.dp))
            tips.forEach { tip ->
                Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                    Text("✓ ", fontSize = 12.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                    Text(tip, fontSize = 12.sp, color = Color(0xFF92400E), lineHeight = 18.sp)
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 리뷰 요약 및 리스트 컴포넌트들
// ──────────────────────────────────────────────────────────────

@Composable
private fun ReviewSectionHeader(
    reviewCount: Int,
    onWriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⭐ 리뷰",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1A2E)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEF2FF)) // 연한 보라/블루 톤
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${reviewCount}개",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3B6EF8)
                )
            }
        }

        // 리뷰 작성 버튼 (프리미엄 노란색/오렌지 버튼)
        Button(
            onClick = onWriteClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB300)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "⭐ 리뷰 작성",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
        }
    }
}

@Composable
private fun ReviewEmptyCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 말풍선 아이콘 컨테이너 (제공된 시안과 완벽한 일치)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💬",
                    fontSize = 42.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "아직 리뷰가 없어요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "첫 번째 리뷰를 남겨보세요!",
                fontSize = 13.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

@Composable
private fun ReviewListItemCard(
    review: ReviewDto,
    currentUserId: Long?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
                // 프로필 이미지 및 플레이스홀더 서클 처리
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
                    val firstChar = review.authorNickname.firstOrNull()?.toString() ?: "👤"
                    val colors = listOf(
                        Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B),
                        Color(0xFFEF4444), Color(0xFFEC4899), Color(0xFF14B8A6), Color(0xFF6366F1)
                    )
                    val backgroundColor = colors[java.lang.Math.abs(review.authorNickname.hashCode()) % colors.size]
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstChar,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 닉네임 & 상대 날짜 표시
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.authorNickname,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getRelativeTimeString(review.createdAt),
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        if (review.userId == currentUserId) {
                            Text(
                                text = " • ",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "수정",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B6EF8),
                                modifier = Modifier.clickable { onEditClick() }
                            )
                            Text(
                                text = " | ",
                                fontSize = 11.sp,
                                color = Color(0xFFD1D5DB)
                            )
                            Text(
                                text = "삭제",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable { onDeleteClick() }
                            )
                        }
                    }
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


@Composable
private fun StoreReviewWriteDialog(
    storeName: String,
    reviewToEdit: ReviewDto? = null,
    writeGuide: com.example.pickitpickit.core.model.ReviewWriteGuideResponse? = null,
    onDismiss: () -> Unit,
    onSubmit: (Double, Int, String?) -> Unit,
    isSubmitting: Boolean
) {
    var rating by remember(reviewToEdit) { mutableDoubleStateOf(reviewToEdit?.rating ?: 5.0) }
    var difficulty by remember(reviewToEdit) { mutableIntStateOf(reviewToEdit?.difficulty ?: 3) } // 보통 기본값
    var content by remember(reviewToEdit) { mutableStateOf(reviewToEdit?.content ?: "") }
    val contentCharLimit = 200

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isSubmitting,
            dismissOnClickOutside = !isSubmitting,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 상단 X 닫기 버튼 영역
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("✕", fontSize = 16.sp, color = Color(0xFF9CA3AF))
                    }
                }

                // 스크롤 가능한 본문 영역
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (reviewToEdit != null) "✏️ 매장 리뷰 수정" else "⭐ 매장 리뷰 작성",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${storeName}을(를) 방문하셨나요? 경험을 공유해주세요!",
                        fontSize = 13.sp,
                        color = Color(0xFF3B6EF8),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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

                    Text(
                        text = "한 줄 평가 (선택사항)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )

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

                    Text(
                        text = "${content.length}/$contentCharLimit",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )

                    // ── 💡 가이드 아코디언 카드 영역 ──────────────────
                    if (writeGuide != null) {
                        var isGuideExpanded by remember { mutableStateOf(false) }

                        LaunchedEffect(isGuideExpanded) {
                            if (isGuideExpanded) {
                                // 레이아웃 확장 애니메이션(animateContentSize) 시간에 맞춰 부드럽게 하단 자동 스크롤
                                kotlinx.coroutines.delay(150)
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Column(
                                modifier = Modifier
                                    .clickable { isGuideExpanded = !isGuideExpanded }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "💡", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "리뷰 작성 가이드 꿀팁",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4B5563)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isGuideExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (isGuideExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE5E7EB))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 후기 가이드
                                    val reviewG = writeGuide.reviewGuide
                                    Text(
                                        text = "📝 ${reviewG.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    reviewG.messages.forEach { msg ->
                                        Text(
                                            text = "• $msg",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4B5563),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 6.dp, bottom = 2.dp),
                                            textAlign = TextAlign.Start
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // 자랑하기 가이드
                                    val bragG = writeGuide.bragGuide
                                    Text(
                                        text = "📸 ${bragG.title}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    bragG.messages.forEach { msg ->
                                        Text(
                                            text = "• $msg",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4B5563),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 6.dp, bottom = 2.dp),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 취소 / 등록 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("취소", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSubmit(rating, difficulty, content.ifBlank { null })
                        },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color(0xFF1A1A2E), modifier = Modifier.size(20.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (reviewToEdit != null) {
                                    Text("✏️ ", fontSize = 14.sp, color = Color(0xFF1A1A2E))
                                    Text("리뷰 수정", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                                } else {
                                    Text("✈ ", fontSize = 14.sp, color = Color(0xFF1A1A2E))
                                    Text("리뷰 등록", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 자랑하기 관련 컴포넌트들
// ──────────────────────────────────────────────────────────────

@Composable
private fun BragSectionHeader(
    bragCount: Int,
    onWriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🎉 자랑하기",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1A2E)
            )
        }

        // 자랑하기 작성 버튼 (그라데이션 버튼)
        val bragGradient = Brush.horizontalGradient(
            colors = listOf(Color(0xFFF6339A), Color(0xFFAD46FF))
        )
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bragGradient)
                .clickable { onWriteClick() }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_profile),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "자랑하기 작성",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BragEmptyCard(
    onWriteClick: () -> Unit
) {
    val bragGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF6339A), Color(0xFFAD46FF))
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(Color(0xFFF6339A).copy(alpha = 0.2f), Color(0xFFAD46FF).copy(alpha = 0.2f))
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 스파클 아이콘 컨테이너
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF6339A).copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_tag),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(42.dp)
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(
                                    brush = bragGradient,
                                    blendMode = BlendMode.SrcAtop
                                )
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "아직 자랑하기가 없어요",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "이 매장에서 뽑은 인형이나 가챠를 자랑해보세요!",
                fontSize = 13.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 첫 번째 자랑하기 작성 버튼
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(bragGradient)
                    .clickable { onWriteClick() }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_profile),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "첫 번째 자랑하기 작성",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BragListItemCard(
    brag: com.example.pickitpickit.core.model.BragDto,
    currentUserId: Long?,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val (title, description, storeName, tags) = parseBragContent(brag.content)

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
            // 작성자 프로필
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!brag.authorProfileImageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = brag.authorProfileImageUrl,
                        contentDescription = "프로필 이미지",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brag.authorNickname,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val formattedDate = try {
                            val isoDateTime = brag.createdAt
                            if (isoDateTime.contains("T")) {
                                val datePart = isoDateTime.split("T")[0]
                                val timePart = isoDateTime.split("T")[1].substring(0, 5)
                                "$datePart $timePart"
                            } else {
                                isoDateTime
                            }
                        } catch (e: Exception) {
                            brag.createdAt
                        }
                        Text(
                            text = formattedDate,
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                        if (brag.userId == currentUserId) {
                            Text(
                                text = " • ",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "수정",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B6EF8),
                                modifier = Modifier.clickable { onEditClick() }
                            )
                            Text(
                                text = " • ",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            Text(
                                text = "삭제",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable { onDeleteClick() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 자랑 이미지
            AsyncImage(
                model = brag.imageUrl,
                contentDescription = "자랑 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onImageClick(brag.imageUrl) },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 제목
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )

            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            }

            if (!storeName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = storeName,
                        fontSize = 12.sp,
                        color = Color(0xFF3B6EF8),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            fontSize = 11.sp,
                            color = Color(0xFFAD46FF),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF5F3FF))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreBragWriteDialog(
    storeName: String,
    bragToEdit: com.example.pickitpickit.core.model.BragDto? = null,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit, // (imageUrl, content)
    isSubmitting: Boolean
) {
    val parsedContent = remember(bragToEdit) {
        bragToEdit?.let { parseBragContent(it.content) }
    }

    var title by remember { mutableStateOf(parsedContent?.first ?: "") }
    var description by remember { mutableStateOf(parsedContent?.second ?: "") }
    var customStoreName by remember { mutableStateOf(parsedContent?.third ?: storeName) }
    var selectedImageUri by remember { mutableStateOf<String?>(bragToEdit?.imageUrl) }
    var tagInput by remember { mutableStateOf("") }
    var tagsList by remember { mutableStateOf(parsedContent?.fourth ?: emptyList<String>()) }
    
    val titleLimit = 50
    val descLimit = 300
    val maxTags = 5

    // 카메라/갤러리 선택 팝업 상태
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    // 카메라 사진 저장을 위한 임시 파일 및 URI 관리
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // 갤러리 이미지 선택 런처
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                    val file = java.io.File.createTempFile("BRAG_${timeStamp}_", ".jpg", storageDir)
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val fileProviderUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    selectedImageUri = fileProviderUri.toString()
                }
            } catch (e: Exception) {
                Log.e("GALLERY_FLOW", "갤러리 이미지 복사 에러", e)
                android.widget.Toast.makeText(context, "이미지를 가져오는 데 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카메라 촬영 런처
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            selectedImageUri = tempPhotoUri.toString()
        }
    }

    // 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri?.let { takePictureLauncher.launch(it) }
        } else {
            android.widget.Toast.makeText(context, "사진을 찍기 위해 카메라 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 헬퍼: 카메라 띄우기 전 권한 확인 및 임시 파일 생성
    fun launchCameraFlow() {
        try {
            val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
            val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
            val file = java.io.File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            tempPhotoUri = uri

            // 권한 체크
            val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            )
            if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                takePictureLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        } catch (e: Exception) {
            Log.e("CAMERA_FLOW", "카메라 촬영 준비 에러", e)
            android.widget.Toast.makeText(context, "카메라 실행 준비에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()

    if (showPhotoSourceDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = {
                Text(
                    text = "사진 추가하기",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A2E)
                )
            },
            text = {
                Text(
                    text = "어떤 방법으로 사진을 추가하시겠습니까?",
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhotoSourceDialog = false
                        launchCameraFlow()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B6EF8)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("직접 촬영", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showPhotoSourceDialog = false
                        pickMediaLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4B5563)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("갤러리에서 선택", color = Color.White)
                }
            },
            containerColor = Color.White,
            properties = DialogProperties(usePlatformDefaultWidth = true)
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // 상단 X 닫기 버튼 영역
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("✕", fontSize = 16.sp, color = Color(0xFF9CA3AF))
                    }
                }

                // 스크롤 가능한 본문 영역
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = if (bragToEdit != null) "✏️ 자랑하기 수정" else "🎉 자랑하기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (bragToEdit != null) "작성하신 자랑글 내용을 수정해 보세요!" else "내가 뽑은 인형이나 가챠를 자랑해보세요!",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. 사진 선택 영역 (필수)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("사진", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("*", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 점선 테두리 상자
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF9FAFB))
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFD1D5DB),
                                    style = stroke,
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                            }
                            .clickable(enabled = !isSubmitting) {
                                showPhotoSourceDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "선택된 이미지",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                // 이미지 위에 닫기 버튼 배치
                                if (!isSubmitting) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable { selectedImageUri = null },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✕", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_profile),
                                        contentDescription = null,
                                        tint = Color(0xFF3B6EF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("사진 추가하기", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("클릭하여 이미지 업로드", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. 제목 입력 (필수)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("제목", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("*", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = title,
                        onValueChange = {
                            if (it.length <= titleLimit) title = it
                        },
                        enabled = !isSubmitting,
                        placeholder = {
                            Text("예: 드디어 뽑았어요! 🎉", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1F2937)),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${title.length}/$titleLimit",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. 설명 입력 (선택)
                    Text("설명 (선택사항)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = description,
                        onValueChange = {
                            if (it.length <= descLimit) description = it
                        },
                        enabled = !isSubmitting,
                        placeholder = {
                            Text("뽑기 성공 스토리를 들려주세요!", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1F2937)),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${description.length}/$descLimit",
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.align(Alignment.End)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. 매장명 (선택)
                    Text("매장명 (선택사항)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = customStoreName,
                        onValueChange = { customStoreName = it },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9FAFB),
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1F2937)),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. 태그 입력 (선택, 최대 5개)
                    Text("태그 (선택사항, 최대 5개)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = tagInput,
                            onValueChange = { tagInput = it },
                            enabled = !isSubmitting,
                            placeholder = {
                                Text("태그 입력 후 추가 버튼", fontSize = 13.sp, color = Color(0xFF9CA3AF))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9FAFB),
                                unfocusedContainerColor = Color(0xFFF9FAFB),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = TextStyle(fontSize = 13.sp, color = Color(0xFF1F2937)),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val trimmed = tagInput.trim()
                                if (trimmed.isNotEmpty() && tagsList.size < maxTags && !tagsList.contains(trimmed)) {
                                    tagsList = tagsList + trimmed
                                    tagInput = ""
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF3B6EF8)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF3B6EF8)
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("추가", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 추가된 태그들 칩으로 표시
                    if (tagsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(tagsList) { tag ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF5F3FF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        color = Color(0xFFAD46FF)
                                    )
                                    if (!isSubmitting) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "✕",
                                            fontSize = 10.sp,
                                            color = Color(0xFF9CA3AF),
                                            modifier = Modifier.clickable {
                                                tagsList = tagsList.filter { it != tag }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 취소 / 게시하기 버튼 영역
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF6B7280)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("취소", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    val buttonEnabled = selectedImageUri != null && title.isNotBlank() && !isSubmitting

                    Button(
                        onClick = {
                            if (selectedImageUri == null) return@Button
                            if (title.isBlank()) return@Button

                            // 포맷에 맞춘 content 조립
                            val finalContent = buildString {
                                append(title)
                                if (description.isNotBlank()) {
                                    append("\n\n")
                                    append(description)
                                }
                                if (customStoreName.isNotBlank()) {
                                    append("\n\n📍")
                                    append(customStoreName)
                                }
                                if (tagsList.isNotEmpty()) {
                                    append("\n\n")
                                    append(tagsList.joinToString(" ") { "#$it" })
                                }
                            }

                            onSubmit(selectedImageUri!!, finalContent)
                        },
                        enabled = buttonEnabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            disabledContainerColor = Color(0xFFE5E7EB)
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = if (buttonEnabled) Color.White else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (bragToEdit != null) "수정하기" else "게시하기",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (buttonEnabled) Color.White else Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 헬퍼: 자랑글 content 파싱 함수
// ──────────────────────────────────────────────────────────────
private fun parseBragContent(content: String): Tuple4<String, String?, String?, List<String>> {
    val lines = content.split("\n\n")
    if (lines.isEmpty()) return Tuple4("", null, null, emptyList())
    val title = lines[0]
    var description: String? = null
    var storeName: String? = null
    val tags = mutableListOf<String>()

    if (lines.size > 1) {
        val remaining = lines.subList(1, lines.size)
        // 마지막 라인이 태그인지 확인
        val lastLine = remaining.last().trim()
        val isLastLineTags = lastLine.split(" ").all { it.startsWith("#") }
        
        val cleanRemaining = if (isLastLineTags) {
            lastLine.split(" ").forEach {
                val clean = it.removePrefix("#").trim()
                if (clean.isNotEmpty()) tags.add(clean)
            }
            remaining.dropLast(1)
        } else {
            remaining
        }
        
        val descLines = mutableListOf<String>()
        cleanRemaining.forEach { line ->
            if (line.trim().startsWith("📍")) {
                storeName = line.trim().removePrefix("📍")
            } else {
                descLines.add(line)
            }
        }
        if (descLines.isNotEmpty()) {
            description = descLines.joinToString("\n\n")
        }
    }
    return Tuple4(title, description, storeName, tags)
}

// kotlin에는 표준 Tuple4가 없으므로 간단히 데이터 클래스로 정의해서 쓴다.
private data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "매장 상세 페이지")
@Composable
fun StoreDetailScreenPreview() {
    PickitPickitTheme {
        val dummyProducts = listOf(
            ProductDto(
                productId = 1L, itemId = 1L,
                itemName = "산리오 헬로키티",
                category = "인형",
                price = 1000,
                inventoryMode = "소형",
                stockQuantity = 15,
                stockStatus = "재고충분",
                difficulty = 1,
                difficultyLabel = "쉬움",
                imageUrl = null,
                tags = listOf(TagDto(1L, "산리오"), TagDto(2L, "헬로키티"), TagDto(3L, "인형"))
            ),
            ProductDto(
                productId = 2L, itemId = 2L,
                itemName = "뚱구 인형",
                category = "인형",
                price = 1000,
                inventoryMode = "소형",
                stockQuantity = 6,
                stockStatus = "보통",
                difficulty = 2,
                difficultyLabel = "보통",
                imageUrl = null,
                tags = listOf(TagDto(1L, "뚱구"), TagDto(2L, "애니메이션"), TagDto(3L, "인형"))
            ),
            ProductDto(
                productId = 3L, itemId = 3L,
                itemName = "원피스 루피 피규어",
                category = "피규어",
                price = 1000,
                inventoryMode = "대형",
                stockQuantity = 13,
                stockStatus = "마감임박",
                difficulty = 4,
                difficultyLabel = "극악",
                imageUrl = null,
                tags = listOf(TagDto(1L, "원피스"), TagDto(2L, "루피"), TagDto(3L, "피규어"))
            )
        )
        val dummyStore = StoreDetailResponse(
            store = StoreDetailDto(
                id = 1L, sourcePlaceId = null,
                name = "짱오락실 홍대점",
                type = "CLAW",
                latitude = 37.5565, longitude = 126.9235,
                distance = 45,
                address = "서울특별시 마포구 홍익로 45",
                contact = "02-2345-6789",
                businessHours = "09:00 - 23:00",
                mainImageUrl = null,
                kakaoDetailUrl = null
            ),
            productCount = 42,
            totalStockQuantity = 384,
            tags = listOf(TagDto(1L, "24시간"), TagDto(2L, "혜자샵")),
            products = dummyProducts
        )
        StoreDetailScreen(
            storeDetail = dummyStore,
            reviewData = null,
            bragData = null,
            onBackClick = {},
            onSubmitReview = { _, _, _ -> },
            isReviewSubmitting = false,
            submitResultFlow = kotlinx.coroutines.flow.MutableSharedFlow(),
            currentUserId = null,
            writeGuide = null,
            onEditReview = { _, _, _, _ -> },
            onDeleteReview = {},
            isBragSubmitting = false,
            bragSubmitResultFlow = kotlinx.coroutines.flow.MutableSharedFlow(),
            onSubmitBrag = { _, _, _ -> },
            onDeleteBrag = {},
            onEditBrag = { _, _, _, _ -> }
        )
    }
}

// ──────────────────────────────────────────────────────────────
// 헬퍼 컴포넌트 & 유틸리티 함수들 (리뷰 디자인 개편 추가)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ReviewSummaryHeader(data: StoreReviewListResponse) {
    val ratingCounts = remember(data.reviews) {
        val counts = IntArray(6)
        data.reviews.forEach { r ->
            val star = r.rating.toInt()
            if (star in 1..5) {
                counts[star]++
            }
        }
        counts
    }
    val totalReviews = data.reviewCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 좌측: 평균 평점 요약
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = String.format("%.1f", data.averageRating),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    val average = data.averageRating.toInt()
                    for (i in 1..5) {
                        val isYellow = i <= average
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isYellow) Color(0xFFFFCA28) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${totalReviews}개 리뷰",
                    fontSize = 11.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }

            // 세로 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(Color(0xFFE5E7EB))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 우측: 평점별 가로 막대 그래프 리스트
            Column(
                modifier = Modifier.weight(1.8f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (star in 5 downTo 1) {
                    val count = ratingCounts[star]
                    val progress = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = star.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF555555),
                            modifier = Modifier.width(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFCA28),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 비율 바
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE5E7EB))
                        ) {
                            if (progress > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(Color(0xFFFF9635), RoundedCornerShape(4.dp))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = count.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF777777),
                            modifier = Modifier.width(14.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

private fun getRelativeTimeString(createdAtStr: String): String {
    return try {
        val cleanedStr = if (createdAtStr.contains(".") && !createdAtStr.endsWith("Z")) {
            createdAtStr.split(".")[0]
        } else {
            createdAtStr
        }
        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
        val dateTime = java.time.LocalDateTime.parse(cleanedStr.removeSuffix("Z"), formatter)
        val now = java.time.LocalDateTime.now()
        val duration = java.time.Duration.between(dateTime, now)
        val seconds = duration.seconds
        
        when {
            seconds < 0 -> "방금 전"
            seconds < 60 -> "방금 전"
            seconds < 3600 -> "${seconds / 60}분 전"
            seconds < 86400 -> "${seconds / 3600}시간 전"
            seconds < 2592000 -> "${seconds / 86400}일 전"
            else -> createdAtStr.split("T").firstOrNull() ?: createdAtStr
        }
    } catch (e: Exception) {
        try {
            createdAtStr.split("T").firstOrNull() ?: createdAtStr
        } catch (ex: Exception) {
            createdAtStr
        }
    }
}

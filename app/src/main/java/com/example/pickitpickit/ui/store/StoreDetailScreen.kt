package com.example.pickitpickit.ui.store

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.ProductDto
import com.example.pickitpickit.core.model.StoreDetailDto
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.model.TagDto
import com.example.pickitpickit.ui.theme.PickitPickitTheme

// ──────────────────────────────────────────────────────────────
// 매장 상세 화면
// ──────────────────────────────────────────────────────────────

@Composable
fun StoreDetailScreen(
    storeDetail: StoreDetailResponse,
    onBackClick: () -> Unit
) {
    val store = storeDetail.store

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
            Button(
                onClick = { /* TODO: 카카오맵 아웃링크 */ },
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
                    "길찾기 (Google Maps)",
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
        StoreDetailScreen(storeDetail = dummyStore, onBackClick = {})
    }
}

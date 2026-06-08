package com.example.pickitpickit.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.painterResource
import com.example.pickitpickit.R
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pickitpickit.core.model.ProductDto
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.model.StoreDetailDto
import com.example.pickitpickit.core.model.TagDto
import com.example.pickitpickit.core.network.api.ItemDto
import com.example.pickitpickit.ui.store.StoreDetailUiState
import com.example.pickitpickit.ui.store.StoreDetailViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.util.Log

// ──────────────────────────────────────────────────────────────
// 매장 관리 화면 (관리자 전용)
// ──────────────────────────────────────────────────────────────

@Composable
fun StoreAdminScreen(
    storeId: Int,
    storeName: String,
    onBackClick: () -> Unit,
    viewModel: StoreDetailViewModel = viewModel(
        factory = StoreDetailViewModel.Factory(storeId = storeId)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val itemSearchResults by viewModel.itemSearchResults.collectAsState()
    val isItemSearching by viewModel.isItemSearching.collectAsState()
    val isOwnerActionSubmitting by viewModel.isOwnerActionSubmitting.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var productToEdit by remember { mutableStateOf<ProductDto?>(null) }
    var productToDelete by remember { mutableStateOf<ProductDto?>(null) }
    var showEditTagsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.ownerActionResult.collect { result ->
            when (result) {
                "ADD_SUCCESS" -> {
                    android.widget.Toast.makeText(context, "상품이 등록되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                }
                "UPDATE_SUCCESS" -> {
                    android.widget.Toast.makeText(context, "상품 정보가 수정되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    productToEdit = null
                }
                "DELETE_SUCCESS" -> {
                    android.widget.Toast.makeText(context, "상품이 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    productToDelete = null
                }
                "UPDATE_TAGS_SUCCESS" -> {
                    android.widget.Toast.makeText(context, "매장 대표 태그가 수정되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                    showEditTagsDialog = false
                }
                else -> {
                    errorMessage = result
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F2))
    ) {
        when (val state = uiState) {
            is StoreDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF97316))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("매장 정보를 불러오는 중...", color = Color(0xFF888888), fontSize = 13.sp)
                    }
                }
            }

            is StoreDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("😢", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(state.message, color = Color(0xFF555555), fontSize = 14.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.loadStoreDetail() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                        ) {
                            Text("다시 시도", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is StoreDetailUiState.Success -> {
                StoreAdminContent(
                    detail = state.detail,
                    storeName = storeName,
                    onBackClick = onBackClick,
                    itemSearchResults = itemSearchResults,
                    isItemSearching = isItemSearching,
                    onSearchItems = viewModel::searchItems,
                    onAddClick = { itemId, itemName, category, price, stockQuantity, difficulty, imageUrl, tags ->
                        viewModel.addStoreProduct(
                            itemId = itemId,
                            itemName = itemName,
                            category = category,
                            price = price,
                            inventoryMode = "QUANTITY",
                            stockQuantity = stockQuantity,
                            stockStatus = "IN_STOCK",
                            difficulty = difficulty,
                            imageUrl = imageUrl,
                            tags = tags
                        )
                    },
                    onEditClick = { product -> productToEdit = product },
                    onDeleteClick = { product -> productToDelete = product },
                    onEditTagsClick = { showEditTagsDialog = true }
                )
            }
        }

        // ── 수정 다이얼로그 ─────────────────────────────────────
        if (productToEdit != null) {
            EditProductDialog(
                product = productToEdit!!,
                onDismiss = { productToEdit = null },
                onConfirm = { price, stockQuantity, difficulty, updatedImageUrl, tags ->
                    viewModel.updateStoreProduct(
                        productId = productToEdit!!.productId,
                        price = price,
                        inventoryMode = "QUANTITY",
                        stockQuantity = stockQuantity,
                        stockStatus = "IN_STOCK",
                        difficulty = difficulty,
                        imageUrl = updatedImageUrl,
                        tags = tags
                    )
                }
            )
        }

        // ── 대표 태그 수정 다이얼로그 ─────────────────────────────────────
        if (showEditTagsDialog && uiState is StoreDetailUiState.Success) {
            val currentTags = (uiState as StoreDetailUiState.Success).detail.tags.map { it.name }
            EditStoreTagsDialog(
                initialTags = currentTags,
                onDismiss = { showEditTagsDialog = false },
                onConfirm = { updatedTags ->
                    viewModel.updateStoreTags(updatedTags)
                }
            )
        }

        // ── 삭제 다이얼로그 ─────────────────────────────────────
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("상품 삭제", fontWeight = FontWeight.Bold) },
                text = { Text("'${productToDelete!!.itemName}' 상품을 내 매장에서 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.deleteStoreProduct(productToDelete!!.productId) },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("삭제", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("취소", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }

        // ── 로딩 오버레이 ─────────────────────────────────────
        if (isOwnerActionSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFF97316))
            }
        }

        // ── 에러 다이얼로그 ─────────────────────────────────────
        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("오류", fontWeight = FontWeight.Bold) },
                text = { Text(errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = { errorMessage = null }) {
                        Text("확인", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 매장 관리 콘텐츠
// ──────────────────────────────────────────────────────────────

@Composable
private fun StoreAdminContent(
    detail: StoreDetailResponse,
    storeName: String,
    onBackClick: () -> Unit,
    itemSearchResults: List<ItemDto>,
    isItemSearching: Boolean,
    onSearchItems: (String) -> Unit,
    onAddClick: (itemId: Long, itemName: String, category: String, price: Int, stockQuantity: Int, difficulty: Int, imageUrl: String?, tags: List<String>) -> Unit,
    onEditClick: (ProductDto) -> Unit,
    onDeleteClick: (ProductDto) -> Unit,
    onEditTagsClick: () -> Unit
) {
    var showAddProductForm by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // ── 헤더 ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF97316), Color(0xFFEF4444))
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_store),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "매장 관리",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            storeName.ifEmpty { detail.store.name },
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.logo_pikipiki),
                        contentDescription = "삐끼삐끼 로고",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(1.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // ── 요약 카드 3종 ─────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    emoji = "📦",
                    label = "총 재고",
                    value = "${detail.totalStockQuantity}개",
                    color = Color(0xFF6B4EFF),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    emoji = "🛍️",
                    label = "상품 종류",
                    value = "${detail.productCount}종",
                    color = Color(0xFFEC4899),
                    modifier = Modifier.weight(1f)
                )
                val avgPrice = if (detail.products.isNotEmpty())
                    detail.products.map { it.price }.average().toInt()
                else 0
                SummaryCard(
                    emoji = "💰",
                    label = "평균 가격",
                    value = "${avgPrice}원",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 매장 대표 태그 영역 ──────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ 매장 대표 태그",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = onEditTagsClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "태그 수정",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (detail.tags.isEmpty()) {
                        Text(
                            text = "등록된 대표 태그가 없습니다. 우측 수정 버튼을 통해 등록해보세요!",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            detail.tags.forEach { tagDto ->
                                TagChip(text = "#${tagDto.name}", color = Color(0xFF6B4EFF))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── 상품 추가 버튼 ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { showAddProductForm = !showAddProductForm },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (showAddProductForm) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showAddProductForm) "접기" else "상품 추가",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // ── 상품 추가 폼 (API 연동 완료) ───────────────────────
            AnimatedVisibility(
                visible = showAddProductForm,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                AddProductForm(
                    itemSearchResults = itemSearchResults,
                    isItemSearching = isItemSearching,
                    onSearchItems = onSearchItems,
                    onAddConfirm = { itemId, itemName, category, price, stockQty, difficulty, imageUrl, tags ->
                        onAddClick(itemId, itemName, category, price, stockQty, difficulty, imageUrl, tags)
                        showAddProductForm = false
                    },
                    onCancel = { showAddProductForm = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 상품 목록 ─────────────────────────────────────
            Text(
                "상품 목록",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (detail.products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "등록된 상품이 없습니다.\n상품 추가 버튼을 눌러 추가해보세요!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            } else {
                detail.products.forEach { product ->
                    ProductAdminCard(
                        product = product,
                        onEditClick = { onEditClick(product) },
                        onDeleteClick = { onDeleteClick(product) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 요약 카드
// ──────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.5.dp, color.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            label,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}

// ──────────────────────────────────────────────────────────────
// 상품 카드 (관리자용)
// ──────────────────────────────────────────────────────────────

@Composable
private fun ProductAdminCard(
    product: ProductDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val (stockLabel, stockColor) = when {
        product.stockQuantity <= 0 -> "품절" to Color(0xFFEF4444)
        product.stockQuantity <= 10 -> "마감임박" to Color(0xFFF59E0B)
        else -> when (product.stockStatus?.uppercase()) {
            "SUFFICIENT", "IN_STOCK" -> "충분" to Color(0xFF10B981)
            "NORMAL"                 -> "보통" to Color(0xFFF59E0B)
            "SHORTAGE"               -> "부족" to Color(0xFFEF4444)
            else                     -> "미확인" to Color(0xFF94A3B8)
        }
    }

    val popularityColor = when (product.difficulty) {
        1    -> Color(0xFFEF4444)
        2    -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    val popularityLabel = when (product.difficulty) {
        1    -> "높음"
        2    -> "보통"
        else -> "낮음"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 상품 이미지 영역
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "상품 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text("🧸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "이미지 없음",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 상품 상세 정보 Column
            Column(modifier = Modifier.weight(1f)) {
                // 상품명 + 편집/삭제 버튼
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        product.itemName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .background(Color.White),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 수정 버튼
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 26.dp)
                                .background(Color(0xFFEFF6FF))
                                .clickable(onClick = onEditClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "수정",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        // 구분선
                        Box(
                            modifier = Modifier
                                .width(0.5.dp)
                                .height(16.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                        // 삭제 버튼
                        Box(
                            modifier = Modifier
                                .size(width = 30.dp, height = 26.dp)
                                .background(Color(0xFFFFF1F2))
                                .clickable(onClick = onDeleteClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 카테고리 + 태그
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    product.category?.let { cat ->
                        TagChip(text = cat, color = Color(0xFF6B4EFF))
                    }
                    val diffLabel = product.difficultyLabel ?: "인기도:${popularityLabel}"
                    TagChip(text = diffLabel, color = Color(0xFFF97316))
                    product.tags.take(3).forEach { tag ->
                        TagChip(text = "#${tag.name}", color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // 재고 / 가격 / 상태
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LabelValueColumn(label = "재고", value = "${product.stockQuantity}개")
                    LabelValueColumn(label = "가격", value = "${product.price}원")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("재고 상태", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            stockLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = stockColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("난이도", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(
                            popularityLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = popularityColor
                        )
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 상품 추가 폼 (API 연동 + 자동완성 검색 기능 탑재)
// ──────────────────────────────────────────────────────────────

@Composable
private fun AddProductForm(
    itemSearchResults: List<ItemDto>,
    isItemSearching: Boolean,
    onSearchItems: (String) -> Unit,
    onAddConfirm: (itemId: Long, itemName: String, category: String, price: Int, stockQuantity: Int, difficulty: Int, imageUrl: String?, tags: List<String>) -> Unit,
    onCancel: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<ItemDto?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("기타") }
    var stockQty by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("상") } // 상(1), 중(2), 하(3)
    var price by remember { mutableStateOf("") }
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>() }

    val difficulties = listOf("상", "중", "하")

    val context = androidx.compose.ui.platform.LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // 갤러리 이미지 선택 런처
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                    val file = java.io.File.createTempFile("PRODUCT_${timeStamp}_", ".jpg", storageDir)
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val fileProviderUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    imageUrl = fileProviderUri.toString()
                }
            } catch (e: Exception) {
                Log.e("ADD_PRODUCT_IMAGE", "갤러리 이미지 복사 에러", e)
                android.widget.Toast.makeText(context, "이미지를 가져오는 데 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카메라 촬영 런처
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            imageUrl = tempPhotoUri.toString()
        }
    }

    // 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri?.let { takePictureLauncher.launch(it) }
        } else {
            android.widget.Toast.makeText(context, "사진을 찍기 위해 카메라 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 띄우기 함수
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
            Log.e("ADD_PRODUCT_IMAGE", "카메라 실행 에러", e)
            android.widget.Toast.makeText(context, "카메라 실행 준비에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("사진 업로드", fontWeight = FontWeight.Bold) },
            text = { Text("사진을 가져올 방식을 선택해주세요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        launchCameraFlow()
                    }
                ) {
                    Text("카메라로 촬영", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        pickMediaLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text("갤러리에서 선택", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF7ED))
            .border(1.dp, Color(0xFFFBD38D), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            "새 상품 추가",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color(0xFF92400E)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 0. 상품 이미지
        Text("사진 *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { showImageSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "선택된 이미지",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    IconButton(
                        onClick = { imageUrl = "" },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "삭제",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = null,
                            tint = Color(0xFF4F46E5),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("사진 추가하기", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("클릭하여 이미지 업로드", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. 상품명 및 자동완성 검색어
        Text("상품 검색 *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = productName,
            onValueChange = {
                productName = it
                selectedItem = null
                onSearchItems(it)
            },
            placeholder = { Text("예: 피카츄 인형", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF97316),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // 자동완성 검색 결과 리스트 노출
        if (selectedItem == null && productName.isNotEmpty() && itemSearchResults.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    itemSearchResults.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedItem = item
                                    productName = item.name
                                    item.category?.let { cat ->
                                        category = when (cat.uppercase()) {
                                            "PLUSH" -> "인형"
                                            "FIGURE" -> "피규어"
                                            "GACHA" -> "가챠"
                                            "KEYRING" -> "키링"
                                            "SNACK" -> "간식"
                                            "ETC" -> "기타"
                                            else -> cat
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧸", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(item.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                item.category?.let {
                                    Text(it, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 재고 수량 + 난이도 (2열)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("재고 수량 *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = stockQty,
                    onValueChange = { stockQty = it },
                    placeholder = { Text("10", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("난이도 *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                var diffExpanded by remember { mutableStateOf(false) }
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = diffExpanded,
                    onExpandedChange = { diffExpanded = !diffExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = difficulty,
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF97316),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = diffExpanded,
                        onDismissRequest = { diffExpanded = false }
                    ) {
                        difficulties.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { difficulty = d; diffExpanded = false }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 카테고리 + 가격 (2열)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("카테고리 *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                var categoryExpanded by remember { mutableStateOf(false) }
                val categories = listOf("인형", "피규어", "가챠", "키링", "간식", "기타")
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF97316),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = { category = c; categoryExpanded = false }
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("가격 (원) *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    placeholder = { Text("1000", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. 태그 입력
        Text("태그", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = tagInput,
                onValueChange = { tagInput = it },
                placeholder = { Text("태그 입력 후 추가", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFF97316),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
            Button(
                onClick = {
                    val t = tagInput.trim().removePrefix("#")
                    if (t.isNotEmpty() && !tags.contains(t)) {
                        tags.add(t)
                        tagInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(" 추가", color = Color.White, fontSize = 12.sp)
            }
        }

        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tags.forEach { t ->
                    TagChip(text = "#$t", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. 등록 / 취소 버튼
        val isValid = productName.isNotBlank() && price.isNotBlank() && stockQty.isNotBlank()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("✕ 취소", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (isValid) {
                        val diffInt = when (difficulty) {
                            "상" -> 1
                            "중" -> 2
                            else -> 3
                        }
                        onAddConfirm(
                            if (selectedItem?.isMock == true) 0L else selectedItem?.id ?: 0L,
                            productName,
                            category,
                            price.toIntOrNull() ?: 0,
                            stockQty.toIntOrNull() ?: 0,
                            diffInt,
                            imageUrl.trim().ifEmpty { null },
                            tags.toList()
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("추가하기", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 상품 수정 다이얼로그 Composable
// ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProductDialog(
    product: ProductDto,
    onDismiss: () -> Unit,
    onConfirm: (price: Int, stockQuantity: Int, difficulty: Int, imageUrl: String?, tags: List<String>) -> Unit
) {
    var price by remember { mutableStateOf(product.price.toString()) }
    var stockQty by remember { mutableStateOf(product.stockQuantity.toString()) }
    var difficulty by remember {
        mutableStateOf(
            when (product.difficulty) {
                1    -> "상"
                2    -> "중"
                else -> "하"
            }
        )
    }
    var imageUrl by remember { mutableStateOf(product.imageUrl ?: "") }
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>().apply { addAll(product.tags.map { it.name }) } }

    val difficulties = listOf("상", "중", "하")

    val context = androidx.compose.ui.platform.LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // 갤러리 이미지 선택 런처
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(java.util.Date())
                    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                    val file = java.io.File.createTempFile("PRODUCT_EDIT_${timeStamp}_", ".jpg", storageDir)
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    val fileProviderUri = androidx.core.content.FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    imageUrl = fileProviderUri.toString()
                }
            } catch (e: Exception) {
                Log.e("EDIT_PRODUCT_IMAGE", "갤러리 이미지 복사 에러", e)
                android.widget.Toast.makeText(context, "이미지를 가져오는 데 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카메라 촬영 런처
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            imageUrl = tempPhotoUri.toString()
        }
    }

    // 카메라 권한 런처
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tempPhotoUri?.let { takePictureLauncher.launch(it) }
        } else {
            android.widget.Toast.makeText(context, "사진을 찍기 위해 카메라 권한이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 띄우기 함수
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
            Log.e("EDIT_PRODUCT_IMAGE", "카메라 실행 에러", e)
            android.widget.Toast.makeText(context, "카메라 실행 준비에 실패했습니다.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("사진 업로드", fontWeight = FontWeight.Bold) },
            text = { Text("사진을 가져올 방식을 선택해주세요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        launchCameraFlow()
                    }
                ) {
                    Text("카메라로 촬영", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImageSourceDialog = false
                        pickMediaLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text("갤러리에서 선택", color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(
                    "✏️ 상품 정보 수정",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    product.itemName,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 사진 수정
                Text("사진", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl.isNotBlank()) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "수정할 상품 이미지",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { imageUrl = "" },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "삭제",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_camera),
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("사진 수정하기", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Text("클릭하여 이미지 변경", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 가격 수정
                Text("가격 (원)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 재고 수량 수정
                Text("재고 수량", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = stockQty,
                    onValueChange = { stockQty = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 난이도 수정
                Text("인기도 (난이도)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                var diffExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = diffExpanded,
                    onExpandedChange = { diffExpanded = !diffExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = difficulty,
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = diffExpanded,
                        onDismissRequest = { diffExpanded = false }
                    ) {
                        difficulties.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { difficulty = d; diffExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 태그 수정
                Text("태그", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("태그 입력 후 추가", fontSize = 11.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val t = tagInput.trim().removePrefix("#")
                            if (t.isNotEmpty() && !tags.contains(t)) {
                                tags.add(t)
                                tagInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("추가")
                    }
                }

                if (tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { t ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("#$t", fontSize = 10.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "✕",
                                    fontSize = 10.sp,
                                    color = Color.Red,
                                    modifier = Modifier.clickable { tags.remove(t) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("취소", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            val diffInt = when (difficulty) {
                                "상" -> 1
                                "중" -> 2
                                else -> 3
                            }
                            onConfirm(
                                price.toIntOrNull() ?: 0,
                                stockQty.toIntOrNull() ?: 0,
                                diffInt,
                                imageUrl.trim().ifEmpty { null },
                                tags.toList()
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316))
                    ) {
                        Text("수정완료")
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
// 공통 UI 헬퍼
// ──────────────────────────────────────────────────────────────

@Composable
private fun TagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LabelValueColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "매장 관리 화면 - 데이터 있음")
@Composable
fun StoreAdminScreenPreview() {
    val dummyStore = StoreDetailDto(
        id = 368L,
        sourcePlaceId = "123",
        name = "강남 미니돌",
        type = "CLAW",
        latitude = 37.5665,
        longitude = 126.9780,
        distance = 0,
        address = "서울특별시 강남구 역삼동 123-45",
        contact = "02-1234-5678",
        businessHours = "10:00 - 22:00",
        mainImageUrl = null,
        kakaoDetailUrl = null
    )

    val dummyProducts = listOf(
        ProductDto(
            productId = 1L,
            itemId = 10L,
            itemName = "손오공 피규어",
            category = "FIGURE",
            price = 1000,
            inventoryMode = "QUANTITY",
            stockQuantity = 100,
            stockStatus = "IN_STOCK",
            difficulty = 1,
            difficultyLabel = "매우 쉬움",
            imageUrl = null,
            tags = listOf(TagDto(1L, "드래곤볼"), TagDto(2L, "피규어"))
        ),
        ProductDto(
            productId = 2L,
            itemId = 11L,
            itemName = "오리 인형",
            category = "PLUSH",
            price = 2000,
            inventoryMode = "QUANTITY",
            stockQuantity = 50,
            stockStatus = "IN_STOCK",
            difficulty = 2,
            difficultyLabel = "보통",
            imageUrl = null,
            tags = listOf(TagDto(3L, "오리"), TagDto(4L, "인형"))
        )
    )

    val dummyDetail = StoreDetailResponse(
        store = dummyStore,
        productCount = 2,
        totalStockQuantity = 150,
        tags = listOf(TagDto(1L, "드래곤볼"), TagDto(3L, "오리")),
        products = dummyProducts
    )

    PickitPickitTheme {
        StoreAdminContent(
            detail = dummyDetail,
            storeName = "강남 미니돌",
            onBackClick = {},
            itemSearchResults = emptyList(),
            isItemSearching = false,
            onSearchItems = {},
            onAddClick = { _, _, _, _, _, _, _, _ -> },
            onEditClick = {},
            onDeleteClick = {},
            onEditTagsClick = {}
        )
    }
}

// ──────────────────────────────────────────────────────────────
// 대표 태그 수정 다이얼로그 & 개별 삭제 Chip
// ──────────────────────────────────────────────────────────────

@Composable
fun EditStoreTagsDialog(
    initialTags: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var tagInput by remember { mutableStateOf("") }
    val tags = remember { mutableStateListOf<String>().apply { addAll(initialTags) } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "🏷️ 매장 대표 태그 수정",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 현재 태그 칩 리스트
                if (tags.isEmpty()) {
                    Text(
                        text = "등록된 태그가 없습니다. 아래에서 태그를 추가해보세요.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.forEach { t ->
                            DeletableTagChip(text = "#$t", onDelete = { tags.remove(t) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 태그 추가 입력 폼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { tagInput = it },
                        placeholder = { Text("예: 포켓몬, 가챠", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF97316),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val cleanTag = tagInput.trim().removePrefix("#")
                            if (cleanTag.isNotEmpty() && !tags.contains(cleanTag)) {
                                tags.add(cleanTag)
                                tagInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "추가",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("추가", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 취소 / 저장 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("취소", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            onConfirm(tags.toList())
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletableTagChip(
    text: String,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFF7ED),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFBD38D))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                color = Color(0xFF92400E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = Color(0xFFEF4444),
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onDelete() }
            )
        }
    }
}


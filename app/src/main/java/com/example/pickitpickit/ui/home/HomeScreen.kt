package com.example.pickitpickit.ui.home

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex


import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.pickitpickit.R
import com.example.pickitpickit.ui.map.MapCategory
import com.example.pickitpickit.ui.map.MapViewModel
import com.google.android.gms.location.LocationServices
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(mapViewModel: MapViewModel) {
    val selectedCategory by mapViewModel.selectedCategory.collectAsState()
    val isBottomSheetVisible by mapViewModel.isBottomSheetVisible.collectAsState()
    val searchQuery by mapViewModel.searchQuery.collectAsState()
    val recentSearches by mapViewModel.recentSearches.collectAsState()
    val registeredTags by mapViewModel.registeredTags.collectAsState()
    val context = LocalContext.current

    // 카테고리 + 검색어 통합 필터링
    val filteredStores = remember(searchQuery, selectedCategory) {
        mapViewModel.getFilteredStores()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var isSearchFocused by remember { mutableStateOf(false) }
    
    // 사용자가 입력 중인 검색어 상태 (실시간 검색 방지)
    var inputText by remember(searchQuery) { mutableStateOf(searchQuery) }
    val focusManager = LocalFocusManager.current

    // 검색창이 열려있을 때 뒤로 가기 버튼을 누르면 포커스만 해제
    BackHandler(enabled = isSearchFocused) {
        focusManager.clearFocus()
        isSearchFocused = false
    }

    // 카카오맵 인스턴스 보관
    var kakaoMapInstance by remember { mutableStateOf<KakaoMap?>(null) }
    var myLocationLabel by remember { mutableStateOf<com.kakao.vectormap.label.Label?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // 현재 위치로 카메라 이동 + 빨간 핀 마커 표시
    fun moveToCurrentLocation() {
        if (android.content.pm.PackageManager.PERMISSION_GRANTED ==
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng.from(it.latitude, it.longitude)

                    // 카메라 이동
                    kakaoMapInstance?.moveCamera(
                        CameraUpdateFactory.newCenterPosition(latLng, 15)
                    )

                    // 기존 마커 제거 후 새 마커 추가
                    // KakaoMap은 Vector Drawable을 직접 지원하지 않으므로 Bitmap 변환 필요
                    val markerBitmap = getBitmapFromDrawable(context, R.drawable.ic_my_location_marker)
                    val layer = kakaoMapInstance?.labelManager?.layer
                    myLocationLabel?.let { label -> layer?.remove(label) }
                    myLocationLabel = layer?.addLabel(
                        LabelOptions.from(latLng)
                            .setStyles(LabelStyle.from(markerBitmap))
                    )
                }
            }
        }
    }

    // ── 위치 권한 요청 ─────────────────────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) moveToCurrentLocation()
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // 지도 준비되면 현재 위치로 이동
    LaunchedEffect(kakaoMapInstance) {
        if (kakaoMapInstance != null) moveToCurrentLocation()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── 1. 카카오맵 ──────────────────────────────────────
        KakaoMapView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { kakaoMapInstance = it }
        )

        // ── 2. 상단 오버레이 ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1) 검색 영역 (투명해지지 않고 최상단 유지)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(2f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = inputText,
                        onQueryChange = { inputText = it },
                        onSearch = { 
                            mapViewModel.updateSearchQuery(inputText)
                            focusManager.clearFocus()
                        },
                        onClearQuery = { inputText = "" },
                        isFocused = isSearchFocused,
                        onFocusChanged = { isSearchFocused = it },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // 검색 실행 버튼 (우측 파란 박스)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B6EF8))
                            .clickable {
                                mapViewModel.updateSearchQuery(inputText)
                                focusManager.clearFocus()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location_arrow),
                            contentDescription = "검색",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 2) 하단 내용 (드롭다운 패널과 겹치는 Box)
                Box(modifier = Modifier.fillMaxWidth()) {
                    // 원래 하단 버튼들 (드롭다운이 열리면 30% 투명해짐)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isSearchFocused) 0.3f else 1f)
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        CategoryFilterRow(
                            selectedCategory = selectedCategory,
                            onCategorySelect = { mapViewModel.setCategory(it) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        StoreCountBadge(count = filteredStores.size)
                    }

                    // 드롭다운 패널 (버튼들 위로 덮으며 나타남)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isSearchFocused,
                        modifier = Modifier.zIndex(3f),
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        SearchDropdownPanel(
                            recentSearches = recentSearches,
                            registeredTags = registeredTags,
                            onSearchSelect = { query ->
                                mapViewModel.updateSearchQuery(query)
                                focusManager.clearFocus()
                            }
                        )
                    }
                }
            }
        }

        // ── 3. 우측 FAB ──────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MapFabButton(iconRes = R.drawable.ic_compass, contentDescription = "나침반")
            MapFabButton(
                iconRes = R.drawable.ic_my_location,
                contentDescription = "현재 위치",
                onClick = { moveToCurrentLocation() }
            )
        }

        // ── 4. 하단 내 주변 추천 버튼 ─────────────────
        NearbyRecommendButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            onClick = { mapViewModel.showBottomSheet() }
        )
    }

    // ── 5. Bottom Sheet ──────────────────────────────────────
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { mapViewModel.hideBottomSheet() },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            NearbyStoreBottomSheet(
                stores = filteredStores,
                onClose = { mapViewModel.hideBottomSheet() }
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 카카오맵 AndroidView 래핑
// ────────────────────────────────────────────────────────────
@Composable
fun KakaoMapView(
    modifier: Modifier = Modifier,
    onMapReady: (KakaoMap) -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef: MapView? = null

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.resume()
                Lifecycle.Event.ON_PAUSE  -> mapViewRef?.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        factory = { context ->
            MapView(context).also { mapView ->
                mapViewRef = mapView
                mapView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mapView.start(
                    object : MapLifeCycleCallback() {
                        override fun onMapDestroy() {}
                        override fun onMapError(e: Exception) {}
                    },
                    object : KakaoMapReadyCallback() {
                        override fun onMapReady(kakaoMap: KakaoMap) {
                            onMapReady(kakaoMap)
                        }
                    }
                )
            }
        },
        modifier = modifier
    )
}

// ────────────────────────────────────────────────────────────
// 검색바 (실제 TextField 구현)
// ────────────────────────────────────────────────────────────
@Composable
fun SearchBar(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onClearQuery: () -> Unit = {},
    isFocused: Boolean = false,
    onFocusChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(if (isFocused) 16.dp else 50.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "검색",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // 실제 입력 가능한 TextField
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
                .onFocusChanged { state -> onFocusChanged(state.isFocused) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = Color.DarkGray
            ),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "매장명, 주소, 태그 검색",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
                innerTextField()
            }
        )

        // X 버튼 (query 있을 때만 표시)
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "✕",
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onClearQuery() }
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 검색 드롭다운 패널
// ────────────────────────────────────────────────────────────
@Composable
fun SearchDropdownPanel(
    recentSearches: List<String>,
    registeredTags: List<String>,
    onSearchSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 최근 검색 Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF3B6EF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("최근 검색", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 최근 검색 리스트
            if (recentSearches.isEmpty()) {
                Text(
                    text = "최근 검색어가 없습니다.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 28.dp, top = 8.dp)
                )
            } else {
                recentSearches.forEach { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSearchSelect(search) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(search, fontSize = 15.sp, color = Color.DarkGray)
                }
                } // forEach 닫기
            } // else 닫기
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))
            
            // 내가 등록한 태그 Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFE5A000), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("내가 등록한 태그", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(12.dp))
            // 태그 리스트
            if (registeredTags.isEmpty()) {
                Text(
                    text = "등록한 태그가 없습니다.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 28.dp, top = 8.dp)
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    registeredTags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFFBE6), RoundedCornerShape(8.dp))
                            .clickable { onSearchSelect(tag) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(tag, fontSize = 13.sp, color = Color.DarkGray)
                    }
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────
// 카테고리 필터 Row
// ────────────────────────────────────────────────────────────
@Composable
fun CategoryFilterRow(
    selectedCategory: MapCategory,
    onCategorySelect: (MapCategory) -> Unit
) {
    val items = listOf(
        MapCategory.ALL to "≡ 전체",
        MapCategory.CLAW_MACHINE to "인형뽑기",
        MapCategory.GACHA to "가챠",
        MapCategory.MIXED to "복합"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (category, label) ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isSelected) Color(0xFF3B6EF8) else Color.White)
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color.DarkGray,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

// ────────────────────────────────────────────────────────────
// 주변 매장 수 배지
// ────────────────────────────────────────────────────────────
@Composable
fun StoreCountBadge(count: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "📍 주변 매장 ", fontSize = 13.sp, color = Color.DarkGray)
            Text(
                text = "${count}개",
                fontSize = 13.sp,
                color = Color(0xFF3B6EF8),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ────────────────────────────────────────────────────────────
// 우측 FAB 버튼
// ────────────────────────────────────────────────────────────
@Composable
fun MapFabButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = Color.DarkGray,
            modifier = Modifier.size(22.dp)
        )
    }
}

// ────────────────────────────────────────────────────────────
// 내 주변 추천 버튼
// ────────────────────────────────────────────────────────────
@Composable
fun NearbyRecommendButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF1A1A2E))
            .clickable { onClick() }
            .padding(horizontal = 32.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "내 주변 추천",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "∨", color = Color.White, fontSize = 13.sp)
        }
    }
}

// ────────────────────────────────────────────────────────────
// Bottom Sheet 내용
// ────────────────────────────────────────────────────────────
@Composable
fun NearbyStoreBottomSheet(
    stores: List<StoreItem>,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 핸들
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "주변 매장 ${stores.size}개",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stores) { store ->
                StoreListItem(store = store)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ────────────────────────────────────────────────────────────
// 매장 리스트 아이템
// ────────────────────────────────────────────────────────────
@Composable
fun StoreListItem(store: StoreItem) {
    val categoryLabel = when (store.category) {
        MapCategory.CLAW_MACHINE -> "인형뽑기"
        MapCategory.GACHA -> "가챠"
        MapCategory.MIXED -> "복합"
        MapCategory.ALL -> ""
    }
    val categoryColor = when (store.category) {
        MapCategory.CLAW_MACHINE -> Color(0xFFFF9500)
        MapCategory.GACHA -> Color(0xFF34C759)
        MapCategory.MIXED -> Color(0xFF5856D6)
        MapCategory.ALL -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일 (더미)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.TopStart
            ) {
                // 카테고리 뱃지
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(categoryColor)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(text = categoryLabel, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                    Text(
                        text = "${store.distanceMeters}m",
                        fontSize = 13.sp,
                        color = Color(0xFF3B6EF8),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "${store.rating}", fontSize = 12.sp, color = Color.DarkGray)
                    Text(text = " (${store.reviewCount})", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "📍 ${store.address}", fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "🕐 ${store.hours}", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    store.tags.take(3).forEach { tag ->
                        Text(
                            text = "#$tag",
                            fontSize = 11.sp,
                            color = Color(0xFF3B6EF8),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEEF3FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryLabel(category: MapCategory): String {
    return when (category) {
        MapCategory.ALL -> "전체"
        MapCategory.CLAW_MACHINE -> "인형뽑기"
        MapCategory.GACHA -> "가챠"
        MapCategory.MIXED -> "복합"
    }
}

// ────────────────────────────────────────────────────────────
// Vector Drawable → Bitmap 변환 (KakaoMap 마커용)
// ────────────────────────────────────────────────────────────
fun getBitmapFromDrawable(context: android.content.Context, drawableResId: Int): android.graphics.Bitmap {
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableResId)!!
    val bitmap = android.graphics.Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

// ────────────────────────────────────────────────────────────
// Previews
// ────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar()
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterRowPreview() {
    CategoryFilterRow(
        selectedCategory = MapCategory.ALL,
        onCategorySelect = {}
    )
}

@Preview(showBackground = true)
@Composable
fun StoreCountBadgePreview() {
    StoreCountBadge(count = 20)
}

@Preview(showBackground = true)
@Composable
fun NearbyRecommendButtonPreview() {
    NearbyRecommendButton(onClick = {})
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun StoreListItemPreview() {
    StoreListItem(store = dummyStores.first())
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
fun NearbyStoreBottomSheetPreview() {
    NearbyStoreBottomSheet(
        stores = dummyStores,
        onClose = {}
    )
}

package com.example.pickitpickit.ui.mypage

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pickitpickit.R
import com.example.pickitpickit.core.model.InterestTagResponse
import com.example.pickitpickit.ui.theme.PickitPickitTheme

@Composable
fun MyPageScreen(
    onLogoutClick: () -> Unit,
    myPageViewModel: MyPageViewModel = viewModel()
) {
    val uiState by myPageViewModel.uiState.collectAsState()
    var showProfileEdit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        myPageViewModel.loadUserProfile()
    }

    // 에러 다이얼로그
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = myPageViewModel::clearError,
            confirmButton = {
                TextButton(onClick = myPageViewModel::clearError) {
                    Text("확인", color = Color(0xFF5393FA), fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("알림", fontWeight = FontWeight.Bold) },
            text = { Text(uiState.errorMessage ?: "") }
        )
    }

    // 프로필 편집 화면 오버레이
    if (showProfileEdit) {
        ProfileEditScreen(
            uiState = uiState,
            onNicknameChange = myPageViewModel::updateEditNickname,
            onSelectImage = myPageViewModel::selectEditProfileImage,
            onToggleTag = myPageViewModel::toggleEditTag,
            onAddCustomTag = myPageViewModel::addCustomTag,
            onRemoveTag = myPageViewModel::removeSelectedTag,
            onCustomTagInputChange = myPageViewModel::updateCustomTagInput,
            onSave = {
                myPageViewModel.saveProfile {
                    showProfileEdit = false
                }
            },
            onCancel = {
                myPageViewModel.resetEditState()
                showProfileEdit = false
            }
        )
        return
    }

    MyPageScreenContent(
        uiState = uiState,
        onLogoutClick = onLogoutClick,
        onEditClick = {
            myPageViewModel.resetEditState()
            showProfileEdit = true
        },
        onDeleteAccountClick = {
            myPageViewModel.deleteAccount {
                // 회원 탈퇴 성공 시 로그아웃과 마찬가지로 Onboarding/Login 화면으로 전환
                onLogoutClick()
            }
        }
    )
}

@Composable
internal fun MyPageScreenContent(
    uiState: MyPageState,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPreferences = com.example.pickitpickit.GlobalApplication.userPreferences

    // 현재 알림 허용 여부 상태 (코루튼 재시작 시 자동 갱신)
    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    // 로컬 푸시 알림 설정 값 (기본값 true)
    val localPushEnabled by userPreferences.isPushNotificationsEnabled.collectAsState(initial = true)

    // 로컬 검색 반경 설정 값 (기본값 1000m = 1km)
    val searchRadius by userPreferences.searchRadius.collectAsState(initial = 1000)
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    // 최종 사용자 알림 활성화 여부
    val isNotificationsOn = notificationsEnabled && localPushEnabled

    // Android 13+ 시스템 알림 권한 요청 launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsEnabled = granted
        if (granted) {
            coroutineScope.launch {
                userPreferences.setPushNotificationsEnabled(true)
            }
        }
    }

    val backgroundBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF2ACAE7),
            Color(0xFF4D6FDF)
        )
    )

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── 상단 그라데이션 배너 ──────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = backgroundBrush)
                    .padding(top = 56.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_pikipiki),
                        contentDescription = "앱 로고",
                        modifier = Modifier
                            .width(120.dp)
                            .aspectRatio(1.5f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "앱 환경을 커스터마이징하세요",
                        color = Color(0xFF6B4B20).copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── 콘텐츠 영역 ───────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7FA))
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 100.dp)
            ) {

                // 프로필 섹션 레이블
                SectionLabel(text = "프로필")

                // 프로필 카드
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 원형 프로필 이미지
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F0FF))
                                .border(2.dp, Color(0xFF5393FA), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.profileImageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = uiState.profileImageUrl,
                                    contentDescription = "프로필 이미지",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF5393FA),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (uiState.isLoading) {
                                Text("불러오는 중...", color = Color.Gray, fontSize = 14.sp)
                            } else {
                                Text(
                                    text = uiState.nickname.ifEmpty { "닉네임 없음" },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1A2E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (!uiState.kakaoEmail.isNullOrEmpty()) {
                                    Text(
                                        text = uiState.kakaoEmail,
                                        fontSize = 12.sp,
                                        color = Color(0xFF6A7282),
                                        fontWeight = FontWeight.Medium
                                    )
                                } else if (!uiState.kakaoProfileImageUrl.isNullOrEmpty()) {
                                    Text(
                                        text = "카카오 계정 연동",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6A7282),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (uiState.selectedTags.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        uiState.selectedTags.forEach { tag ->
                                            TagChip(tag = tag)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 편집 버튼 (우측 상단)
                    TextButton(
                        onClick = onEditClick,
                        modifier = Modifier.align(Alignment.TopEnd),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "편집",
                            tint = Color(0xFF5393FA),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("편집", color = Color(0xFF5393FA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel(text = "계정")
                SettingsCard {
                    SettingsItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        iconTint = Color(0xFF5393FA),
                        title = "로그아웃",
                        subtitle = "계정에서 로그아웃",
                        onClick = onLogoutClick
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel(text = "위치 설정")
                SettingsCard {
                    val radiusSubtitle = when (searchRadius) {
                        500 -> "500미터 (500m)"
                        1000 -> "1 킬로미터 (1km)"
                        3000 -> "3 킬로미터 (3km)"
                        5000 -> "5 킬로미터 (5km)"
                        else -> "${searchRadius / 1000.0} 킬로미터"
                    }
                    SettingsItem(
                        iconRes = R.drawable.ic_distance,
                        iconTint = Color(0xFF5393FA),
                        title = "거리 단위",
                        subtitle = radiusSubtitle,
                        onClick = { showRadiusDialog = true }
                    )
                    HorizontalDivider(color = Color(0xFFF0F0F0), thickness = 0.7.dp)
                    SettingsItem(
                        iconRes = R.drawable.ic_location,
                        iconTint = Color(0xFF5393FA),
                        title = "자동 위치 갱신",
                        subtitle = "앱 실행 시 자동으로 현재 위치 불러오기"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel(text = "알림 설정")
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        iconTint = Color(0xFF5393FA),
                        title = "푸시 알림",
                        subtitle = if (isNotificationsOn) "켜짐" else "꺼짐",
                        onClick = {
                            val systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                            if (!systemEnabled) {
                                // 1. 시스템 권한이 비활성화 상태이면 권한 요청 또는 앱 설정창으로 유도
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            } else {
                                // 2. 시스템 권한이 활성화 상태이면, 로컬 푸시 알림 허용 여부를 반대로 토글!
                                coroutineScope.launch {
                                    userPreferences.setPushNotificationsEnabled(!localPushEnabled)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel(text = "화면 설정")
                SettingsCard {
                    SettingsItem(
                        iconRes = R.drawable.ic_tema,
                        iconTint = Color(0xFF5393FA),
                        title = "테마",
                        subtitle = "라이트 모드"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                SectionLabel(text = "앱 정보")
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = Color(0xFF5393FA),
                        title = "버전 정보",
                        subtitle = "1.0.0"
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFFFA726), Color(0xFFFFD54F))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "기본 설정 안내",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF4A2800)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("✓ 기본 검색 거리: 3km", fontSize = 12.sp, color = Color(0xFF5A3400))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("✓ 거리 필터는 0.5km ~ 10km까지 설정 가능합니다", fontSize = 12.sp, color = Color(0xFF5A3400))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_pikipiki),
                        contentDescription = "앱 로고",
                        modifier = Modifier
                            .width(90.dp)
                            .aspectRatio(1.5f)
                            .alpha(0.35f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("샒끄샘끊 - 인형뚝기 & 가차 찾기", fontSize = 11.sp, color = Color.Gray)
                    Text("© 2026 PikiPiki. All rights reserved.", fontSize = 10.sp, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444))
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "회원 탈퇴",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF5393FA))
            }
        }

        if (showRadiusDialog) {
            val options = listOf(
                500 to "500미터 (500m)",
                1000 to "1 킬로미터 (1km)",
                3000 to "3 킬로미터 (3km)",
                5000 to "5 킬로미터 (5km)"
            )

            AlertDialog(
                onDismissRequest = { showRadiusDialog = false },
                title = {
                    Text(
                        text = "검색 반경 설정",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A2E)
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        options.forEach { (value, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        coroutineScope.launch {
                                            userPreferences.setSearchRadius(value)
                                            showRadiusDialog = false
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (searchRadius == value),
                                    onClick = {
                                        coroutineScope.launch {
                                            userPreferences.setSearchRadius(value)
                                            showRadiusDialog = false
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5393FA))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1A1A2E),
                                    fontWeight = if (searchRadius == value) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRadiusDialog = false }) {
                        Text("닫기", color = Color(0xFF5393FA), fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = {
                    Text(
                        text = "회원 탈퇴",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A2E)
                    )
                },
                text = {
                    Text(
                        text = "정말로 회원 탈퇴를 하시겠습니까?\n\n탈퇴 시:\n- 작성하신 리뷰와 자랑하기 게시글은 삭제되지 않고 유지되지만 작성자 정보는 '탈퇴한 사용자'로 익명 처리됩니다.\n- 관심 태그, 관심 매장, 검색 기록 등 모든 설정은 완전히 파기됩니다.",
                        fontSize = 14.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteAccountClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("탈퇴하기", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = false },
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                    ) {
                        Text("취소", color = Color(0xFF4B5563))
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF888888),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(14.dp), spotColor = Color(0x0F000000))
            .background(Color.White, RoundedCornerShape(14.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    SettingsItemContent(
        iconPainter = androidx.compose.ui.graphics.vector.rememberVectorPainter(image = icon),
        iconTint = iconTint,
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
private fun SettingsItem(
    iconRes: Int,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    SettingsItemContent(
        iconPainter = painterResource(id = iconRes),
        iconTint = iconTint,
        title = title,
        subtitle = subtitle,
        onClick = onClick
    )
}

@Composable
private fun SettingsItemContent(
    iconPainter: androidx.compose.ui.graphics.painter.Painter,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color(0xFFEEF4FF), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1A1A2E))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF888888))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFBBBBBB),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TagChip(tag: InterestTagResponse) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFEF9C2), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFFFDF20), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "#${tag.name}",
            fontSize = 11.sp,
            color = Color(0xFF7A5500),
            fontWeight = FontWeight.Medium
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "마이페이지 - 데이터 로드 완료")
@Composable
fun MyPageScreenPreview() {
    PickitPickitTheme {
        val dummyState = MyPageState(
            nickname = "후훗나란남자란",
            profileImageUrl = null,
            profileImageType = "DEFAULT",
            selectedTags = listOf(
                InterestTagResponse(1L, "드래곤볼"),
                InterestTagResponse(2L, "포켓몬"),
                InterestTagResponse(3L, "디즈니")
            ),
            kakaoProfileImageUrl = null,
            isLoading = false
        )
        // ViewModel 없이 State만으로 UI 렌더링하는 Preview 전용 내부 Composable
        MyPageScreenContent(
            uiState = dummyState,
            onLogoutClick = {},
            onEditClick = {},
            onDeleteAccountClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "마이페이지 - 로딩 중")
@Composable
fun MyPageScreenLoadingPreview() {
    PickitPickitTheme {
        MyPageScreenContent(
            uiState = MyPageState(isLoading = true),
            onLogoutClick = {},
            onEditClick = {},
            onDeleteAccountClick = {}
        )
    }
}


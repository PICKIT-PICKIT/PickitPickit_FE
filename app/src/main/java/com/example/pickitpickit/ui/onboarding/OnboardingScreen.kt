package com.example.pickitpickit.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.pickitpickit.R
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.DefaultProfileImageResponse
import com.example.pickitpickit.core.model.InterestTagResponse

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onLogout: () -> Unit, // 🌟 유령 토큰 자동 로그아웃 정화 통로!
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    // 서버에 저장되어 있던 온보딩 완료 진척도 페이지 복원
    LaunchedEffect(uiState.initialPage) {
        if (uiState.initialPage > 0) {
            pagerState.scrollToPage(uiState.initialPage)
        }
    }

    // [🌟 초강력 수동 복원 동기화 장치]
    // 만약 이미 서버에 온보딩 정보가 완수된 사용자임이 식별되어 isCompleted가 true로 바뀌면, 
    // 로컬 Preferences 상태와 무관하게 즉각적으로 메인으로 넘겨 진입을 정상화합니다!
    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            onComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Text(
                    text = "프로필은 언제든지 설정에서 변경할 수 있습니다",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF5F8FF)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.logo_pikipiki),
                    contentDescription = "삐끼삐끼 로고",
                    modifier = Modifier
                        .width(161.dp)
                        .height(107.dp)
                )

                Text(
                    text = "프로필을 완성하고 서비스를 시작하세요!",
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                StepIndicator(currentStep = pagerState.currentPage)
                
                Spacer(modifier = Modifier.height(30.dp))
                
                Card(
                    modifier = Modifier
                        .width(328.dp)
                        .height(540.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                          0 -> NicknameStep(
                              nickname = uiState.nickname,
                              onNicknameChange = viewModel::updateNickname,
                              onRandomNickname = viewModel::generateRandomNickname,
                              onNext = {
                                  viewModel.saveNickname {
                                      coroutineScope.launch {
                                          pagerState.animateScrollToPage(1)
                                      }
                                  }
                              }
                          )
                          1 -> ProfileImageStep(
                              nickname = uiState.nickname,
                              selectedUrl = uiState.selectedProfileUrl,
                              kakaoProfileUrl = uiState.kakaoProfileUrl,
                              defaultImages = uiState.defaultProfileImages,
                              onSelectImage = viewModel::selectProfileImage,
                              onPrev = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                              onNext = {
                                  viewModel.saveProfileImage {
                                      coroutineScope.launch {
                                          pagerState.animateScrollToPage(2)
                                      }
                                  }
                              }
                          )
                          2 -> InterestTagStep(
                              selectedTagIds = uiState.selectedTagIds,
                              availableTags = uiState.availableTags,
                              onToggleTag = viewModel::toggleTag,
                              onPrev = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                              onComplete = {
                                  viewModel.saveInterestTagsAndComplete(onComplete)
                              }
                          )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 로딩바 인디케이터 오버레이
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {}, // 클릭 전파 방지
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6B4EFF))
            }
        }

        // 에러 메세지 알림창
        if (uiState.errorMessage != null) {
            val isUserNotFound = uiState.errorMessage == "USER_NOT_FOUND"
            val displayMessage = if (isUserNotFound) {
                "유효하지 않거나 만료된 로그인 세션입니다. 원활한 복구를 위해 다시 로그인을 시도해 주세요."
            } else {
                uiState.errorMessage ?: ""
            }
            
            AlertDialog(
                onDismissRequest = { 
                    viewModel.clearError() 
                    if (isUserNotFound) onLogout()
                },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.clearError() 
                        if (isUserNotFound) onLogout()
                    }) {
                        Text("확인", color = Color(0xFF6B4EFF), fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("안내", fontWeight = FontWeight.Bold) },
                text = { Text(displayMessage) }
            )
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    // Active 그라데이션 (현재 단계): #5754FF, #9DD2FB, #E8BE4B 삼색 선형 그라데이션!
    val activeBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF5754FF),
            Color(0xFF9DD2FB),
            Color(0xFFE8BE4B)
        )
    )
    
    // Completed 그라데이션 (완료 단계): #00C950, #009966 이색 선형 그라데이션!
    val completedBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF00C950),
            Color(0xFF009966)
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0..2) {
            val isActive = i == currentStep
            val isCompleted = i < currentStep
            
            // 서클 그리기 (지름 44dp)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = when {
                            isCompleted -> completedBrush
                            isActive -> activeBrush
                            else -> Brush.linearGradient(colors = listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    // 완료 단계는 제공된 ic_check.xml 리소스를 연동하여 초록 체크 엠블럼으로 채움!
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "완료",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "${i + 1}",
                        color = if (isActive) Color.White else Color(0xFF64748B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 구분선: 양 끝이 정교하게 둥글고 도톰한 6dp 둥근 라인 바(Bar) 형태
            if (i < 2) {
                val isLineCompleted = i < currentStep
                Spacer(modifier = Modifier.width(7.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            color = if (isLineCompleted) Color(0xFF00C950) else Color(0xFFE2E8F0)
                        )
                )
                Spacer(modifier = Modifier.width(7.dp))
            }
        }
    }
}

@Composable
fun NicknameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onRandomNickname: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // 🌟 카드 세로 중심부에 모든 요소를 단정하고 예쁘게 모아 정렬!
    ) {
        // 타이틀이 카드 맨 위에 딱 붙지 않도록 예쁜 상단 숨구멍 여백 부여
        Spacer(modifier = Modifier.height(10.dp))
        
        Text("닉네임을 설정해주세요.", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("다른 사용자들에게 보여질 이름입니다.", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("닉네임 (2~10자)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6B4EFF),
                unfocusedBorderColor = Color.LightGray
            )
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("${nickname.length}/10자", color = Color.Gray, fontSize = 12.sp)
            TextButton(onClick = onRandomNickname) {
                Text("🎲 랜덤 생성", color = Color(0xFF6B4EFF), fontSize = 12.sp)
            }
        }
        
        // 🌟 상하로 극단적으로 밀어내던 weight(1f) 대신, 피그마 비율에 입각한 정밀 24dp 간격 여백 배치!
        Spacer(modifier = Modifier.height(24.dp))
        
        // 피그마 맞춤형 고품격 연파랑 알림 박스 (Box & CenterStart 적용으로 세로 정중앙 정렬 완벽화!)
        Box(
            modifier = Modifier
                .border(
                    width = 1.5.dp, 
                    color = Color(0xFFBEDBFF), 
                    shape = RoundedCornerShape(14.dp)
                )
                .width(308.dp)
                .height(35.dp)
                .background(
                    color = Color(0xFFEFF6FF), 
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 17.dp),
            contentAlignment = Alignment.CenterStart // 🌟 텍스트를 위아래 쏠림 없이 정확히 세로 정중앙에 배치!
        ) {
            Text(
                text = "• 닉네임은 나중에 설정에서 변경할 수 있어요!",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 20.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF1C398E)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 다음 단계 선형 그라데이션 적용 브러시 (#4F39F6 ~ #9810FA)
        val isEnabled = nickname.length >= 2
        val buttonGradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4F39F6),
                Color(0xFF9810FA)
            )
        )
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    brush = if (isEnabled) buttonGradient else Brush.linearGradient(colors = listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0))),
                    shape = RoundedCornerShape(25.dp)
                ),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // 🌟 배경을 투명하게 뚫어 그라데이션이 아름답게 비치도록 유도!
                disabledContainerColor = Color.Transparent
            ),
            enabled = isEnabled,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "다음 단계 →", 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun ProfileImageStep(
    nickname: String,
    selectedUrl: String?,
    kakaoProfileUrl: String?,
    defaultImages: List<DefaultProfileImageResponse>,
    onSelectImage: (type: String, url: String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    // 현재 최종적으로 대표로 보여줄 이미지 (선택한 게 있다면 selectedUrl, 없으면 기본 이미지 1번)
    val displayRepresentativeUrl = selectedUrl ?: defaultImages.firstOrNull()?.imageUrl ?: ""
    val isKakaoSelected = !kakaoProfileUrl.isNullOrEmpty() && selectedUrl == kakaoProfileUrl

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 헤더 카메라 배너 (보라-핑크 그라데이션 배경)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color(0xFFC026D3), Color(0xFF7C3AED))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "프로필 설정 카메라",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("프로필 이미지 선택", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(4.dp))
        Text("원하는 프로필 이미지를 선택해주세요", color = Color.Gray, fontSize = 13.sp)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 2. 대형 대표 프로필 영역 (그라데이션 테두리 링 + 그린 체크 뱃지)
        Box(
            modifier = Modifier.size(130.dp),
            contentAlignment = Alignment.Center
        ) {
            // 그라데이션 보더 백그라운드 링
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFEC4899))
                        )
                    )
                    .padding(3.dp) // 테두리 굵기용 패딩
                    .background(Color.White, CircleShape)
                    .padding(3.dp) // 이미지와 테두리 사이 갭 패딩
            ) {
                AsyncImage(
                    model = displayRepresentativeUrl,
                    contentDescription = "선택된 대표 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            
            // 우측 하단 그린 체크 뱃지
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 닉네임 텍스트 표시
        Text(
            text = nickname.ifEmpty { "카카오 사용자" },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 3. 기본 프로필 격자 (3x2 칩 배열)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val chunkedImages = defaultImages.chunked(3)
            chunkedImages.forEach { rowList ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowList.forEach { img ->
                        val isThisSelected = selectedUrl == img.imageUrl
                        
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .clickable { onSelectImage("DEFAULT", img.imageUrl) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = img.imageUrl,
                                contentDescription = "기본 프로필 칩",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            // 선택 시 딤드 레이어 + 흰 체크
                            if (isThisSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 4. 구분선 & 카카오톡 대체 설정 카드
        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE2E8F0),
            thickness = 1.dp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "또는 다른 방법으로 이미지 설정하기",
            color = Color(0xFF475569),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // 카카오 프로필 카드 (치수가 정수로 정돈된 고품격 정사각형 연노랑 카드)
        Box(
            modifier = Modifier
                .width(137.dp)
                .height(131.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFFFDF0)) // 연노랑 배경
                .border(
                    width = if (isKakaoSelected) 2.dp else 1.5.dp,
                    color = if (isKakaoSelected) Color(0xFFF0B100) else Color(0xFFFFDF20),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(enabled = !kakaoProfileUrl.isNullOrEmpty()) {
                    kakaoProfileUrl?.let { onSelectImage("KAKAO", it) }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 노란색 원형 아이콘 엠블럼 (지름 48dp, 좌우 대칭 12dp 패딩 정교 매핑)
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .background(color = Color(0xFFF0B100), shape = CircleShape)
                        .padding(start = 12.dp, end = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "카카오 로고",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "카카오 프로필",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (!kakaoProfileUrl.isNullOrEmpty()) "현재 프로필 그대로" else "연결된 프로필 없음",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // 우측 상단 소형 체크박스 배지
            if (isKakaoSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0B100)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // 5. 하단 액션 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("이전", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
            
            // 다음 단계 그라데이션 디자인 버튼
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color(0xFFC026D3), Color(0xFF7C3AED))
                        )
                    ),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                enabled = selectedUrl != null
            ) {
                Text("다음 단계 →", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestTagStep(
    selectedTagIds: Set<Long>,
    availableTags: List<InterestTagResponse>,
    onToggleTag: (Long) -> Unit,
    onPrev: () -> Unit,
    onComplete: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. 선형 그라데이션 헤더 원형 배경 (#E60076 ~ #EC003F)
        val tagHeaderGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFE60076), Color(0xFFEC003F))
        )
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(tagHeaderGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tag),
                contentDescription = "관심 태그",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text("관심 태그 선택", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(4.dp))
        Text("좋아하는 캐릭터나 브랜드를 선택해주세요 \n(최소 1개)", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // 2. "X개 선택됨" 러블리 핑크 그라데이션 스테이터스 플레이트
        val statusBgGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFFDF2F8), Color(0xFFFFF1F2))
        )
        Box(
            modifier = Modifier
                .width(285.dp)
                .height(58.dp)
                .border(
                    width = 1.5.dp,
                    color = Color(0xFFFCCEE8),
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    brush = statusBgGradient,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${selectedTagIds.size}개 선택됨",
                color = Color(0xFFE60076),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // 3. 태그 리스트 영역
        if (availableTags.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE60076))
            }
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableTags.forEach { tag ->
                    val isSelected = selectedTagIds.contains(tag.id)
                    val tagGradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFE60076), Color(0xFFEC003F))
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = if (isSelected) tagGradient else Brush.linearGradient(colors = listOf(Color.White, Color.White)),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.50314.dp, // 🌟 피그마 지정 보더 두께 보존
                                color = if (isSelected) Color.Transparent else Color(0xFFD1D5DC), // 🌟 피그마 지정 미선택 보더 색상 보존
                                shape = RoundedCornerShape(8.dp) // 🌟 피그마 지정 둥글기 8.dp 보존
                            )
                            .clickable { onToggleTag(tag.id) }
                            .padding(horizontal = 12.dp, vertical = 7.dp) // 🌟 텍스트 가독성을 최대로 높이는 꿀패딩!
                    ) {
                        Text(
                            text = "#${tag.name}",
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            fontSize = 13.sp, // 🌟 태그가 잘리지 않도록 13.sp로 시원하고 우아하게 표현!
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        // 4. "Tip" 유의사항 알림 박스 (Figma width 285dp x height 74dp 고정)
        Box(
            modifier = Modifier
                .border(
                    width = 1.5.dp, 
                    color = Color(0xFFBEDBFF), 
                    shape = RoundedCornerShape(14.dp)
                )
                .width(285.dp)
                .height(74.dp) // 🌟 유저 지정 74dp 고정 높이!
                .background(
                    color = Color(0xFFEFF6FF), 
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            val tipAnnotatedText = buildAnnotatedString {
                append("💡 ")
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C398E)
                    )
                ) {
                    append("Tip:") // 🌟 "Tip:" 글자만 굵게 볼드 처리!
                }
                append(" 관심 태그는 맞춤 추천에 사용되며,\n나중에 변경할 수 있어요!")
            }
            Text(
                text = tipAnnotatedText,
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 11.5.sp, // 74dp 내에서 2줄 텍스트가 위아래 이쁘게 채워지도록 스케일 보정
                    lineHeight = 18.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF1C398E)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        // 5. 하단 액션 버튼 그룹 (완료 버튼 그라데이션 + 뒤쪽 체크 아이콘)
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("이전", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
            }
            
            val isCompleteEnabled = selectedTagIds.isNotEmpty()
            val completeGradient = Brush.linearGradient(
                colors = listOf(Color(0xFFE60076), Color(0xFFEC003F))
            )
            
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .background(
                        brush = if (isCompleteEnabled) completeGradient else Brush.linearGradient(colors = listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0))),
                        shape = RoundedCornerShape(25.dp)
                    ),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                enabled = isCompleteEnabled,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "완료하고 시작하기",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleteEnabled) Color.White else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "완료 체크",
                        tint = if (isCompleteEnabled) Color.White else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp)) // 🌟 롱 스크롤을 시원하게 보장해 주는 하단 여백 추가!
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    PickitPickitTheme {
        OnboardingScreen(onComplete = {}, onLogout = {})
    }
}

@Preview(showBackground = true, name = "1단계: 닉네임 입력")
@Composable
fun NicknameStepPreview() {
    PickitPickitTheme {
        NicknameStep(
            nickname = "홍길동",
            onNicknameChange = {},
            onRandomNickname = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, name = "2단계: 프로필 선택")
@Composable
fun ProfileImageStepPreview() {
    PickitPickitTheme {
        ProfileImageStep(
            nickname = "카카오 사용자",
            selectedUrl = null,
            kakaoProfileUrl = "https://example.com/kakao.jpg",
            defaultImages = listOf(
                DefaultProfileImageResponse("1", "https://example.com/1.jpg"),
                DefaultProfileImageResponse("2", "https://example.com/2.jpg"),
                DefaultProfileImageResponse("3", "https://example.com/3.jpg")
            ),
            onSelectImage = { _, _ -> },
            onPrev = {},
            onNext = {}
        )
    }
}

@Preview(showBackground = true, name = "3단계: 태그 선택")
@Composable
fun InterestTagStepPreview() {
    PickitPickitTheme {
        InterestTagStep(
            selectedTagIds = setOf(1L, 2L),
            availableTags = listOf(
                InterestTagResponse(1L, "산리오"),
                InterestTagResponse(2L, "포켓몬"),
                InterestTagResponse(3L, "짱구"),
                InterestTagResponse(4L, "커비")
            ),
            onToggleTag = {},
            onPrev = {},
            onComplete = {}
        )
    }
}

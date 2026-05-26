package com.example.pickitpickit.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                        .padding(bottom = 24.dp)
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
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .weight(1f),
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
                Spacer(modifier = Modifier.height(30.dp))
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
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("확인", color = Color(0xFF6B4EFF), fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("안내", fontWeight = FontWeight.Bold) },
                text = { Text(uiState.errorMessage ?: "") }
            )
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0..2) {
            val isCompleted = i <= currentStep
            val color = if (isCompleted) Color(0xFF6B4EFF) else Color(0xFFE0E0E0)
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                if (i < currentStep) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(text = "${i + 1}", color = if (isCompleted) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
            
            if (i < 2) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .background(color)
                )
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
            .padding(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("닉네임을 설정해주세요.", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("다른 사용자들에게 보여질 이름입니다.", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF)),
            enabled = nickname.length >= 2
        ) {
            Text("다음 단계 →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileImageStep(
    selectedUrl: String?,
    kakaoProfileUrl: String?,
    defaultImages: List<DefaultProfileImageResponse>,
    onSelectImage: (type: String, url: String) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    // 렌더링에 사용할 임시 리스트 조립 (카카오 이미지 정보가 있으면 첫 번째 배치)
    val displayList = remember(kakaoProfileUrl, defaultImages) {
        val list = mutableListOf<Pair<String, String>>() // Pair(Type, Url)
        if (!kakaoProfileUrl.isNullOrEmpty()) {
            list.add(Pair("KAKAO", kakaoProfileUrl))
        }
        defaultImages.forEach {
            list.add(Pair("DEFAULT", it.imageUrl))
        }
        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("프로필 이미지 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("원하는 프로필 이미지를 선택해주세요", color = Color.Gray, fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (displayList.isEmpty()) {
            // 데이터 로드 전/실패 시 더미 표시
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6B4EFF))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayList.size) { index ->
                    val (type, url) = displayList[index]
                    val isSelected = selectedUrl == url
                    
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable { onSelectImage(type, url) },
                        contentAlignment = Alignment.Center
                    ) {
                        // 실제 동그란 프로필 이미지 영역
                        Box(
                            modifier = Modifier
                                .fillMaxSize(0.85f) // 체크박스/배지가 들어갈 공간을 확보하기 위해 약간의 여백 확보
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.2f))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF6B4EFF) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = if (type == "KAKAO") "카카오 프로필" else "기본 프로필",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                        
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6B4EFF))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                        
                        // 카카오 이미지 상단 조그만 뱃지 표시
                        if (type == "KAKAO") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .background(Color(0xFFFEE500), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("Talk", color = Color(0xFF3C1E1E), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("이전", color = Color.Black)
            }
            Button(
                onClick = onNext,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF)),
                enabled = selectedUrl != null
            ) {
                Text("다음 단계 →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("관심 태그 선택", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("좋아하는 캐릭터나 브랜드를 선택해주세요\n(최소 1개)", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEEF2FF))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("${selectedTagIds.size}개 선택됨", color = Color(0xFF6B4EFF), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (availableTags.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF6B4EFF))
            }
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTags.forEach { tag ->
                    val isSelected = selectedTagIds.contains(tag.id)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF6B4EFF) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color.LightGray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onToggleTag(tag.id) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(tag.name, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("이전", color = Color.Black)
            }
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF)),
                enabled = selectedTagIds.isNotEmpty()
            ) {
                Text("완료하고 시작하기 ✓", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    PickitPickitTheme {
        OnboardingScreen(onComplete = {})
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
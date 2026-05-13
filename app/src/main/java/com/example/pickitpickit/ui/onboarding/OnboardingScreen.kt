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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

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
                    .height(107.dp) // 높이를 지정하여 상단 여백 보장
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
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            }
                        )
                        1 -> ProfileImageStep(
                            selectedId = uiState.selectedProfileId,
                            onSelectImage = viewModel::selectProfileImage,
                            onPrev = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                            onNext = { coroutineScope.launch { pagerState.animateScrollToPage(2) } }
                        )
                        2 -> InterestTagStep(
                            selectedTags = uiState.selectedTags,
                            onToggleTag = viewModel::toggleTag,
                            onPrev = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                            onComplete = onComplete
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
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
        Text("닉네임을 설정해주세요.", fontSize = 29.sp, fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
    selectedId: Int?,
    onSelectImage: (Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val sampleImages = listOf(
        Color.Red, Color.Blue, Color.Green, 
        Color.Yellow, Color.Cyan, Color.Magenta,
        Color.Gray, Color.Black, Color(0xFFFFA500)
    )
    
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
        
        // Profiles grid placeholder
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f) // 고정 높이 대신 weight(1f)를 사용하여 남은 공간 활용
        ) {
            items(sampleImages.size) { index ->
                val color = sampleImages[index]
                val isSelected = selectedId == index
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.5f))
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) Color(0xFF6B4EFF) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelectImage(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6B4EFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp)) // 그리드와 버튼 사이 여백
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("이전", color = Color.Black)
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A7B)) // 카카오 버튼 컬러 등 임시
            ) {
                Text("다음 단계 →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestTagStep(
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    onPrev: () -> Unit,
    onComplete: () -> Unit
) {
    val sampleTags = listOf("포켓몬", "디즈니", "원피스", "산리오", "마블", "BT21", "짱구", "팬텀", "귀멸의칼날", "나루토", "메이플스토리", "카카오프렌즈", "지브리")
    
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF0F5)).padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("${selectedTags.size}개 선택됨", color = Color(0xFFFF2A7B), fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sampleTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) Color(0xFFFF2A7B) else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else Color.LightGray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggleTag(tag) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tag, color = if (isSelected) Color.White else Color.DarkGray, fontSize = 14.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrev,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("이전", color = Color.Black)
            }
            Button(
                onClick = onComplete,
                modifier = Modifier.weight(2f).height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A7B)),
                enabled = selectedTags.isNotEmpty()
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
        // ViewModel 기본값이 선언되어 있어 Preview 환경에서도 인스턴스가 자동으로 생성됩니다.
        OnboardingScreen(onComplete = {})
    }
}

@Preview(showBackground = true)
@Composable
fun NicknameStepPreview() {
    PickitPickitTheme {
        Box(modifier = Modifier.background(Color.White).fillMaxWidth().height(400.dp)) {
            NicknameStep(
                nickname = "홍길동",
                onNicknameChange = {},
                onRandomNickname = {},
                onNext = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileImageStepPreview() {
    PickitPickitTheme {
        Box(modifier = Modifier.background(Color.White).fillMaxWidth().height(500.dp)) {
            ProfileImageStep(
                selectedId = 1,
                onSelectImage = {},
                onPrev = {},
                onNext = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InterestTagStepPreview() {
    PickitPickitTheme {
        Box(modifier = Modifier.background(Color.White).fillMaxWidth().height(500.dp)) {
            InterestTagStep(
                selectedTags = setOf("포켓몬", "디즈니", "기동전사 건담"),
                onToggleTag = {},
                onPrev = {},
                onComplete = {}
            )
        }
    }
}

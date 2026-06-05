package com.example.pickitpickit.ui.mypage

import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.BragDto

@Composable
fun MyBragsScreen(
    onBackClick: () -> Unit,
    viewModel: MyBragsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var bragToDelete by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserBrags()
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

    // 게시물 삭제 확인 다이얼로그
    if (bragToDelete != null) {
        AlertDialog(
            onDismissRequest = { bragToDelete = null },
            title = { Text("게시글 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("작성하신 자랑하기 게시글을 정말로 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        bragToDelete?.let { viewModel.deleteBrag(it) }
                        bragToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { bragToDelete = null },
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
            // ── 상단 노란색/주황색 그라데이션 타이틀 바 ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFFA726), Color(0xFFFFD54F))
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
                        tint = Color(0xFF4A2800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "뒤로 가기",
                        color = Color(0xFF4A2800),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 타이틀 & 서브타이틀
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "내가 작성한 자랑하기",
                        color = Color(0xFF4A2800),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "총 ${uiState.totalBragsCount}개의 게시물",
                        color = Color(0xFF5A3400).copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── 자랑하기 목록 영역 ───────────────────────────────────
            if (uiState.paginatedBrags.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📸", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "작성하신 자랑 게시글이 없습니다.",
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
                    items(uiState.paginatedBrags) { brag ->
                        MyBragCard(
                            brag = brag,
                            onDeleteClick = { bragToDelete = brag.bragId },
                            onShareClick = {
                                val (title, description, storeName, tags) = parseBragContent(brag.content)
                                val shareText = buildString {
                                    append("🎉 [PikiPiki] 내가 뽑은 인형 자랑하기!\n\n")
                                    append("제목: $title\n")
                                    if (!description.isNullOrEmpty()) {
                                        append("설명: $description\n")
                                    }
                                    if (!storeName.isNullOrEmpty()) {
                                        append("📍 위치: $storeName\n")
                                    }
                                    if (tags.isNotEmpty()) {
                                        append("\n")
                                        append(tags.joinToString(" ") { "#$it" })
                                        append("\n")
                                    }
                                    append("\n앱에서 자랑글 확인하기: https://pickit-pickit.site/brags/${brag.bragId}")
                                }

                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "자랑글 공유하기"))
                            }
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
                CircularProgressIndicator(color = Color(0xFFFFA726))
            }
        }
    }
}

@Composable
private fun MyBragCard(
    brag: BragDto,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val (title, description, storeName, tags) = parseBragContent(brag.content)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .border(1.2.dp, Color(0xFFFFE082), RoundedCornerShape(16.dp)), // 노란색 계열 테두리
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 작성자 프로필 헤더 및 삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 프로필 아바타
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
                    val initial = brag.authorNickname.firstOrNull()?.toString() ?: "카"
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9C27B0)), // 보라색 아바타 기본값
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 닉네임 & 상대 시간
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = brag.authorNickname,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = getRelativeTimeString(brag.createdAt),
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }

                // 삭제 버튼
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_delete),
                        contentDescription = "게시글 삭제",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 자랑 이미지 (1:1 비율)
            AsyncImage(
                model = brag.imageUrl,
                contentDescription = "자랑 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 상호작용 바 (댓글, 공유하기)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 댓글
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = com.example.pickitpickit.R.drawable.ic_comment),
                        contentDescription = "댓글",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0",
                        fontSize = 13.sp,
                        color = Color(0xFF555555),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // 공유하기
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "공유하기",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 자랑글 제목
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )

            // 설명 (본문)
            if (!description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )
            }

            // 위치 링크
            if (!storeName.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF3B6EF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = storeName,
                        fontSize = 12.sp,
                        color = Color(0xFF3B6EF8),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 태그 리스트
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

// ──────────────────────────────────────────────────────────────
// 헬퍼: 자랑글 content 파싱 함수 (StoreDetailScreen과 동일)
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

private data class Tuple4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

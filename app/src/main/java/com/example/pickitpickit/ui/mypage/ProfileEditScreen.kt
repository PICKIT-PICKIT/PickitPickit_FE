package com.example.pickitpickit.ui.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.pickitpickit.core.model.DefaultProfileImageResponse
import com.example.pickitpickit.core.model.InterestTagResponse
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import com.example.pickitpickit.ui.theme.Variables

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileEditScreen(
    uiState: MyPageState,
    onNicknameChange: (String) -> Unit,
    onSelectImage: (type: String, url: String) -> Unit,
    onToggleTag: (Long) -> Unit,
    onAddCustomTag: (String) -> Unit,
    onRemoveTag: (Long) -> Unit,
    onCustomTagInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scrollState = rememberScrollState()
    val imageScrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            // ── 상단 헤더 ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF5393FA), Color(0xFF7B6EF6))
                        )
                    )
                    .padding(top = 52.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "프로필",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // ── 닉네임 섹션 ───────────────────────────────────────
                EditSectionLabel(text = "닉네임 (2~10자)")

                OutlinedTextField(
                    value = uiState.editNickname,
                    onValueChange = onNicknameChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Variables.Blue500,
                        unfocusedBorderColor = Color(0xFFDDE3F0)
                    )
                )
                Text(
                    text = "${uiState.editNickname.length}/10자",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── 프로필 이미지 섹션 ────────────────────────────────
                EditSectionLabel(text = "프로필 이미지")

                // 대형 현재 선택 이미지 중앙 표시
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .border(3.dp, Variables.Blue500, CircleShape)
                        .background(Color(0xFFE8F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.editProfileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = uiState.editProfileImageUrl,
                            contentDescription = "선택된 프로필",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Variables.Blue500,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 가로 스크롤 이미지 썸네일 목록
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(imageScrollState),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 카카오 이미지가 있는 경우 표시
                    if (!uiState.kakaoProfileImageUrl.isNullOrEmpty()) {
                        val isSelected = uiState.editProfileImageUrl == uiState.kakaoProfileImageUrl
                        ProfileImageThumb(
                            imageUrl = uiState.kakaoProfileImageUrl!!,
                            isSelected = isSelected,
                            label = "카카오",
                            onClick = { onSelectImage("KAKAO", uiState.kakaoProfileImageUrl!!) }
                        )
                    }
                    uiState.defaultProfileImages.forEach { img ->
                        val isSelected = uiState.editProfileImageUrl == img.imageUrl
                        ProfileImageThumb(
                            imageUrl = img.imageUrl,
                            isSelected = isSelected,
                            onClick = { onSelectImage("DEFAULT", img.imageUrl) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 앨범에서 가져오기 버튼 (추후 구현)
                OutlinedButton(
                    onClick = { /* TODO: 갤러리 선택 구현 */ },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("앨범에서 가져오기", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── 관심 태그 섹션 ────────────────────────────────────
                val selectedTagIds = uiState.editSelectedTagIds
                val allTags = uiState.availableTags
                val selectedTagObjects = allTags.filter { it.id in selectedTagIds }

                EditSectionLabel(text = "관심 태그 (1개 선택필)")

                // 태그 직접 입력
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.customTagInput,
                        onValueChange = onCustomTagInputChange,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        placeholder = { Text("직접 입력 (10자 이하)", fontSize = 13.sp, color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Variables.Blue500,
                            unfocusedBorderColor = Color(0xFFDDE3F0)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAddCustomTag(uiState.customTagInput) },
                        enabled = uiState.customTagInput.trim().length in 1..10,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE500)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF1A1A1A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("추가", color = Color(0xFF1A1A1A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 내 태그 (선택된 태그들)
                if (selectedTagObjects.isNotEmpty()) {
                    Text("내 태그", fontSize = 12.sp, color = Color(0xFF888888), fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF9E6), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedTagObjects.forEach { tag ->
                                SelectedTagChip(
                                    tagName = tag.name,
                                    onRemove = { onRemoveTag(tag.id) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 추천 태그
                Text("추천 태그", fontSize = 12.sp, color = Color(0xFF888888), fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allTags.forEach { tag ->
                            val isSelected = tag.id in selectedTagIds
                            RecommendedTagChip(
                                tagName = tag.name,
                                isSelected = isSelected,
                                onClick = { onToggleTag(tag.id) }
                            )
                        }
                    }
                }
            }
        }

        // ── 하단 고정 취소/저장 버튼 ──────────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text("취소", fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = onSave,
                enabled = !uiState.isLoading && uiState.editNickname.length >= 2,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Variables.Blue500)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("저장", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun EditSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF555555),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProfileImageThumb(
    imageUrl: String,
    isSelected: Boolean,
    label: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(if (isSelected) 3.dp else 1.5.dp, if (isSelected) Variables.Blue500 else Color.Transparent, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = label,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun SelectedTagChip(tagName: String, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFEE500), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#$tagName", fontSize = 12.sp, color = Color(0xFF4A3000), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = Color(0xFF8A6000),
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
private fun RecommendedTagChip(tagName: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFFE8EEFF) else Color.White
    val borderColor = if (isSelected) Variables.Blue500 else Color(0xFFDDE3F0)
    val textColor = if (isSelected) Variables.Blue500 else Color(0xFF555555)

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "#$tagName",
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Previews
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "프로필 편집 - 데이터 로드 완료")
@Composable
fun ProfileEditScreenPreview() {
    PickitPickitTheme {
        val dummyImages = listOf(
            DefaultProfileImageResponse("1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80"),
            DefaultProfileImageResponse("2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"),
            DefaultProfileImageResponse("3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80")
        )
        val dummyTags = listOf(
            InterestTagResponse(1L, "포켓몬"),
            InterestTagResponse(2L, "디즈니"),
            InterestTagResponse(3L, "마블"),
            InterestTagResponse(4L, "짱구"),
            InterestTagResponse(5L, "지브리")
        )
        val dummyState = MyPageState(
            editNickname = "후훗나란남자란",
            editProfileImageUrl = dummyImages.first().imageUrl,
            editProfileImageType = "DEFAULT",
            editSelectedTagIds = setOf(1L, 3L),
            defaultProfileImages = dummyImages,
            availableTags = dummyTags,
            customTagInput = "",
            isLoading = false
        )
        ProfileEditScreen(
            uiState = dummyState,
            onNicknameChange = {},
            onSelectImage = { _, _ -> },
            onToggleTag = {},
            onAddCustomTag = {},
            onRemoveTag = {},
            onCustomTagInputChange = {},
            onSave = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "프로필 편집 - 저장 중")
@Composable
fun ProfileEditScreenSavingPreview() {
    PickitPickitTheme {
        val dummyState = MyPageState(
            editNickname = "후훗나란남자란",
            isLoading = true
        )
        ProfileEditScreen(
            uiState = dummyState,
            onNicknameChange = {},
            onSelectImage = { _, _ -> },
            onToggleTag = {},
            onAddCustomTag = {},
            onRemoveTag = {},
            onCustomTagInputChange = {},
            onSave = {},
            onCancel = {}
        )
    }
}


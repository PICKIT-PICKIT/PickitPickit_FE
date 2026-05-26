package com.example.pickitpickit.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pickitpickit.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient

import androidx.compose.foundation.border
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import com.example.pickitpickit.ui.theme.PickitPickitTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 배경 그라데이션 브러쉬 (4단 선형 그라데이션)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF2A85A),
            Color(0xFFFFD790),
            Color(0xFFFFF3E2),
            Color(0xFF6B98F8)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.logo_pikipiki),
            contentDescription = "앱 로고",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .aspectRatio(1.5f)
        )
        
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .shadow(elevation = 16.dp, spotColor = Color(0x14000000), ambientColor = Color(0x14000000), shape = RoundedCornerShape(16.dp))
                .shadow(elevation = 24.dp, spotColor = Color(0x24000000), ambientColor = Color(0x24000000), shape = RoundedCornerShape(16.dp))
                .width(343.dp)
                .height(48.dp)
                .background(color = Color(0xFFFEE500), shape = RoundedCornerShape(16.dp))
                .clickable { handleKakaoLogin(context, onLoginSuccess) },
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(start = 27.dp, top = 12.dp, end = 27.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kakao),
                    contentDescription = "카카오 아이콘",
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "카카오로 빠르게 시작하기",
                    color = Color(0xFF1A1A1A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

private fun handleKakaoLogin(context: Context, onLoginSuccess: () -> Unit) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("KAKAO_LOGIN", "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i("KAKAO_LOGIN", "카카오계정으로 로그인 성공 ${token.accessToken}")
            onLoginSuccess()
        }
    }

    // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                Log.e("KAKAO_LOGIN", "카카오톡으로 로그인 실패", error)

                // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    return@loginWithKakaoTalk
                }

                // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            } else if (token != null) {
                Log.i("KAKAO_LOGIN", "카카오톡으로 로그인 성공 ${token.accessToken}")
                onLoginSuccess()
            }
        }
    } else {
        UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    PickitPickitTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

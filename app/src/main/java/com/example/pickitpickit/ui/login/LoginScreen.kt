package com.example.pickitpickit.ui.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pickitpickit.R
import com.example.pickitpickit.core.datastore.UserPreferences
import com.example.pickitpickit.ui.theme.PickitPickitTheme
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (onboardingCompleted: Boolean) -> Unit,
    userPreferences: UserPreferences,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(userPreferences)
    )
    val loginState by loginViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 로그인 상태 처리
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginState.Success -> {
                onLoginSuccess(state.onboardingCompleted)  // 서버 응답의 온보딩 여부 전달
                loginViewModel.resetState()
            }
            is LoginState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                loginViewModel.resetState()
            }
            else -> Unit
        }
    }

    // 배경 그라데이션 브러쉬 (4단 선형 그라데이션)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF2A85A),
            Color(0xFFFFD790),
            Color(0xFFFFF3E2),
            Color(0xFF6B98F8)
        )
    )

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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

            // 카카오 로그인 버튼
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        spotColor = Color(0x14000000),
                        ambientColor = Color(0x14000000),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .width(343.dp)
                    .height(48.dp)
                    .background(
                        color = if (loginState is LoginState.Loading)
                            Color(0xFFD4B800) else Color(0xFFFEE500),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = loginState !is LoginState.Loading) {
                        handleKakaoLogin(context) { kakaoAccessToken ->
                            loginViewModel.loginWithKakao(kakaoAccessToken)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (loginState is LoginState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A1A1A),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        modifier = Modifier.padding(
                            start = 27.dp, top = 12.dp, end = 27.dp, bottom = 12.dp
                        ),
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
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // 에러 스낵바
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White
            )
        }
    }
}

/**
 * 카카오 로그인 처리
 * 성공 시 kakaoAccessToken을 onTokenReceived 콜백으로 전달
 */
private fun handleKakaoLogin(
    context: Context,
    onTokenReceived: (kakaoAccessToken: String) -> Unit
) {
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("KAKAO_LOGIN", "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            Log.i("KAKAO_LOGIN", "카카오 토큰 획득 성공 → 서버 로그인 요청")
            onTokenReceived(token.accessToken)
        }
    }

    if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                Log.e("KAKAO_LOGIN", "카카오톡으로 로그인 실패", error)
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    return@loginWithKakaoTalk
                }
                // 카카오톡 로그인 실패 시 카카오계정으로 fallback
                UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
            } else if (token != null) {
                Log.i("KAKAO_LOGIN", "카카오톡 토큰 획득 성공 → 서버 로그인 요청")
                onTokenReceived(token.accessToken)
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
        // Preview용 더미 UserPreferences (실제 DataStore 미사용)
        // LoginScreen(onLoginSuccess = {}, userPreferences = ...)
        // → Preview에서는 Context 없이 UserPreferences 생성 불가하므로 UI만 확인
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF2A85A),
                            Color(0xFFFFD790),
                            Color(0xFFFFF3E2),
                            Color(0xFF6B98F8)
                        )
                    )
                )
        )
    }
}

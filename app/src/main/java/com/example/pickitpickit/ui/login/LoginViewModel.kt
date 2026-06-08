package com.example.pickitpickit.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.core.datastore.UserPreferences
import com.example.pickitpickit.core.model.KakaoLoginRequest
import com.example.pickitpickit.core.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ──────────────────────────────────────────────────────────────
// 로그인 상태
// ──────────────────────────────────────────────────────────────

sealed class LoginState {
    object Idle    : LoginState()
    object Loading : LoginState()
    data class Success(val onboardingCompleted: Boolean) : LoginState()  // 서버가 알려주는 온보딩 완료 여부
    data class Error(val message: String) : LoginState()
}

// ──────────────────────────────────────────────────────────────
// ViewModel
// ──────────────────────────────────────────────────────────────

class LoginViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    /**
     * 카카오 SDK에서 받은 accessToken을 서버에 전달하고 JWT를 발급받아 저장
     *
     * 흐름: 카카오 SDK → kakaoAccessToken → POST /api/auth/kakao/login → JWT 저장
     */
    fun loginWithKakao(kakaoAccessToken: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = RetrofitClient.authApi.loginWithKakao(
                    KakaoLoginRequest(kakaoAccessToken = kakaoAccessToken)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data!!

                    // JWT 토큰 및 userId DataStore에 저장
                    userPreferences.saveTokens(
                        accessToken  = data.accessToken,
                        refreshToken = data.refreshToken,
                        userId       = data.user.id
                    )
                    
                    // 서버 온보딩 상태를 로컬 DataStore에 동기화
                    userPreferences.setOnboardingCompleted(data.user.onboardingCompleted)

                    // 회원 탈퇴 후 재로그인 시 온보딩 강제 진행 체크
                    val isWithdrawn = userPreferences.getIsWithdrawn().first()
                    val effectiveOnboarding = if (isWithdrawn) {
                        // 탈퇴 후 재가입: 서버 값과 무관하게 온보딩 재진행
                        userPreferences.setOnboardingCompleted(false)
                        userPreferences.setIsWithdrawn(false) // 플래그 삭제
                        Log.i("LOGIN", "탈퇴 후 재로그인 새 온보딩 강제 시작")
                        false
                    } else {
                        data.user.onboardingCompleted
                    }

                    Log.i("LOGIN", "서버 로그인 성공 | userId=${data.user.id}, nickname=${data.user.nickname}")
                    // 서버가 알려주는 온보딩 완료 여부를 그대로 전달
                    _loginState.value = LoginState.Success(effectiveOnboarding)

                } else {
                    val errorMsg = response.body()?.message ?: "로그인에 실패했습니다."
                    Log.e("LOGIN", "서버 로그인 실패: $errorMsg (HTTP ${response.code()})")
                    _loginState.value = LoginState.Error(errorMsg)
                }

            } catch (e: Exception) {
                Log.e("LOGIN", "네트워크 오류", e)
                _loginState.value = LoginState.Error("네트워크 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.")
            }
        }
    }

    /** 에러 상태 초기화 (재시도 등에 사용) */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    // ──────────────────────────────────────────────────────────────
    // Factory (UserPreferences 주입용)
    // ──────────────────────────────────────────────────────────────

    companion object {
        fun factory(userPreferences: UserPreferences): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LoginViewModel(userPreferences) as T
                }
            }
        }
    }
}

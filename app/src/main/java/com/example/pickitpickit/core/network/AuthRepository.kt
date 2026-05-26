package com.example.pickitpickit.core.network

import android.content.Context
import android.util.Log
import com.example.pickitpickit.core.datastore.UserPreferences
import com.example.pickitpickit.core.model.LogoutRequest
import com.example.pickitpickit.core.model.TokenRefreshRequest
import com.example.pickitpickit.core.model.UserDto
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.flow.firstOrNull

/**
 * 인증 관련 API 호출을 담당하는 Repository
 *
 * - 토큰 재발급 (reissueToken)
 * - 로그아웃     (logout) — 서버 폐기 + Kakao SDK logout
 * - 내 정보 조회 (getMe)
 *
 * UI에서 직접 RetrofitClient를 쓰지 않고 이 클래스를 통해 호출
 */
class AuthRepository(
    private val userPreferences: UserPreferences,
    private val context: Context
) {

    // ──────────────────────────────────────────────────────────────
    // 토큰 재발급
    // POST /api/auth/token/reissue
    // ──────────────────────────────────────────────────────────────

    /**
     * Refresh Token으로 새 Access Token + Refresh Token 발급
     * 앱 시작 시 또는 401 응답 시 자동으로 호출
     *
     * @return 성공 여부 (true = 토큰 갱신 완료)
     */
    suspend fun reissueToken(): Boolean {
        val currentRefreshToken = userPreferences.getRefreshToken().firstOrNull()
        if (currentRefreshToken.isNullOrEmpty()) {
            Log.w("AUTH_REPO", "reissueToken: refreshToken 없음 → 로그인 필요")
            return false
        }

        return try {
            val response = RetrofitClient.authApi.reissueToken(
                TokenRefreshRequest(refreshToken = currentRefreshToken)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                userPreferences.saveTokens(
                    accessToken  = data.accessToken,
                    refreshToken = data.refreshToken
                )
                Log.i("AUTH_REPO", "토큰 재발급 성공")
                true
            } else {
                Log.e("AUTH_REPO", "토큰 재발급 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "토큰 재발급 네트워크 오류", e)
            false
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 로그아웃
    // POST /api/auth/logout → 성공 시 Kakao SDK logout
    // ──────────────────────────────────────────────────────────────

    /**
     * 서버 로그아웃 + 카카오 SDK 로그아웃 + 로컬 토큰 삭제
     *
     * ⚠️ 명세: "Android에서는 이 API 성공 후 Kakao SDK logout도 함께 호출하세요"
     *
     * @param onComplete 로그아웃 완료 후 콜백 (성공/실패 여부 전달)
     */
    suspend fun logout(onComplete: (success: Boolean) -> Unit) {
        val refreshToken = userPreferences.getRefreshToken().firstOrNull()
        if (refreshToken.isNullOrEmpty()) {
            Log.w("AUTH_REPO", "logout: refreshToken 없음, 로컬 데이터만 삭제")
            clearLocalSession(onComplete)
            return
        }

        try {
            val response = RetrofitClient.authApi.logout(
                LogoutRequest(refreshToken = refreshToken)
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("AUTH_REPO", "서버 로그아웃 성공")
            } else {
                Log.w("AUTH_REPO", "서버 로그아웃 실패: ${response.body()?.message} — 로컬 데이터는 삭제")
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "서버 로그아웃 네트워크 오류 — 로컬 데이터는 삭제", e)
        }

        // 서버 결과와 무관하게 로컬 세션 삭제 + 카카오 SDK 로그아웃
        clearLocalSession(onComplete)
    }

    /** 로컬 토큰 삭제 + 카카오 SDK 로그아웃 */
    private suspend fun clearLocalSession(onComplete: (success: Boolean) -> Unit) {
        userPreferences.clearTokens()

        // 카카오 SDK 로그아웃 (토큰 삭제, 실패해도 무시)
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.w("AUTH_REPO", "카카오 SDK 로그아웃 실패 (무시)", error)
            } else {
                Log.i("AUTH_REPO", "카카오 SDK 로그아웃 완료")
            }
            onComplete(true)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 내 정보 조회
    // GET /api/auth/me  (Authorization 헤더 자동 삽입)
    // ──────────────────────────────────────────────────────────────

    /**
     * 현재 로그인한 사용자 정보 조회
     * AccessToken은 AuthInterceptor가 자동으로 헤더에 삽입
     *
     * @return UserDto (성공) / null (실패)
     */
    suspend fun getMe(): UserDto? {
        return try {
            val response = RetrofitClient.authApi.getMe()

            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()!!.data
                Log.i("AUTH_REPO", "내 정보 조회 성공: id=${user?.id}, nickname=${user?.nickname}")
                user
            } else {
                Log.e("AUTH_REPO", "내 정보 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("AUTH_REPO", "내 정보 조회 네트워크 오류", e)
            null
        }
    }
}

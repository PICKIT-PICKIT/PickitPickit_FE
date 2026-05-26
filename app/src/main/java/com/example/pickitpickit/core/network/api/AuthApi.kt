package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.AuthResponse
import com.example.pickitpickit.core.model.KakaoLoginRequest
import com.example.pickitpickit.core.model.TokenRefreshRequest
import com.example.pickitpickit.core.model.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 인증 관련 API 인터페이스
 *
 * TODO: 백엔드 명세 확인 후 엔드포인트 경로 수정 필요
 *   - 예시로 /auth/kakao, /auth/refresh 를 써뒀음
 *   - 실제 명세에 따라 @POST 경로 및 Request/Response 필드 변경
 */
interface AuthApi {

    /**
     * 카카오 Access Token → 서버 JWT 교환
     * TODO: 실제 엔드포인트 경로 확인 후 수정
     */
    @POST("auth/kakao")
    suspend fun loginWithKakao(
        @Body body: KakaoLoginRequest
    ): Response<AuthResponse>

    /**
     * Refresh Token으로 Access Token 재발급
     * TODO: 실제 엔드포인트 경로 확인 후 수정
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body body: TokenRefreshRequest
    ): Response<TokenRefreshResponse>
}

package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.KakaoLoginRequest
import com.example.pickitpickit.core.model.LoginData
import com.example.pickitpickit.core.model.LogoutRequest
import com.example.pickitpickit.core.model.TokenRefreshData
import com.example.pickitpickit.core.model.TokenRefreshRequest
import com.example.pickitpickit.core.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 인증 관련 API 인터페이스
 */
interface AuthApi {

    /**
     * 카카오 Access Token → 서버 JWT 교환
     * POST /api/auth/kakao/login
     *
     * Request:  { "kakaoAccessToken": "..." }
     * Response: { status, success, code, message, data: { accessToken, refreshToken, user } }
     */
    @POST("api/auth/kakao/login")
    suspend fun loginWithKakao(
        @Body body: KakaoLoginRequest
    ): Response<ApiResponse<LoginData>>

    /**
     * Refresh Token으로 Access Token + Refresh Token 재발급
     * POST /api/auth/token/reissue
     *
     * Request:  { "refreshToken": "..." }
     * Response: { data: { accessToken, refreshToken } }
     */
    @POST("api/auth/token/reissue")
    suspend fun reissueToken(
        @Body body: TokenRefreshRequest
    ): Response<ApiResponse<TokenRefreshData>>

    /**
     * 로그아웃 (Refresh Token 폐기)
     * POST /api/auth/logout
     *
     * ⚠️ Android에서는 이 API 성공 후 Kakao SDK logout도 함께 호출할 것
     * Request:  { "refreshToken": "..." }
     * Response: { data: "string" }
     */
    @POST("api/auth/logout")
    suspend fun logout(
        @Body body: LogoutRequest
    ): Response<ApiResponse<String>>

    /**
     * 내 정보 조회
     * GET /api/auth/me
     *
     * Header: Authorization: Bearer {accessToken} (자동 삽입 - AuthInterceptor)
     * Response: { data: { id, nickname, profileImageUrl } }
     */
    @GET("api/auth/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>
}


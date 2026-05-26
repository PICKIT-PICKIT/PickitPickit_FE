package com.example.pickitpickit.core.model

// ──────────────────────────────────────────────────────────────────────────────
// 공통 API 응답 래퍼
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 모든 API 응답의 공통 래퍼
 * { "status": 0, "success": true, "code": "...", "message": "...", "data": { ... } }
 */
data class ApiResponse<T>(
    val status: Int,
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T?
)

// ──────────────────────────────────────────────────────────────────────────────
// 요청 (Request) 모델
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 카카오 로그인 요청 Body
 * POST /api/auth/kakao/login
 */
data class KakaoLoginRequest(
    val kakaoAccessToken: String   // 카카오 SDK OAuthToken.accessToken
)

/**
 * 토큰 갱신 요청 Body
 * TODO: 토큰 갱신 엔드포인트 명세 확인 후 수정
 */
data class TokenRefreshRequest(
    val refreshToken: String
)

// ──────────────────────────────────────────────────────────────────────────────
// 응답 (Response) 모델
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 카카오 로그인 응답 data 필드
 */
data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

/**
 * 사용자 정보
 */
data class UserDto(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val onboardingCompleted: Boolean = false  // 서버는 온보딩 완료 여부를 관리함
)

/**
 * 토큰 재발급 응답 data 필드
 * POST /api/auth/token/reissue
 */
data class TokenRefreshData(
    val accessToken: String,
    val refreshToken: String     // 새 refreshToken도 함께 발급됨
)

/**
 * 로그아웃 요청 Body
 * POST /api/auth/logout
 */
data class LogoutRequest(
    val refreshToken: String
)

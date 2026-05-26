package com.example.pickitpickit.core.model

// ──────────────────────────────────────────────────────────────────────────────
// 요청 (Request) 모델
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 카카오 로그인 요청 Body
 * TODO: 백엔드 명세 확인 후 필드명 수정 필요
 *   - accessToken 방식인지 authorizationCode 방식인지 확인
 */
data class KakaoLoginRequest(
    val accessToken: String      // 카카오 SDK에서 받은 Access Token
)

// ──────────────────────────────────────────────────────────────────────────────
// 응답 (Response) 모델
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 로그인/회원가입 공통 응답
 * TODO: 백엔드 명세 확인 후 필드명 수정 필요
 */
data class AuthResponse(
    val accessToken: String,     // 서버 발급 JWT Access Token
    val refreshToken: String,    // 서버 발급 JWT Refresh Token
    val isNewUser: Boolean       // true → 신규 가입 (온보딩 필요), false → 기존 유저
)

/**
 * 토큰 갱신 요청 Body
 * TODO: 백엔드 명세 확인 후 필드명 수정 필요
 */
data class TokenRefreshRequest(
    val refreshToken: String
)

/**
 * 토큰 갱신 응답
 * TODO: 백엔드 명세 확인 후 필드명 수정 필요
 */
data class TokenRefreshResponse(
    val accessToken: String
)

/**
 * API 공통 에러 응답
 * TODO: 백엔드 에러 응답 포맷 확인 후 수정 필요
 */
data class ApiError(
    val code: String,
    val message: String
)

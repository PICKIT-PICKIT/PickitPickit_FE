package com.example.pickitpickit.core.model

/**
 * 온보딩 상태 조회 응답 DTO
 * GET /api/onboarding/me
 */
data class OnboardingStatusResponse(
    val userId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val kakaoProfileImageUrl: String?,
    val profileImageType: String?, // KAKAO, DEFAULT
    val selectedTags: List<InterestTagResponse>,
    val onboardingCompleted: Boolean
)

/**
 * 프로필 이미지 후보군 응답 DTO
 * GET /api/onboarding/profile-images
 */
data class ProfileImageOptionsResponse(
    val kakaoProfileImageUrl: String?,
    val defaultImages: List<DefaultProfileImageResponse>
)

/**
 * 기본 프로필 이미지 DTO
 */
data class DefaultProfileImageResponse(
    val id: String,
    val imageUrl: String
)

/**
 * 관심 태그 정보 DTO
 * GET /api/onboarding/interest-tags
 */
data class InterestTagResponse(
    val id: Long,
    val name: String
)

/**
 * 닉네임 수정 요청 Body
 * PATCH /api/onboarding/nickname
 */
data class NicknameUpdateRequest(
    val nickname: String
)

/**
 * 프로필 이미지 수정 요청 Body
 * PATCH /api/onboarding/profile-image
 */
data class ProfileImageUpdateRequest(
    val type: String, // KAKAO, DEFAULT
    val profileImageUrl: String
)

/**
 * 관심 태그 수정 요청 Body
 * PATCH /api/onboarding/interest-tags
 */
data class InterestTagUpdateRequest(
    val tagIds: List<Long>
)

/**
 * 온보딩 최종 완료 처리 응답 DTO
 * POST /api/onboarding/complete
 */
data class OnboardingCompleteResponse(
    val onboardingCompleted: Boolean,
    val next: String // MAIN 등
)

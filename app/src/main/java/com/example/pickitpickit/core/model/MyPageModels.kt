package com.example.pickitpickit.core.model

/**
 * 마이페이지 관심 태그 DTO
 */
data class MyPageInterestTag(
    val id: Long,
    val name: String,
    val selected: Boolean
)

/**
 * 마이페이지 기본 프로필 이미지 DTO
 */
data class MyPageDefaultImage(
    val id: String,
    val imageUrl: String,
    val selected: Boolean
)

/**
 * 마이페이지 프로필 조회 응답 Data DTO
 * GET /api/users/me/profile
 * PATCH /api/users/me/profile
 */
data class MyPageProfileResponse(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val kakaoProfileImageUrl: String?,
    val profileImageType: String, // KAKAO, DEFAULT
    val interestTags: List<MyPageInterestTag>,
    val defaultProfileImages: List<MyPageDefaultImage>,
    val reviewCount: Int,
    val bragCount: Int,
    val favoriteStoreCount: Int
)

/**
 * 마이페이지 프로필 수정 요청 Body
 * PATCH /api/users/me/profile
 */
data class MyPageProfileUpdateRequest(
    val nickname: String,
    val profileImageType: String, // KAKAO, DEFAULT
    val profileImageUrl: String,
    val interestTagIds: List<Long>
)

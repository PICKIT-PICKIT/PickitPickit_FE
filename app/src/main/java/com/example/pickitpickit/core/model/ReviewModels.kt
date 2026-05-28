package com.example.pickitpickit.core.model

// 개별 리뷰 DTO
data class ReviewDto(
    val reviewId: Long,
    val userId: Long,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
    val storeId: Long,
    val rating: Double,
    val difficulty: Int,
    val difficultyLabel: String?,
    val content: String?,
    val imageUrl: String?,
    val createdAt: String,
    val modifiedAt: String
)

// 매장 리뷰 목록 및 요약 응답 DTO
data class StoreReviewListResponse(
    val storeId: Long,
    val reviewCount: Int,
    val averageRating: Double,
    val showRating: Boolean,
    val averageDifficulty: Double,
    val showDifficulty: Boolean,
    val reviews: List<ReviewDto>
)

// 리뷰 작성 요청 Body
data class ReviewRequest(
    val userId: Long,
    val storeId: Long,
    val rating: Double,
    val difficulty: Int,
    val content: String?,
    val imageUrl: String? = null
)

// 리뷰 수정(PATCH) 요청 Body
data class ReviewPatchRequest(
    val userId: Long,
    val rating: Double,
    val difficulty: Int,
    val content: String?,
    val imageUrl: String? = null
)

// 리뷰 가이드 세부 DTO
data class GuideItemDto(
    val title: String,
    val messages: List<String>
)

// 리뷰 가이드 전체 응답 DTO
data class ReviewWriteGuideResponse(
    val reviewGuide: GuideItemDto,
    val bragGuide: GuideItemDto
)


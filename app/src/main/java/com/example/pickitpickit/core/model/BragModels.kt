package com.example.pickitpickit.core.model

/**
 * 개별 자랑글 DTO
 */
data class BragDto(
    val bragId: Long,
    val userId: Long,
    val authorNickname: String,
    val authorProfileImageUrl: String?,
    val storeId: Long,
    val spentCost: Int,
    val imageUrl: String,
    val content: String,
    val createdAt: String,
    val modifiedAt: String
)

/**
 * 매장 자랑글 목록 응답 DTO (추정)
 */
data class StoreBragListResponse(
    val storeId: Long,
    val bragCount: Int,
    val brags: List<BragDto>
)

/**
 * 자랑글 등록 요청 Body
 * POST /api/reviews/brags
 */
data class BragRequest(
    val userId: Long,
    val storeId: Long,
    val spentCost: Int,
    val imageUrl: String,
    val content: String
)

/**
 * 자랑글 수정 요청 Body
 * PATCH /api/reviews/brags/{bragId}
 */
data class BragPatchRequest(
    val userId: Long,
    val storeId: Long,
    val spentCost: Int,
    val imageUrl: String,
    val content: String
)

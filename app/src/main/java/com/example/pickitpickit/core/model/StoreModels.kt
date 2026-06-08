package com.example.pickitpickit.core.model

/**
 * 매장 타입
 */
enum class StoreType {
    CLAW,
    GACHA,
    ALL
}

/**
 * 주변 매장 개별 정보 DTO
 * GET /api/stores/nearby 응답의 data 배열 아이템
 */
data class StoreNearbyResponse(
    val id: Int,
    val sourcePlaceId: String?,
    val name: String,
    val type: String,               // "CLAW", "GACHA"
    val latitude: Double,
    val longitude: Double,
    val distance: Int,              // 미터 단위 거리
    val address: String?,
    val contact: String?,
    val businessHours: String?,
    val mainImageUrl: String?,
    val kakaoDetailUrl: String?
)

/**
 * 태그 정보 DTO
 */
data class TagDto(
    val tagId: Long,
    val name: String
)

/**
 * 매장 등록 상품 DTO
 */
data class ProductDto(
    val productId: Long,
    val itemId: Long,
    val itemName: String,
    val category: String?,
    val price: Int,
    val inventoryMode: String?,
    val stockQuantity: Int,
    val stockStatus: String?,
    val difficulty: Int,
    val difficultyLabel: String?,
    val imageUrl: String?,
    val tags: List<TagDto>
)

/**
 * 매장 기본 상세 정보 DTO
 */
data class StoreDetailDto(
    val id: Long,
    val sourcePlaceId: String?,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Int,
    val address: String?,
    val contact: String?,
    val businessHours: String?,
    val mainImageUrl: String?,
    val kakaoDetailUrl: String?
)

/**
 * 매장 상세 조회 API 응답 data 필드 DTO
 * GET /api/stores/{storeId}
 */
data class StoreDetailResponse(
    val store: StoreDetailDto,
    val productCount: Int,
    val totalStockQuantity: Int,
    val tags: List<TagDto>,
    val products: List<ProductDto>
)

data class StoreTagsUpdateRequest(
    val tags: List<String>
)


package com.example.pickitpickit.core.model

data class StoreProductRegisterRequest(
    val storeId: Long,
    val itemId: Long,
    val price: Int,
    val inventoryMode: String = "QUANTITY",
    val stockQuantity: Int,
    val stockStatus: String = "IN_STOCK",
    val difficulty: Int,
    val imageUrl: String = "",
    val tags: List<String>
)

data class StoreProductUpdateRequest(
    val price: Int,
    val inventoryMode: String = "QUANTITY",
    val stockQuantity: Int,
    val stockStatus: String = "IN_STOCK",
    val difficulty: Int,
    val imageUrl: String = "",
    val tags: List<String>
)

data class ItemCreateRequest(
    val name: String,
    val category: String,
    val defaultImageUrl: String = "",
    val tags: List<String>
)

data class ItemResponse(
    val itemId: Long,
    val name: String,
    val category: String,
    val defaultImageUrl: String?,
    val tags: List<TagDto>
)

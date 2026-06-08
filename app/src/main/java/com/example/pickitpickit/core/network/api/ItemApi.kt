package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class ItemDto(
    val id: Long,
    val name: String,
    val category: String?,
    val imageUrl: String?,
    val isMock: Boolean = false
)

interface ItemApi {

    /**
     * 캐릭터/상품 아이템 검색
     * GET /api/items/search
     */
    @GET("api/items/search")
    suspend fun searchItems(
        @Query("keyword") keyword: String
    ): Response<ApiResponse<List<ItemDto>>>
    
    /**
     * 캐릭터/상품 아이템 전체/필터 조회 (대체용)
     * GET /api/items
     */
    @GET("api/items")
    suspend fun getItems(
        @Query("keyword") keyword: String? = null
    ): Response<ApiResponse<List<ItemDto>>>
}

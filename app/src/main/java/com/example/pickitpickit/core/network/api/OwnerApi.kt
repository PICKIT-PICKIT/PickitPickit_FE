package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.ProductDto
import com.example.pickitpickit.core.model.StoreProductRegisterRequest
import com.example.pickitpickit.core.model.StoreProductUpdateRequest
import com.example.pickitpickit.core.model.ItemCreateRequest
import com.example.pickitpickit.core.model.ItemResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import com.example.pickitpickit.core.model.StoreTagsUpdateRequest
import com.example.pickitpickit.core.model.TagDto

interface OwnerApi {

    /**
     * 상품 마스터 등록
     * POST /api/admin/items
     */
    @POST("api/admin/items")
    suspend fun registerItem(
        @Body body: ItemCreateRequest
    ): Response<ApiResponse<ItemResponse>>

    /**
     * 내 매장 상품 등록
     * POST /api/owner/store-products
     */
    @POST("api/owner/store-products")
    suspend fun registerStoreProduct(
        @Body body: StoreProductRegisterRequest
    ): Response<ApiResponse<ProductDto>>

    /**
     * 내 매장 상품 수정
     * PATCH /api/owner/store-products/{storeProductId}
     */
    @PATCH("api/owner/store-products/{storeProductId}")
    suspend fun updateStoreProduct(
        @Path("storeProductId") storeProductId: Long,
        @Body body: StoreProductUpdateRequest
    ): Response<ApiResponse<ProductDto>>

    /**
     * 내 매장 상품 삭제
     * DELETE /api/owner/store-products/{storeProductId}
     */
    @DELETE("api/owner/store-products/{storeProductId}")
    suspend fun deleteStoreProduct(
        @Path("storeProductId") storeProductId: Long
    ): Response<ApiResponse<String>>

    /**
     * 내 매장 대표 태그 교체
     * PUT /api/owner/stores/{storeId}/tags
     */
    @PUT("api/owner/stores/{storeId}/tags")
    suspend fun updateStoreTags(
        @Path("storeId") storeId: Long,
        @Body body: StoreTagsUpdateRequest
    ): Response<ApiResponse<List<TagDto>>>
}

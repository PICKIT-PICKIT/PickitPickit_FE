package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.StoreDetailResponse
import com.example.pickitpickit.core.model.StoreNearbyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StoreApi {

    /**
     * 현재 위치 기반 주변 매장 조회
     * GET /api/stores/nearby
     *
     * @param lat    사용자 위도 (필수)
     * @param lng    사용자 경도 (필수)
     * @param radius 검색 반경(m) - 허용값: 500, 1000, 3000, 5000 (기본값: 1000)
     * @param type   매장 유형 - 허용값: CLAW, GACHA, ALL (기본값: ALL)
     */
    @GET("api/stores/nearby")
    suspend fun getNearbyStores(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int,
        @Query("type") type: String = "ALL"
    ): Response<ApiResponse<List<StoreNearbyResponse>>>

    /**
     * 매장 상세 정보 및 등록된 상품/재고/태그 조회
     * GET /api/stores/{storeId}
     *
     * @param storeId 매장 ID (필수)
     * @param lat     사용자 위도 (선택)
     * @param lng     사용자 경도 (선택)
     */
    @GET("api/stores/{storeId}")
    suspend fun getStoreDetail(
        @Path("storeId") storeId: Long,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): Response<ApiResponse<StoreDetailResponse>>

    /**
     * 매장명 또는 주소 기준 매장 검색
     * GET /api/stores/search
     *
     * @param keyword 검색어 (필수)
     * @param type    매장 유형 - CLAW, GACHA, ALL (기본값: ALL)
     * @param lat     사용자 위도 (선택)
     * @param lng     사용자 경도 (선택)
     * @param limit   조회 개수 1~50 (기본값: 20)
     */
    @GET("api/stores/search")
    suspend fun searchStores(
        @Query("keyword") keyword: String,
        @Query("type") type: String = "ALL",
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<StoreNearbyResponse>>>
}

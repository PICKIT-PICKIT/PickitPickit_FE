package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.MyPageProfileResponse
import com.example.pickitpickit.core.model.MyPageProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.pickitpickit.core.model.FavoriteStoreResponse

interface UserApi {

    /**
     * 마이페이지 프로필 조회
     * GET /api/users/me/profile
     */
    @GET("api/users/me/profile")
    suspend fun getProfile(): Response<ApiResponse<MyPageProfileResponse>>

    /**
     * 마이페이지 프로필 수정
     * PATCH /api/users/me/profile
     */
    @PATCH("api/users/me/profile")
    suspend fun updateProfile(
        @Body body: MyPageProfileUpdateRequest
    ): Response<ApiResponse<MyPageProfileResponse>>

    /**
     * 회원 탈퇴
     * DELETE /api/users/me
     */
    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<ApiResponse<String>>

    /**
     * 내 관심매장 목록 조회
     * GET /api/users/me/favorite-stores
     */
    @GET("api/users/me/favorite-stores")
    suspend fun getFavoriteStores(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null
    ): Response<ApiResponse<List<FavoriteStoreResponse>>>

    /**
     * 관심매장 추가
     * POST /api/users/me/favorite-stores/{storeId}
     */
    @POST("api/users/me/favorite-stores/{storeId}")
    suspend fun addFavoriteStore(
        @Path("storeId") storeId: Long
    ): Response<ApiResponse<FavoriteStoreResponse>>

    /**
     * 관심매장 삭제
     * DELETE /api/users/me/favorite-stores/{storeId}
     */
    @DELETE("api/users/me/favorite-stores/{storeId}")
    suspend fun deleteFavoriteStore(
        @Path("storeId") storeId: Long
    ): Response<ApiResponse<String>>
}

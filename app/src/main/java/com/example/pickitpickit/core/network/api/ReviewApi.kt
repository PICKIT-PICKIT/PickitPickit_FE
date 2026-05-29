package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.ReviewDto
import com.example.pickitpickit.core.model.ReviewRequest
import com.example.pickitpickit.core.model.ReviewPatchRequest
import com.example.pickitpickit.core.model.ReviewWriteGuideResponse
import com.example.pickitpickit.core.model.StoreReviewListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReviewApi {

    /**
     * 특정 매장의 리뷰 목록 및 요약 정보를 조회합니다.
     * GET /api/reviews/stores/{storeId}
     */
    @GET("api/reviews/stores/{storeId}")
    suspend fun getStoreReviews(
        @Path("storeId") storeId: Long
    ): Response<ApiResponse<StoreReviewListResponse>>

    /**
     * 특정 매장에 대한 리뷰를 작성합니다.
     * POST /api/reviews
     */
    @POST("api/reviews")
    suspend fun postReview(
        @Body request: ReviewRequest
    ): Response<ApiResponse<ReviewDto>>

    /**
     * 특정 리뷰를 수정합니다.
     * PATCH /api/reviews/{reviewId}
     */
    @PATCH("api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body request: ReviewPatchRequest
    ): Response<ApiResponse<ReviewDto>>

    /**
     * 특정 리뷰를 삭제합니다.
     * DELETE /api/reviews/{reviewId}?userId={userId}
     */
    @DELETE("api/reviews/{reviewId}")
    suspend fun deleteReview(
        @Path("reviewId") reviewId: Long,
        @Query("userId") userId: Long
    ): Response<ApiResponse<String>>

    /**
     * 리뷰 작성 가이드를 조회합니다.
     * GET /api/reviews/write-guide
     */
    @GET("api/reviews/write-guide")
    suspend fun getReviewWriteGuide(): Response<ApiResponse<ReviewWriteGuideResponse>>
}


package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.BragDto
import com.example.pickitpickit.core.model.BragRequest
import com.example.pickitpickit.core.model.BragPatchRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BragApi {

    /**
     * 인형뽑기 결과물 자랑 게시글을 작성합니다.
     * POST /api/reviews/brags
     */
    @POST("api/reviews/brags")
    suspend fun postBrag(
        @Body request: BragRequest
    ): Response<ApiResponse<BragDto>>

    /**
     * 자랑하기 게시글을 수정합니다.
     * PATCH /api/reviews/brags/{bragId}
     */
    @PATCH("api/reviews/brags/{bragId}")
    suspend fun updateBrag(
        @Path("bragId") bragId: Long,
        @Body request: BragPatchRequest
    ): Response<ApiResponse<BragDto>>

    /**
     * 자랑하기 게시글을 삭제합니다.
     * DELETE /api/reviews/brags/{bragId}?userId={userId}
     */
    @DELETE("api/reviews/brags/{bragId}")
    suspend fun deleteBrag(
        @Path("bragId") bragId: Long,
        @Query("userId") userId: Long
    ): Response<ApiResponse<String>>

    /**
     * 전체 자랑하기 게시글 목록을 조회합니다.
     * GET /api/reviews/brags
     */
    @GET("api/reviews/brags")
    suspend fun getAllBrags(): Response<ApiResponse<List<BragDto>>>
}

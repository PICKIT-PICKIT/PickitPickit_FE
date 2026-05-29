package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.MyPageProfileResponse
import com.example.pickitpickit.core.model.MyPageProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

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
}

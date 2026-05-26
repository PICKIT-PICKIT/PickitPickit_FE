package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface OnboardingApi {

    /**
     * 온보딩 상태 조회
     * GET /api/onboarding/me
     */
    @GET("api/onboarding/me")
    suspend fun getStatus(): Response<ApiResponse<OnboardingStatusResponse>>

    /**
     * 프로필 이미지 후보 조회
     * GET /api/onboarding/profile-images
     */
    @GET("api/onboarding/profile-images")
    suspend fun getProfileImages(): Response<ApiResponse<ProfileImageOptionsResponse>>

    /**
     * 관심 태그 목록 조회
     * GET /api/onboarding/interest-tags
     */
    @GET("api/onboarding/interest-tags")
    suspend fun getInterestTags(): Response<ApiResponse<List<InterestTagResponse>>>

    /**
     * 닉네임 저장
     * PATCH /api/onboarding/nickname
     */
    @PATCH("api/onboarding/nickname")
    suspend fun updateNickname(
        @Body body: NicknameUpdateRequest
    ): Response<ApiResponse<OnboardingStatusResponse>>

    /**
     * 프로필 이미지 저장
     * PATCH /api/onboarding/profile-image
     */
    @PATCH("api/onboarding/profile-image")
    suspend fun updateProfileImage(
        @Body body: ProfileImageUpdateRequest
    ): Response<ApiResponse<OnboardingStatusResponse>>

    /**
     * 관심 태그 저장
     * PATCH /api/onboarding/interest-tags
     */
    @PATCH("api/onboarding/interest-tags")
    suspend fun updateInterestTags(
        @Body body: InterestTagUpdateRequest
    ): Response<ApiResponse<OnboardingStatusResponse>>

    /**
     * 온보딩 완료 처리
     * POST /api/onboarding/complete
     */
    @POST("api/onboarding/complete")
    suspend fun complete(): Response<ApiResponse<OnboardingCompleteResponse>>
}

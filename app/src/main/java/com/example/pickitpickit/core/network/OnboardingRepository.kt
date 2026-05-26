package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.*

/**
 * 온보딩 관련 API 호출을 담당하는 Repository
 */
class OnboardingRepository {

    /**
     * 현재 로그인한 사용자의 온보딩 상태 조회
     * GET /api/onboarding/me
     */
    suspend fun getOnboardingStatus(): OnboardingStatusResponse? {
        return try {
            val response = RetrofitClient.onboardingApi.getStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data
            } else {
                Log.w("ONBOARDING_REPO", "온보딩 상태 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "온보딩 상태 조회 네트워크 오류", e)
            null
        }
    }

    /**
     * 프로필 이미지 후보군(카카오 프로필 및 기본 프로필 목록) 조회
     * GET /api/onboarding/profile-images
     */
    suspend fun getProfileImages(): ProfileImageOptionsResponse? {
        return try {
            val response = RetrofitClient.onboardingApi.getProfileImages()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data
            } else {
                Log.w("ONBOARDING_REPO", "프로필 이미지 목록 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "프로필 이미지 목록 조회 네트워크 오류", e)
            null
        }
    }

    /**
     * 관심 태그 전체 리스트 조회
     * GET /api/onboarding/interest-tags
     */
    suspend fun getInterestTags(): List<InterestTagResponse> {
        return try {
            val response = RetrofitClient.onboardingApi.getInterestTags()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.data ?: emptyList()
            } else {
                Log.w("ONBOARDING_REPO", "관심 태그 목록 조회 실패: ${response.body()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "관심 태그 목록 조회 네트워크 오류", e)
            emptyList()
        }
    }

    /**
     * 입력/생성한 닉네임 임시 저장
     * PATCH /api/onboarding/nickname
     */
    suspend fun updateNickname(nickname: String): Boolean {
        return try {
            val response = RetrofitClient.onboardingApi.updateNickname(NicknameUpdateRequest(nickname))
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "닉네임 저장 성공: $nickname")
                true
            } else {
                Log.w("ONBOARDING_REPO", "닉네임 저장 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "닉네임 저장 네트워크 오류", e)
            false
        }
    }

    /**
     * 선택한 프로필 이미지 임시 저장
     * PATCH /api/onboarding/profile-image
     */
    suspend fun updateProfileImage(type: String, imageUrl: String): Boolean {
        return try {
            val response = RetrofitClient.onboardingApi.updateProfileImage(
                ProfileImageUpdateRequest(type = type, profileImageUrl = imageUrl)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "프로필 이미지 저장 성공: 타입=$type, URL=$imageUrl")
                true
            } else {
                Log.w("ONBOARDING_REPO", "프로필 이미지 저장 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "프로필 이미지 저장 네트워크 오류", e)
            false
        }
    }

    /**
     * 선택한 관심 태그 ID 목록 임시 저장
     * PATCH /api/onboarding/interest-tags
     */
    suspend fun updateInterestTags(tagIds: List<Long>): Boolean {
        return try {
            val response = RetrofitClient.onboardingApi.updateInterestTags(InterestTagUpdateRequest(tagIds))
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "관심 태그 저장 성공: 태그개수=${tagIds.size}")
                true
            } else {
                Log.w("ONBOARDING_REPO", "관심 태그 저장 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "관심 태그 저장 네트워크 오류", e)
            false
        }
    }

    /**
     * 온보딩 최종 검증 및 완료 처리
     * POST /api/onboarding/complete
     */
    suspend fun completeOnboarding(): OnboardingCompleteResponse? {
        return try {
            val response = RetrofitClient.onboardingApi.complete()
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "온보딩 완료 처리 성공")
                response.body()!!.data
            } else {
                Log.w("ONBOARDING_REPO", "온보딩 완료 처리 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "온보딩 완료 처리 네트워크 오류", e)
            null
        }
    }
}

package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.*

/**
 * 온보딩 관련 API 호출을 담당하는 Repository (Resilient Direct Network Layer)
 * 🌟 유령 토큰 자가 정화 처리를 위해 복잡한 재시도 및 딜레이 루프를 완전히 걷어내고, 명쾌한 1회 직접 호출 구조로 최적화되었습니다.
 */
class OnboardingRepository {

    /**
     * 현재 로그인한 사용자의 온보딩 상태 조회
     * GET /api/onboarding/me
     */
    suspend fun getOnboardingStatus(): OnboardingStatusResponse? {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        try {
            val response = RetrofitClient.onboardingApi.getStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                var data = response.body()!!.data
                if (data != null) {
                    // 상대 경로 보정
                    if (!data.profileImageUrl.isNullOrEmpty() && data.profileImageUrl.startsWith("/")) {
                        data = data.copy(profileImageUrl = "$baseUrl${data.profileImageUrl}")
                    }
                    // http -> https 보안 경로 보정
                    if (!data.profileImageUrl.isNullOrEmpty() && data.profileImageUrl.startsWith("http://")) {
                        data = data.copy(profileImageUrl = data.profileImageUrl.replace("http://", "https://"))
                    }
                    if (!data.kakaoProfileImageUrl.isNullOrEmpty() && data.kakaoProfileImageUrl.startsWith("http://")) {
                        data = data.copy(kakaoProfileImageUrl = data.kakaoProfileImageUrl.replace("http://", "https://"))
                    }
                    Log.i("ONBOARDING_REPO", "온보딩 상태 조회 성공 🏆")
                    return data
                }
            } else {
                val code = response.code()
                val errorMsg = response.errorBody()?.string() ?: ""
                Log.w("ONBOARDING_REPO", "온보딩 상태 조회 실패 | HTTP $code | 에러: $errorMsg")
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "온보딩 상태 조회 네트워크 오류", e)
        }
        return null
    }

    /**
     * 프로필 이미지 후보군(카카오 프로필 및 기본 프로필 목록) 조회
     * GET /api/onboarding/profile-images
     */
    suspend fun getProfileImages(): ProfileImageOptionsResponse? {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        try {
            val response = RetrofitClient.onboardingApi.getProfileImages()
            if (response.isSuccessful && response.body()?.success == true) {
                var data = response.body()!!.data
                if (data != null) {
                    val mappedImages = data.defaultImages.map { img ->
                        val fullUrl = if (img.imageUrl.startsWith("/")) {
                            "$baseUrl${img.imageUrl}"
                        } else {
                            "$baseUrl/${img.imageUrl}"
                        }
                        val secureUrl = if (fullUrl.startsWith("http://")) {
                            fullUrl.replace("http://", "https://")
                        } else {
                            fullUrl
                        }
                        img.copy(imageUrl = secureUrl)
                    }
                    
                    val secureKakaoUrl = data.kakaoProfileImageUrl?.let { url ->
                        if (url.startsWith("http://")) url.replace("http://", "https://") else url
                    }
                    
                    data = data.copy(
                        kakaoProfileImageUrl = secureKakaoUrl,
                        defaultImages = mappedImages
                    )
                    return data
                }
            } else {
                val code = response.code()
                Log.w("ONBOARDING_REPO", "프로필 이미지 목록 조회 실패 | HTTP $code")
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "프로필 이미지 목록 조회 네트워크 오류", e)
        }
        return null
    }

    /**
     * 관심 태그 전체 리스트 조회
     * GET /api/onboarding/interest-tags
     */
    suspend fun getInterestTags(): List<InterestTagResponse> {
        try {
            val response = RetrofitClient.onboardingApi.getInterestTags()
            if (response.isSuccessful && response.body()?.success == true) {
                return response.body()!!.data ?: emptyList()
            } else {
                val code = response.code()
                Log.w("ONBOARDING_REPO", "관심 태그 목록 조회 실패 | HTTP $code")
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "관심 태그 목록 조회 네트워크 오류", e)
        }
        return emptyList()
    }

    /**
     * 입력/생성한 닉네임 임시 저장
     * PATCH /api/onboarding/nickname
     */
    suspend fun updateNickname(nickname: String): String? {
        try {
            val response = RetrofitClient.onboardingApi.updateNickname(NicknameUpdateRequest(nickname))
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "닉네임 저장 성공: $nickname")
                return null // 에러 없음 -> 성공
            } else {
                val code = response.code()
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "닉네임 저장에 실패했습니다."
                Log.w("ONBOARDING_REPO", "닉네임 저장 실패 | HTTP $code | 에러: $errorMsg")
                return errorMsg
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "닉네임 저장 네트워크 오류", e)
            return "네트워크 연결에 실패했습니다. 다시 시도해 주세요."
        }
    }

    /**
     * 프로필 이미지 정보 저장
     * PATCH /api/onboarding/profile-image
     */
    suspend fun updateProfileImage(type: String, imageUrl: String): Boolean {
        val baseUrl = RetrofitClient.BASE_URL.removeSuffix("/")
        val finalUrl = if (type == "DEFAULT" && imageUrl.startsWith(baseUrl)) {
            imageUrl.substringAfter(baseUrl)
        } else {
            imageUrl
        }

        try {
            val response = RetrofitClient.onboardingApi.updateProfileImage(
                ProfileImageUpdateRequest(type = type, profileImageUrl = finalUrl)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "프로필 이미지 저장 성공: 타입=$type, 변환URL=$finalUrl")
                return true
            } else {
                val code = response.code()
                Log.w("ONBOARDING_REPO", "프로필 이미지 저장 실패 | HTTP $code")
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "프로필 이미지 저장 네트워크 오류", e)
        }
        return false
    }

    /**
     * 선택한 관심 태그 ID 목록 임시 저장
     * PATCH /api/onboarding/interest-tags
     */
    suspend fun updateInterestTags(tagIds: List<Long>): String? {
        try {
            val response = RetrofitClient.onboardingApi.updateInterestTags(InterestTagUpdateRequest(tagIds))
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("ONBOARDING_REPO", "관심 태그 저장 성공: 태그개수=${tagIds.size}")
                return null // 에러 없음 -> 성공
            } else {
                val code = response.code()
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "관심 태그 저장에 실패했습니다."
                Log.w("ONBOARDING_REPO", "관심 태그 저장 실패 | HTTP $code | 에러: $errorMsg")
                return errorMsg
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "관심 태그 저장 네트워크 오류", e)
            return "네트워크 연결에 실패했습니다. 다시 시도해 주세요."
        }
    }

    /**
     * 온보딩 최종 검증 및 완료 처리
     * POST /api/onboarding/complete
     */
    suspend fun completeOnboarding(): Result<OnboardingCompleteResponse> {
        try {
            val response = RetrofitClient.onboardingApi.complete()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) {
                    Log.i("ONBOARDING_REPO", "온보딩 완료 처리 성공")
                    return Result.success(data)
                }
            } else {
                val code = response.code()
                val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "온보딩 완료 처리에 실패했습니다."
                Log.w("ONBOARDING_REPO", "온보딩 완료 처리 실패 | HTTP $code | 에러: $errorMsg")
                return Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("ONBOARDING_REPO", "온보딩 완료 처리 네트워크 오류", e)
            return Result.failure(Exception("네트워크 연결에 실패했습니다. 다시 시도해 주세요."))
        }
        return Result.failure(Exception("온보딩 최종 처리 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요."))
    }
}

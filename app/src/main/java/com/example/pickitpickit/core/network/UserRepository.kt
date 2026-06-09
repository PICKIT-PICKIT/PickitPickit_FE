package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.MyPageProfileResponse
import com.example.pickitpickit.core.model.MyPageProfileUpdateRequest
import com.example.pickitpickit.core.network.RetrofitClient
import com.example.pickitpickit.core.model.FavoriteStoreResponse

class UserRepository {

    /**
     * 마이페이지 프로필 정보 조회
     * GET /api/users/me/profile
     */
    suspend fun getProfile(): MyPageProfileResponse? {
        return try {
            val response = RetrofitClient.userApi.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    apiResponse.data
                } else {
                    Log.e("USER_REPOSITORY", "프로필 조회 실패: ${apiResponse.message}")
                    null
                }
            } else {
                Log.e("USER_REPOSITORY", "프로필 조회 HTTP 에러: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "프로필 조회 예외 발생", e)
            null
        }
    }

    /**
     * 마이페이지 프로필 정보 수정
     * PATCH /api/users/me/profile
     */
    suspend fun updateProfile(request: MyPageProfileUpdateRequest): String? {
        return try {
            val response = RetrofitClient.userApi.updateProfile(request)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    null // 성공 시 에러 메시지 없음
                } else {
                    apiResponse.message
                }
            } else {
                "프로필 수정 실패 (HTTP ${response.code()})"
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "프로필 수정 예외 발생", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    /**
     * 회원 탈퇴 처리
     * DELETE /api/users/me
     */
    suspend fun deleteAccount(): String? {
        return try {
            val response = RetrofitClient.userApi.deleteAccount()
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    null // 성공 시 에러 메시지 없음
                } else {
                    apiResponse.message
                }
            } else {
                "회원 탈퇴 실패 (HTTP ${response.code()})"
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "회원 탈퇴 예외 발생", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    /**
     * 내 관심매장 목록 조회
     * GET /api/users/me/favorite-stores
     */
    suspend fun getFavoriteStores(lat: Double? = null, lng: Double? = null): List<FavoriteStoreResponse>? {
        return try {
            val response = RetrofitClient.userApi.getFavoriteStores(lat, lng)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    apiResponse.data
                } else {
                    Log.e("USER_REPOSITORY", "관심매장 조회 실패: ${apiResponse.message}")
                    null
                }
            } else {
                Log.e("USER_REPOSITORY", "관심매장 조회 HTTP 에러: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "관심매장 조회 예외 발생", e)
            null
        }
    }

    /**
     * 관심매장 추가
     * POST /api/users/me/favorite-stores/{storeId}
     */
    suspend fun addFavoriteStore(storeId: Long): FavoriteStoreResponse? {
        return try {
            val response = RetrofitClient.userApi.addFavoriteStore(storeId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    apiResponse.data
                } else {
                    Log.e("USER_REPOSITORY", "관심매장 추가 실패: ${apiResponse.message}")
                    null
                }
            } else {
                Log.e("USER_REPOSITORY", "관심매장 추가 HTTP 에러: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "관심매장 추가 예외 발생", e)
            null
        }
    }

    /**
     * 관심매장 삭제
     * DELETE /api/users/me/favorite-stores/{storeId}
     */
    suspend fun deleteFavoriteStore(storeId: Long): String? {
        return try {
            val response = RetrofitClient.userApi.deleteFavoriteStore(storeId)
            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!
                if (apiResponse.success) {
                    null // 성공 시 에러 메시지 없음
                } else {
                    apiResponse.message
                }
            } else {
                "관심매장 삭제 실패 (HTTP ${response.code()})"
            }
        } catch (e: Exception) {
            Log.e("USER_REPOSITORY", "관심매장 삭제 예외 발생", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }
}

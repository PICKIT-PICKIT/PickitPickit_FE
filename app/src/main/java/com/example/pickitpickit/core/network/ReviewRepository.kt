package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.ReviewRequest
import com.example.pickitpickit.core.model.ReviewPatchRequest
import com.example.pickitpickit.core.model.ReviewWriteGuideResponse
import com.example.pickitpickit.core.model.StoreReviewListResponse
import com.example.pickitpickit.core.model.ReviewDto

class ReviewRepository {

    suspend fun getStoreReviews(storeId: Long): StoreReviewListResponse? {
        return try {
            val response = RetrofitClient.reviewApi.getStoreReviews(storeId)
            if (response.isSuccessful && response.body()?.success == true) {
                val detail = response.body()!!.data
                Log.i("REVIEW_REPO", "리뷰 조회 성공: storeId=$storeId, reviewsCount=${detail?.reviews?.size ?: 0}")
                detail
            } else {
                Log.w("REVIEW_REPO", "리뷰 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "리뷰 조회 네트워크 에러 storeId=$storeId", e)
            null
        }
    }

    suspend fun postReview(request: ReviewRequest): String? {
        return try {
            val response = RetrofitClient.reviewApi.postReview(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("REVIEW_REPO", "리뷰 작성 성공: reviewId=${response.body()!!.data?.reviewId}")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "리뷰 등록에 실패했습니다.")
                    } catch (e: Exception) {
                        "리뷰 등록에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "리뷰 등록에 실패했습니다."
                }
                Log.w("REVIEW_REPO", "리뷰 작성 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "리뷰 작성 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun updateReview(reviewId: Long, request: ReviewPatchRequest): String? {
        return try {
            val response = RetrofitClient.reviewApi.updateReview(reviewId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("REVIEW_REPO", "리뷰 수정 성공: reviewId=$reviewId")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "리뷰 수정에 실패했습니다.")
                    } catch (e: Exception) {
                        "리뷰 수정에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "리뷰 수정에 실패했습니다."
                }
                Log.w("REVIEW_REPO", "리뷰 수정 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "리뷰 수정 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun deleteReview(reviewId: Long, userId: Long): String? {
        return try {
            val response = RetrofitClient.reviewApi.deleteReview(reviewId, userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("REVIEW_REPO", "리뷰 삭제 성공: reviewId=$reviewId")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "리뷰 삭제에 실패했습니다.")
                    } catch (e: Exception) {
                        "리뷰 삭제에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "리뷰 삭제에 실패했습니다."
                }
                Log.w("REVIEW_REPO", "리뷰 삭제 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "리뷰 삭제 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun getReviewWriteGuide(): ReviewWriteGuideResponse? {
        return try {
            val response = RetrofitClient.reviewApi.getReviewWriteGuide()
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("REVIEW_REPO", "리뷰 가이드 조회 성공")
                response.body()!!.data
            } else {
                Log.w("REVIEW_REPO", "리뷰 가이드 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "리뷰 가이드 조회 네트워크 에러", e)
            null
        }
    }

    suspend fun getUserReviews(): List<ReviewDto>? {
        return try {
            val response = RetrofitClient.reviewApi.getUserReviews()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()!!.data
                Log.i("REVIEW_REPO", "사용자 리뷰 조회 성공: count=${list?.size ?: 0}")
                list
            } else {
                Log.w("REVIEW_REPO", "사용자 리뷰 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("REVIEW_REPO", "사용자 리뷰 조회 네트워크 에러", e)
            null
        }
    }
}


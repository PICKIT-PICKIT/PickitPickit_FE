package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.BragDto
import com.example.pickitpickit.core.model.BragRequest
import com.example.pickitpickit.core.model.BragPatchRequest

class BragRepository {

    suspend fun postBrag(request: BragRequest): String? {
        return try {
            val response = RetrofitClient.bragApi.postBrag(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("BRAG_REPO", "자랑글 작성 성공: bragId=${response.body()!!.data?.bragId}")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "자랑글 등록에 실패했습니다.")
                    } catch (e: Exception) {
                        "자랑글 등록에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "자랑글 등록에 실패했습니다."
                }
                Log.w("BRAG_REPO", "자랑글 작성 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("BRAG_REPO", "자랑글 작성 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun updateBrag(bragId: Long, request: BragPatchRequest): String? {
        return try {
            val response = RetrofitClient.bragApi.updateBrag(bragId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("BRAG_REPO", "자랑글 수정 성공: bragId=$bragId")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "자랑글 수정에 실패했습니다.")
                    } catch (e: Exception) {
                        "자랑글 수정에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "자랑글 수정에 실패했습니다."
                }
                Log.w("BRAG_REPO", "자랑글 수정 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("BRAG_REPO", "자랑글 수정 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun deleteBrag(bragId: Long, userId: Long): String? {
        return try {
            val response = RetrofitClient.bragApi.deleteBrag(bragId, userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("BRAG_REPO", "자랑글 삭제 성공: bragId=$bragId")
                null // null means success
            } else {
                val errorMsg = if (!response.isSuccessful) {
                    val errorBodyStr = response.errorBody()?.string()
                    try {
                        val jsonObject = org.json.JSONObject(errorBodyStr ?: "")
                        jsonObject.optString("message", "자랑글 삭제에 실패했습니다.")
                    } catch (e: Exception) {
                        "자랑글 삭제에 실패했습니다."
                    }
                } else {
                    response.body()?.message ?: "자랑글 삭제에 실패했습니다."
                }
                Log.w("BRAG_REPO", "자랑글 삭제 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("BRAG_REPO", "자랑글 삭제 네트워크 에러", e)
            "네트워크 연결 상태를 확인해 주세요."
        }
    }

    suspend fun getAllBrags(): List<BragDto>? {
        return try {
            val response = RetrofitClient.bragApi.getAllBrags()
            if (response.isSuccessful && response.body()?.success == true) {
                val detail = response.body()!!.data
                Log.i("BRAG_REPO", "전체 자랑글 조회 성공: count=${detail?.size ?: 0}")
                detail
            } else {
                Log.w("BRAG_REPO", "전체 자랑글 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("BRAG_REPO", "전체 자랑글 조회 네트워크 에러", e)
            null
        }
    }

    suspend fun getUserBrags(): List<BragDto>? {
        return try {
            val response = RetrofitClient.bragApi.getUserBrags()
            if (response.isSuccessful && response.body()?.success == true) {
                val detail = response.body()!!.data
                Log.i("BRAG_REPO", "사용자 자랑글 조회 성공: count=${detail?.size ?: 0}")
                detail
            } else {
                Log.w("BRAG_REPO", "사용자 자랑글 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("BRAG_REPO", "사용자 자랑글 조회 네트워크 에러", e)
            null
        }
    }
}

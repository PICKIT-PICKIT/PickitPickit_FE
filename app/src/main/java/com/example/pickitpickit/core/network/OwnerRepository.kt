package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.ProductDto
import com.example.pickitpickit.core.model.StoreProductRegisterRequest
import com.example.pickitpickit.core.model.StoreProductUpdateRequest
import com.example.pickitpickit.core.network.api.ItemDto
import java.io.IOException

class OwnerRepository {

    private val ownerApi = RetrofitClient.ownerApi
    private val itemApi = RetrofitClient.itemApi

    /**
     * 상품 마스터 등록 (카테고리 한글 명칭을 영문 대문자 enum으로 매핑)
     * @return 성공 시 발급된 itemId, 실패 시 null
     */
    suspend fun registerItem(name: String, category: String, tags: List<String>): Pair<Long?, String?> {
        return try {
            val mappedCategory = when (category) {
                "인형" -> "PLUSH"
                "피규어" -> "FIGURE"
                "가챠" -> "GACHA"
                "키링" -> "KEYRING"
                "간식" -> "SNACK"
                else -> "ETC"
            }
            val request = com.example.pickitpickit.core.model.ItemCreateRequest(
                name = name,
                category = mappedCategory,
                defaultImageUrl = "",
                tags = tags
            )
            Log.i("OWNER_REPO", "상품 마스터 등록 요청: name=$name, category=$mappedCategory")
            val response = ownerApi.registerItem(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val registeredItemId = response.body()?.data?.itemId
                Log.i("OWNER_REPO", "상품 마스터 등록 성공: itemId=$registeredItemId")
                Pair(registeredItemId, null)
            } else {
                // 409 중복 자원 등 구체적인 에러 메시지가 들어있는 responseBody 또는 body 파싱
                val errorMsg = try {
                    val errorJson = response.errorBody()?.string()?.let { org.json.JSONObject(it) }
                    errorJson?.optString("message") ?: response.body()?.message
                } catch (e: Exception) {
                    response.body()?.message
                } ?: "상품 마스터 등록에 실패했습니다. (코드: ${response.code()})"
                
                Log.e("OWNER_REPO", "상품 마스터 등록 실패: code=${response.body()?.code}, message=$errorMsg")
                Pair(null, errorMsg)
            }
        } catch (e: Exception) {
            Log.e("OWNER_REPO", "상품 마스터 등록 네트워크 에러", e)
            Pair(null, "네트워크 연결이 원활하지 않습니다. 다시 시도해 주세요.")
        }
    }

    /**
     * 내 매장 상품 등록
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    suspend fun registerProduct(request: StoreProductRegisterRequest): String? {
        return try {
            Log.i("OWNER_REPO", "상품 등록 요청: storeId=${request.storeId}, itemId=${request.itemId}, price=${request.price}")
            val response = ownerApi.registerStoreProduct(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("OWNER_REPO", "상품 등록 성공: productId=${response.body()?.data?.productId}")
                null
            } else {
                val errorMsg = response.body()?.message ?: "상품 등록에 실패했습니다. (코드: ${response.code()})"
                Log.e("OWNER_REPO", "상품 등록 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("OWNER_REPO", "상품 등록 네트워크 에러", e)
            "네트워크 연결이 원활하지 않습니다. 다시 시도해 주세요."
        }
    }

    /**
     * 내 매장 상품 수정
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    suspend fun updateProduct(productId: Long, request: StoreProductUpdateRequest): String? {
        return try {
            Log.i("OWNER_REPO", "상품 수정 요청: productId=$productId, price=${request.price}, qty=${request.stockQuantity}")
            val response = ownerApi.updateStoreProduct(productId, request)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("OWNER_REPO", "상품 수정 성공: productId=$productId")
                null
            } else {
                val errorMsg = response.body()?.message ?: "상품 수정에 실패했습니다. (코드: ${response.code()})"
                Log.e("OWNER_REPO", "상품 수정 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("OWNER_REPO", "상품 수정 네트워크 에러", e)
            "네트워크 연결이 원활하지 않습니다. 다시 시도해 주세요."
        }
    }

    /**
     * 내 매장 상품 삭제
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    suspend fun deleteProduct(productId: Long): String? {
        return try {
            Log.i("OWNER_REPO", "상품 삭제 요청: productId=$productId")
            val response = ownerApi.deleteStoreProduct(productId)
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("OWNER_REPO", "상품 삭제 성공: productId=$productId")
                null
            } else {
                val errorMsg = response.body()?.message ?: "상품 삭제에 실패했습니다. (코드: ${response.code()})"
                Log.e("OWNER_REPO", "상품 삭제 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("OWNER_REPO", "상품 삭제 네트워크 에러", e)
            "네트워크 연결이 원활하지 않습니다. 다시 시도해 주세요."
        }
    }

    suspend fun searchItems(query: String): List<ItemDto> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            Log.i("OWNER_REPO", "아이템 검색 서버 호출 시작: query='$trimmed'")
            // 1. /api/items/search 호출 시도
            val response = itemApi.searchItems(trimmed)
            if (response.isSuccessful && response.body()?.success == true) {
                val results = response.body()?.data ?: emptyList()
                Log.i("OWNER_REPO", "아이템 검색(/search) 성공: ${results.size}건")
                if (results.isNotEmpty()) return results
            }
            
            // 2. 실패 혹은 빈값일 때 /api/items 호출 시도
            val altResponse = itemApi.getItems(trimmed)
            if (altResponse.isSuccessful && altResponse.body()?.success == true) {
                val results = altResponse.body()?.data ?: emptyList()
                Log.i("OWNER_REPO", "아이템 검색(/items) 성공: ${results.size}건")
                if (results.isNotEmpty()) return results
            }

            Log.w("OWNER_REPO", "아이템 검색 서버 결과 없음, 빈 목록 반환")
            emptyList()
        } catch (e: Exception) {
            Log.w("OWNER_REPO", "아이템 검색 네트워크 에러: ${e.message}, 빈 목록 반환")
            emptyList()
        }
    }

    /**
     * 내 매장 대표 태그 교체
     * @return 성공 시 null, 실패 시 에러 메시지
     */
    suspend fun updateStoreTags(storeId: Long, tags: List<String>): String? {
        return try {
            Log.i("OWNER_REPO", "대표 태그 교체 요청: storeId=$storeId, tags=$tags")
            val response = ownerApi.updateStoreTags(
                storeId = storeId,
                body = com.example.pickitpickit.core.model.StoreTagsUpdateRequest(tags)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("OWNER_REPO", "대표 태그 교체 성공: storeId=$storeId")
                null
            } else {
                val errorMsg = response.body()?.message ?: "대표 태그 교체에 실패했습니다. (코드: ${response.code()})"
                Log.e("OWNER_REPO", "대표 태그 교체 실패: $errorMsg")
                errorMsg
            }
        } catch (e: Exception) {
            Log.e("OWNER_REPO", "대표 태그 교체 네트워크 에러 (storeId=$storeId)", e)
            "네트워크 연결이 원활하지 않습니다. 다시 시도해 주세요."
        }
    }
}


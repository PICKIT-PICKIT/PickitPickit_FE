package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.ui.home.StoreItem
import com.example.pickitpickit.ui.map.MapCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StoreRepository {

    /**
     * 내 주변 추천 매장 조회
     *
     * @param lat    사용자 위도
     * @param lng    사용자 경도
     * @param radius 탐색 반경(m)
     * @param type   매장 종류 ("ALL", "CLAW", "GACHA")
     * @return UI에 노출할 StoreItem 리스트 (실패 시 빈 리스트)
     */
    suspend fun getNearbyStores(
        lat: Double,
        lng: Double,
        radius: Int,
        type: String = "ALL"
    ): List<StoreItem> {
        return try {
            val response = RetrofitClient.storeApi.getNearbyStores(
                lat = lat,
                lng = lng,
                radius = radius,
                type = type
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()!!.data ?: emptyList()
                Log.i("STORE_REPO", "주변 매장 조회 성공: ${dtoList.size}건 수신 (radius: ${radius}m)")
                
                val reviewRepository = ReviewRepository()
                
                coroutineScope {
                    val deferredStores = dtoList.map { dto ->
                        async {
                            val mappedCategory = when (dto.type.uppercase()) {
                                "CLAW" -> MapCategory.CLAW_MACHINE
                                "GACHA" -> MapCategory.GACHA
                                else -> MapCategory.MIXED
                            }

                            // 1. 리뷰 데이터 비동기 조회 (실제 평점 및 리뷰 개수 연동)
                            val reviewData = reviewRepository.getStoreReviews(dto.id.toLong())
                            val realRating = reviewData?.averageRating?.toFloat() ?: 0.0f
                            val realReviewCount = reviewData?.reviewCount ?: 0

                            // 2. 매장 상세 정보 비동기 조회를 통해 실제 등록된 태그 수신
                            val detailData = getStoreDetail(dto.id.toLong(), lat, lng)
                            val realTags = detailData?.tags?.map { it.name } ?: when (dto.type.uppercase()) {
                                "CLAW" -> listOf("인형뽑기", "크레인게임")
                                "GACHA" -> listOf("가챠", "캡슐토이", "피규어")
                                else -> listOf("인형뽑기", "가챠", "종합샵")
                            }

                            StoreItem(
                                id = dto.id,
                                name = dto.name,
                                category = mappedCategory,
                                rating = realRating,
                                reviewCount = realReviewCount,
                                address = dto.address ?: "주소 정보 없음",
                                hours = dto.businessHours ?: "영업시간 정보 없음",
                                tags = realTags,
                                distanceMeters = dto.distance,
                                latitude = dto.latitude,
                                longitude = dto.longitude,
                                mainImageUrl = dto.mainImageUrl
                            )
                        }
                    }
                    deferredStores.awaitAll()
                }
            } else {
                Log.w("STORE_REPO", "주변 매장 조회 실패: ${response.body()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("STORE_REPO", "주변 매장 조회 네트워크 오류", e)
            emptyList()
        }
    }

    /**
     * 매장 상세 조회 (등록된 상품/재고/태그 목록 포함)
     *
     * @param storeId 매장 ID
     * @param lat     사용자 위도 (선택)
     * @param lng     사용자 경도 (선택)
     * @return 매장 상세 정보 응답 DTO (실패 시 null)
     */
    suspend fun getStoreDetail(
        storeId: Long,
        lat: Double? = null,
        lng: Double? = null
    ): com.example.pickitpickit.core.model.StoreDetailResponse? {
        return try {
            val response = RetrofitClient.storeApi.getStoreDetail(
                storeId = storeId,
                lat = lat,
                lng = lng
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val detail = response.body()!!.data
                Log.i("STORE_REPO", "매장 상세 조회 성공: ${detail?.store?.name ?: ""} (ID: $storeId)")
                detail
            } else {
                Log.w("STORE_REPO", "매장 상세 조회 실패: ${response.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("STORE_REPO", "매장 상세 조회 네트워크 오류 (ID: $storeId)", e)
            null
        }
    }

    /**
     * 매장 검색 (매장명 또는 주소 기준)
     *
     * @param keyword 검색어 (필수)
     * @param type    매장 유형 ("ALL", "CLAW", "GACHA")
     * @param lat     사용자 위도 (선택)
     * @param lng     사용자 경도 (선택)
     * @param limit   제한 개수 (1~50)
     * @return 검색된 StoreItem 리스트 (실패 시 빈 리스트)
     */
    suspend fun searchStores(
        keyword: String,
        type: String = "ALL",
        lat: Double? = null,
        lng: Double? = null,
        limit: Int = 20
    ): List<StoreItem> {
        return try {
            val response = RetrofitClient.storeApi.searchStores(
                keyword = keyword,
                type = type,
                lat = lat,
                lng = lng,
                limit = limit
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()!!.data ?: emptyList()
                Log.i("STORE_REPO", "매장 검색 성공: ${dtoList.size}건 수신 (keyword: $keyword)")

                val reviewRepository = ReviewRepository()

                coroutineScope {
                    val deferredStores = dtoList.map { dto ->
                        async {
                            val mappedCategory = when (dto.type.uppercase()) {
                                "CLAW" -> MapCategory.CLAW_MACHINE
                                "GACHA" -> MapCategory.GACHA
                                else -> MapCategory.MIXED
                            }

                            // 1. 리뷰 데이터 비동기 조회 (실제 평점 및 리뷰 개수 연동)
                            val reviewData = reviewRepository.getStoreReviews(dto.id.toLong())
                            val realRating = reviewData?.averageRating?.toFloat() ?: 0.0f
                            val realReviewCount = reviewData?.reviewCount ?: 0

                            // 2. 매장 상세 정보 비동기 조회를 통해 실제 등록된 태그 수신
                            val detailData = getStoreDetail(dto.id.toLong(), lat, lng)
                            val realTags = detailData?.tags?.map { it.name } ?: when (dto.type.uppercase()) {
                                "CLAW" -> listOf("인형뽑기", "크레인게임")
                                "GACHA" -> listOf("가챠", "캡슐토이", "피규어")
                                else -> listOf("인형뽑기", "가챠", "종합샵")
                            }

                            StoreItem(
                                id = dto.id,
                                name = dto.name,
                                category = mappedCategory,
                                rating = realRating,
                                reviewCount = realReviewCount,
                                address = dto.address ?: "주소 정보 없음",
                                hours = dto.businessHours ?: "영업시간 정보 없음",
                                tags = realTags,
                                distanceMeters = dto.distance,
                                latitude = dto.latitude,
                                longitude = dto.longitude,
                                mainImageUrl = dto.mainImageUrl
                            )
                        }
                    }
                    deferredStores.awaitAll()
                }
            } else {
                Log.w("STORE_REPO", "매장 검색 실패: ${response.body()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("STORE_REPO", "매장 검색 네트워크 오류 (keyword: $keyword)", e)
            emptyList()
        }
    }
}

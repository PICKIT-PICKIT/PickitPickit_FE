package com.example.pickitpickit.core.network.api

import com.example.pickitpickit.core.model.ApiResponse
import com.example.pickitpickit.core.model.DeleteAllSearchLogsRequest
import com.example.pickitpickit.core.model.DeleteSearchLogRequest
import com.example.pickitpickit.core.model.SaveSearchLogRequest
import com.example.pickitpickit.core.model.SearchLogDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApi {

    /**
     * 검색 로그 저장
     * POST /api/search-logs
     *
     * 검색 실행 시 호출 → 서버에 키워드 기록
     * Request: { userId, keyword, targetType }
     */
    @POST("api/search-logs")
    suspend fun saveSearchLog(
        @Body body: SaveSearchLogRequest
    ): Response<ApiResponse<String>>

    /**
     * 최근 검색어 조회
     * GET /api/search-logs/recent?userId={}&limit={}
     *
     * 같은 검색어+대상 조합은 가장 최근 1건만 노출
     * @param userId 사용자 ID (필수)
     * @param limit  조회 개수 1~20, 기본값 10
     */
    @GET("api/search-logs/recent")
    suspend fun getRecentSearchLogs(
        @Query("userId") userId: Long,
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<SearchLogDto>>>

    /**
     * 최근 검색어 개별 삭제
     * DELETE /api/search-logs/recent
     *
     * 특정 키워드+타입 조합 삭제
     * Request: Query parameters (userId, keyword, targetType)
     */
    @DELETE("api/search-logs/recent")
    suspend fun deleteSearchLog(
        @Query("userId") userId: Long,
        @Query("keyword") keyword: String,
        @Query("targetType") targetType: String
    ): Response<ApiResponse<String>>

    /**
     * 최근 검색어 전체 삭제
     * DELETE /api/search-logs/recent/all
     *
     * 해당 유저의 검색 로그 전부 삭제
     * Request: Query parameters (userId)
     */
    @DELETE("api/search-logs/recent/all")
    suspend fun deleteAllSearchLogs(
        @Query("userId") userId: Long
    ): Response<ApiResponse<String>>
}

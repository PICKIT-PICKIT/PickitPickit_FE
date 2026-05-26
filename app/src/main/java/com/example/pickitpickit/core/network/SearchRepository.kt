package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.model.DeleteAllSearchLogsRequest
import com.example.pickitpickit.core.model.DeleteSearchLogRequest
import com.example.pickitpickit.core.model.SaveSearchLogRequest
import com.example.pickitpickit.core.model.SearchLogDto
import com.example.pickitpickit.core.model.TargetType

/**
 * 검색 로그 관련 API 호출을 담당하는 Repository
 *
 * - 검색 실행 시 로그 저장  (saveSearchLog)
 * - 최근 검색어 조회        (getRecentSearchLogs)
 * - 최근 검색어 개별 삭제   (deleteSearchLog)
 * - 최근 검색어 전체 삭제   (deleteAllSearchLogs)
 */
class SearchRepository {

    // ──────────────────────────────────────────────────────────────
    // 검색 로그 저장
    // POST /api/search-logs
    // ──────────────────────────────────────────────────────────────

    /**
     * 검색 실행 시 서버에 로그 저장
     *
     * @param userId     현재 로그인한 유저 ID
     * @param keyword    검색어
     * @param targetType 검색 대상 타입 (기본값 STORE)
     * @return 성공 여부
     */
    suspend fun saveSearchLog(
        userId: Long,
        keyword: String,
        targetType: TargetType = TargetType.STORE
    ): Boolean {
        return try {
            val response = RetrofitClient.searchApi.saveSearchLog(
                SaveSearchLogRequest(
                    userId = userId,
                    keyword = keyword,
                    targetType = targetType.name
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("SEARCH_REPO", "검색 로그 저장 성공: $keyword")
                true
            } else {
                Log.w("SEARCH_REPO", "검색 로그 저장 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("SEARCH_REPO", "검색 로그 저장 네트워크 오류", e)
            false
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 최근 검색어 조회
    // GET /api/search-logs/recent?userId={}&limit={}
    // ──────────────────────────────────────────────────────────────

    /**
     * 최근 검색어 목록 조회 (최신순, 중복 제거)
     *
     * @param userId 현재 로그인한 유저 ID
     * @param limit  조회 개수 (1~20, 기본값 10)
     * @return 검색 로그 리스트 (실패 시 빈 리스트)
     */
    suspend fun getRecentSearchLogs(
        userId: Long,
        limit: Int = 10
    ): List<SearchLogDto> {
        return try {
            val response = RetrofitClient.searchApi.getRecentSearchLogs(
                userId = userId,
                limit = limit
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val logs = response.body()!!.data ?: emptyList()
                Log.i("SEARCH_REPO", "최근 검색어 조회 성공: ${logs.size}건")
                logs
            } else {
                Log.w("SEARCH_REPO", "최근 검색어 조회 실패: ${response.body()?.message}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SEARCH_REPO", "최근 검색어 조회 네트워크 오류", e)
            emptyList()
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 최근 검색어 개별 삭제
    // DELETE /api/search-logs/recent
    // ──────────────────────────────────────────────────────────────

    /**
     * 특정 키워드 검색 로그 삭제
     *
     * @param userId     현재 로그인한 유저 ID
     * @param keyword    삭제할 검색어
     * @param targetType 검색 대상 타입 (기본값 STORE)
     * @return 성공 여부
     */
    suspend fun deleteSearchLog(
        userId: Long,
        keyword: String,
        targetType: TargetType = TargetType.STORE
    ): Boolean {
        return try {
            val response = RetrofitClient.searchApi.deleteSearchLog(
                userId = userId,
                keyword = keyword,
                targetType = targetType.name
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("SEARCH_REPO", "검색 로그 개별 삭제 성공: $keyword")
                true
            } else {
                Log.w("SEARCH_REPO", "검색 로그 개별 삭제 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("SEARCH_REPO", "검색 로그 개별 삭제 네트워크 오류", e)
            false
        }
    }

    // ──────────────────────────────────────────────────────────────
    // 최근 검색어 전체 삭제
    // DELETE /api/search-logs/recent/all
    // ──────────────────────────────────────────────────────────────

    /**
     * 해당 유저의 검색 로그 전부 삭제
     *
     * @param userId 현재 로그인한 유저 ID
     * @return 성공 여부
     */
    suspend fun deleteAllSearchLogs(userId: Long): Boolean {
        return try {
            val response = RetrofitClient.searchApi.deleteAllSearchLogs(
                userId = userId
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Log.i("SEARCH_REPO", "검색 로그 전체 삭제 성공")
                true
            } else {
                Log.w("SEARCH_REPO", "검색 로그 전체 삭제 실패: ${response.body()?.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("SEARCH_REPO", "검색 로그 전체 삭제 네트워크 오류", e)
            false
        }
    }
}

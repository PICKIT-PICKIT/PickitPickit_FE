package com.example.pickitpickit.core.model

// ──────────────────────────────────────────────────────────────────────────────
// 검색 로그 API 모델
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 검색 대상 타입
 */
enum class TargetType {
    STORE  // 현재는 STORE만 사용
}

/**
 * 검색 로그 저장 요청 Body
 * POST /api/search-logs
 */
data class SaveSearchLogRequest(
    val userId: Long,
    val keyword: String,
    val targetType: String = TargetType.STORE.name
)

/**
 * 최근 검색어 항목
 * GET /api/search-logs/recent 응답의 data 배열 아이템
 */
data class SearchLogDto(
    val keyword: String,
    val targetType: String,     // "STORE" 등
    val searchedAt: String      // ISO 8601 (e.g. "2026-05-26T06:42:32.451Z")
)

/**
 * 최근 검색어 개별 삭제 요청 Body
 * DELETE /api/search-logs/recent
 */
data class DeleteSearchLogRequest(
    val userId: Long,
    val keyword: String,
    val targetType: String = TargetType.STORE.name
)

/**
 * 최근 검색어 전체 삭제 요청 Body
 * DELETE /api/search-logs/recent/all
 */
data class DeleteAllSearchLogsRequest(
    val userId: Long
)

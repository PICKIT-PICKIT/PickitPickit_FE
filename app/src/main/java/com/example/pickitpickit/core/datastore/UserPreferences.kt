package com.example.pickitpickit.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore 인스턴스 생성 (Context의 확장 프로퍼티)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    // ──────────────────────────────────────────────────────────────
    // 저장 키 정의
    // ──────────────────────────────────────────────────────────────

    private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
    private val PUSH_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("push_notifications_enabled")

    // TODO: 백엔드 명세 확인 후 키 이름 변경 가능
    private val ACCESS_TOKEN_KEY  = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val USER_ID_KEY       = longPreferencesKey("user_id")

    // ──────────────────────────────────────────────────────────────
    // 온보딩 및 설정 상태
    // ──────────────────────────────────────────────────────────────

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED_KEY] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
    }

    /** 로컬 푸시 알림 설정 여부 (기본값 true) */
    val isPushNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PUSH_NOTIFICATIONS_ENABLED_KEY] ?: true }

    /** 로컬 푸시 알림 설정 값 변경 */
    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PUSH_NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    // ──────────────────────────────────────────────────────────────
    // JWT 토큰 저장 / 읽기 / 삭제
    // ──────────────────────────────────────────────────────────────

    /** Access Token 읽기 (없으면 null) */
    fun getAccessToken(): Flow<String?> = context.dataStore.data
         .map { it[ACCESS_TOKEN_KEY] }

    /** Refresh Token 읽기 (없으면 null) */
    fun getRefreshToken(): Flow<String?> = context.dataStore.data
         .map { it[REFRESH_TOKEN_KEY] }

    /** User ID 읽기 (없으면 null) */
    fun getUserId(): Flow<Long?> = context.dataStore.data
         .map { it[USER_ID_KEY] }

    /** 로그인 성공 시 토큰 저장 및 userId 저장 */
    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: Long? = null) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY]  = accessToken
            prefs[REFRESH_TOKEN_KEY] = refreshToken
            if (userId != null) {
                prefs[USER_ID_KEY] = userId
            }
        }
    }

    /** 로그아웃 시 토큰 및 유저 정보 삭제 & 온보딩 완료 상태 리셋 (테스트 및 재진입 지원) */
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN_KEY)
            prefs.remove(REFRESH_TOKEN_KEY)
            prefs.remove(USER_ID_KEY)
            prefs[ONBOARDING_COMPLETED_KEY] = false
        }
    }

    /** 로그아웃 시 모든 사용자 데이터 초기화 */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}


package com.example.pickitpickit.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    // TODO: 백엔드 명세 확인 후 키 이름 변경 가능
    private val ACCESS_TOKEN_KEY  = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")

    // ──────────────────────────────────────────────────────────────
    // 온보딩 완료 상태
    // ──────────────────────────────────────────────────────────────

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[ONBOARDING_COMPLETED_KEY] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
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

    /** 로그인 성공 시 토큰 저장 */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN_KEY]  = accessToken
            it[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    /** 로그아웃 시 토큰 삭제 */
    suspend fun clearTokens() {
        context.dataStore.edit {
            it.remove(ACCESS_TOKEN_KEY)
            it.remove(REFRESH_TOKEN_KEY)
        }
    }

    /** 로그아웃 시 모든 사용자 데이터 초기화 */
    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}


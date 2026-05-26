package com.example.pickitpickit.core.network

import com.example.pickitpickit.core.datastore.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * JWT Access Token을 모든 API 요청 헤더에 자동으로 삽입하는 OkHttp Interceptor
 *
 * Authorization: Bearer {accessToken} 형태로 추가
 * 토큰이 없는 경우(로그인 전)에는 헤더를 추가하지 않음
 */
class AuthInterceptor(
    private val userPreferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            userPreferences.getAccessToken().firstOrNull()
        }

        val request = if (token.isNullOrEmpty()) {
            // 토큰 없음 → 헤더 없이 그대로 전송 (로그인 요청 등)
            chain.request()
        } else {
            // 토큰 있음 → Authorization 헤더 추가
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }
}

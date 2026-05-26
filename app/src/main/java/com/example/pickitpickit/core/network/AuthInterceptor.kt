package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
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
        // 🌟 Dispatchers.IO 컨텍스트를 명시 지정하여 OkHttp와 DataStore 간의 스레드 교착 상태(Deadlock) 원천 차단!
        // 🌟 2초 타임아웃 보호막(withTimeoutOrNull)을 두어 혹시 모를 대기 지연 시의 앱 먹통 가능성까지 이중 방어!
        val token = runBlocking(Dispatchers.IO) {
            try {
                withTimeoutOrNull(2000) {
                    userPreferences.getAccessToken().first()
                }
            } catch (e: Exception) {
                Log.e("AUTH_INTERCEPTOR", "DataStore 토큰 로딩 중 예외 발생", e)
                null
            }
        }

        Log.d("AUTH_INTERCEPTOR", "API 요청 가로챔 | 주입할 AccessToken 존재 여부 = ${!token.isNullOrEmpty()}")

        val request = if (token.isNullOrEmpty()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }

        return chain.proceed(request)
    }
}

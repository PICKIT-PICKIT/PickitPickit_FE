package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

/**
 * JWT Access Token을 모든 API 요청 헤더에 자동으로 삽입하고,
 * 401 만료 에러 발생 시 동기식으로 토큰을 자동 재발급하여 재시도 처리하는 OkHttp Interceptor
 */
class AuthInterceptor(
    private val userPreferences: UserPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 🌟 Dispatchers.IO 컨텍스트를 명시 지정하여 OkHttp와 DataStore 간의 스레드 교착 상태(Deadlock) 원천 차단!
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

        val request = chain.request()
        val requestBuilder = request.newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // 401 Unauthorized인 경우 토큰 재발급 자동 시도
        if (response.code == 401) {
            val urlPath = request.url.encodedPath
            // 무한 루프 방지: 로그인/재발급/로그아웃 관련 요청은 재발급 대상에서 제외
            if (!urlPath.contains("/api/auth/token/reissue") && 
                !urlPath.contains("/api/auth/kakao/login") && 
                !urlPath.contains("/api/auth/logout")
            ) {
                synchronized(this) {
                    // 재발급 시도 전, 다른 스레드에서 이미 토큰이 갱신되었는지 최신 토큰 확인
                    val currentToken = runBlocking(Dispatchers.IO) {
                        try {
                            withTimeoutOrNull(1000) {
                                userPreferences.getAccessToken().first()
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (!currentToken.isNullOrEmpty() && currentToken != token) {
                        // 다른 스레드에서 이미 토큰이 갱신된 경우, 새 토큰으로 바로 재시도
                        response.close()
                        val newRequest = request.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                        return chain.proceed(newRequest)
                    }

                    // 토큰 재발급 진행을 위해 Refresh Token 획득
                    val refreshToken = runBlocking(Dispatchers.IO) {
                        try {
                            withTimeoutOrNull(1000) {
                                userPreferences.getRefreshToken().first()
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (!refreshToken.isNullOrEmpty()) {
                        Log.w("AUTH_INTERCEPTOR", "토큰 만료 감지 (HTTP 401) -> 토큰 동기식 재발급 시도 시작 🔄")
                        val isReissueSuccess = reissueTokenSynchronously(refreshToken)
                        if (isReissueSuccess) {
                            val nextToken = runBlocking(Dispatchers.IO) {
                                try {
                                    withTimeoutOrNull(1000) {
                                        userPreferences.getAccessToken().first()
                                    }
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (!nextToken.isNullOrEmpty()) {
                                Log.i("AUTH_INTERCEPTOR", "토큰 재발급 성공! 새로운 AccessToken으로 원래 요청 재시도 진행 🚀")
                                response.close()
                                val newRequest = request.newBuilder()
                                    .header("Authorization", "Bearer $nextToken")
                                    .build()
                                return chain.proceed(newRequest)
                            }
                        } else {
                            Log.e("AUTH_INTERCEPTOR", "토큰 재발급 실패 -> 만료된 세션 처리 필요")
                        }
                    }
                }
            }
        }

        return response
    }

    /**
     * 동기식으로 토큰 재발급 요청 진행 (순수 OkHttpClient를 사용하여 인터셉터 루프 방지)
     */
    private fun reissueTokenSynchronously(refreshToken: String): Boolean {
        try {
            val cleanClient = OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val jsonBody = JSONObject().apply {
                put("refreshToken", refreshToken)
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val reissueRequest = Request.Builder()
                .url(RetrofitClient.BASE_URL.removeSuffix("/") + "/api/auth/token/reissue")
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            val response = cleanClient.newCall(reissueRequest).execute()
            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string()
                if (!responseBodyStr.isNullOrEmpty()) {
                    val rootJson = JSONObject(responseBodyStr)
                    if (rootJson.optBoolean("success", false)) {
                        val dataJson = rootJson.optJSONObject("data")
                        if (dataJson != null) {
                            val newAccessToken = dataJson.optString("accessToken")
                            val newRefreshToken = dataJson.optString("refreshToken")
                            if (!newAccessToken.isNullOrEmpty() && !newRefreshToken.isNullOrEmpty()) {
                                runBlocking(Dispatchers.IO) {
                                    userPreferences.saveTokens(newAccessToken, newRefreshToken)
                                }
                                Log.i("AUTH_INTERCEPTOR", "동기식 토큰 재발급 및 로컬 DataStore 저장 완료 완료 🏆")
                                return true
                            }
                        }
                    }
                }
            }
            Log.e("AUTH_INTERCEPTOR", "동기식 토큰 재발급 실패 | HTTP ${response.code}")
        } catch (e: Exception) {
            Log.e("AUTH_INTERCEPTOR", "동기식 토큰 재발급 중 예외 발생", e)
        }
        return false
    }
}

package com.example.pickitpickit.core.network

import android.util.Log
import com.example.pickitpickit.core.datastore.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    init {
        // 백그라운드 코루틴을 통해 DataStore Flow 데이터를 인메모리 캐시 변수로 상시 실시간 갱신
        scope.launch {
            try {
                userPreferences.getAccessToken().collect { token ->
                    cachedAccessToken = token
                }
            } catch (e: Exception) {
                Log.e("AUTH_INTERCEPTOR", "Access Token 캐시 갱신 실패", e)
            }
        }
        scope.launch {
            try {
                userPreferences.getRefreshToken().collect { token ->
                    cachedRefreshToken = token
                }
            } catch (e: Exception) {
                Log.e("AUTH_INTERCEPTOR", "Refresh Token 캐시 갱신 실패", e)
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1. 인메모리 캐시를 우선 확인하여 runBlocking 및 DataStore Lock에 의한 데드락 완전 회피
        var token = cachedAccessToken
        if (token == null) {
            // 앱 초기 구동 시점 극초기 등 캐시 수집 전인 경우에만 1회 동기식 블락 조회
            token = runBlocking(Dispatchers.IO) {
                try {
                    withTimeoutOrNull(500) {
                        userPreferences.getAccessToken().first()
                    }
                } catch (e: Exception) {
                    Log.e("AUTH_INTERCEPTOR", "최초 DataStore 토큰 로딩 중 예외 발생", e)
                    null
                }
            }
            cachedAccessToken = token
        }

        val request = chain.request()
        val requestBuilder = request.newBuilder()
        val urlPath = request.url.encodedPath

        // 로그인, 토큰 재발급, 로그아웃 등 인증이 필요 없는 API는 Authorization 헤더 추가 방지
        val isNoAuthApi = urlPath.contains("/api/auth/kakao/login") || 
                          urlPath.contains("/api/auth/token/reissue") ||
                          urlPath.contains("/api/auth/logout")

        if (!token.isNullOrEmpty() && !isNoAuthApi) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        // 401 Unauthorized인 경우 토큰 재발급 자동 시도
        if (response.code == 401) {
            val retryUrlPath = request.url.encodedPath
            // 무한 루프 방지: 로그인/재발급/로그아웃 관련 요청은 재발급 대상에서 제외
            if (!retryUrlPath.contains("/api/auth/token/reissue") && 
                !retryUrlPath.contains("/api/auth/kakao/login") && 
                !retryUrlPath.contains("/api/auth/logout")
            ) {
                synchronized(this) {
                    // 재발급 시도 전, 다른 스레드에서 이미 토큰이 갱신되었는지 최신 캐시 토큰 확인
                    val currentToken = cachedAccessToken ?: runBlocking(Dispatchers.IO) {
                        try {
                            withTimeoutOrNull(500) {
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
                    val refreshToken = cachedRefreshToken ?: runBlocking(Dispatchers.IO) {
                        try {
                            withTimeoutOrNull(500) {
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
                            val nextToken = cachedAccessToken
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

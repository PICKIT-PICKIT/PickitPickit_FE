package com.example.pickitpickit.core.network

import com.example.pickitpickit.core.network.api.AuthApi
import com.example.pickitpickit.core.network.api.SearchApi
import com.example.pickitpickit.core.network.api.OnboardingApi
import com.example.pickitpickit.core.network.api.StoreApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 싱글톤 클라이언트
 *
 * TODO: 백엔드 팀에서 서버 주소 받으면 BASE_URL 수정 필요
 *   - 개발 서버: "http://개발서버IP:포트/"
 *   - 운영 서버: "https://api.pickitpickit.com/"  (예시)
 */
object RetrofitClient {

    const val BASE_URL = "https://pickit-pickit.site/"

    /**
     * AuthInterceptor를 주입해 클라이언트를 생성
     * Application이 초기화된 후 GlobalApplication에서 호출
     */
    fun create(authInterceptor: AuthInterceptor): RetrofitClient {
        return RetrofitClient.also {
            it.authInterceptor = authInterceptor
            it.initialize()
        }
    }

    private lateinit var authInterceptor: AuthInterceptor
    private lateinit var retrofit: Retrofit

    private fun initialize() {
        // 디버그용 로그 인터셉터 (Release 빌드에서는 NONE으로 자동 설정)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // JWT 자동 헤더 삽입
            .addInterceptor(loggingInterceptor) // 요청/응답 Logcat 출력
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ──────────────────────────────────────────────────────────────
    // API 인터페이스 접근자 (필요한 API 추가 시 여기에 추가)
    // ──────────────────────────────────────────────────────────────

    val authApi: AuthApi   by lazy { retrofit.create(AuthApi::class.java) }
    val searchApi: SearchApi by lazy { retrofit.create(SearchApi::class.java) }
    val onboardingApi: OnboardingApi by lazy { retrofit.create(OnboardingApi::class.java) }
    val storeApi: StoreApi by lazy { retrofit.create(StoreApi::class.java) }
}

package com.example.pickitpickit

import android.app.Application
import com.example.pickitpickit.core.datastore.UserPreferences
import com.example.pickitpickit.core.network.AuthInterceptor
import com.example.pickitpickit.core.network.RetrofitClient
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk

class GlobalApplication : Application() {
    companion object {
        lateinit var instance: GlobalApplication
            private set

        lateinit var userPreferences: UserPreferences
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, "4597cdb7fc04bfb5aca3e2f07d375aa7")

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, "4597cdb7fc04bfb5aca3e2f07d375aa7")

        // Retrofit 클라이언트 초기화 (JWT 자동 헤더 삽입 포함)
        userPreferences = UserPreferences(this)
        val authInterceptor = AuthInterceptor(userPreferences)
        RetrofitClient.create(authInterceptor)
    }
}

package com.example.pickitpickit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 카카오 로그인 SDK 초기화
        KakaoSdk.init(this, "4597cdb7fc04bfb5aca3e2f07d375aa7")

        // 카카오맵 SDK 초기화
        KakaoMapSdk.init(this, "4597cdb7fc04bfb5aca3e2f07d375aa7")
    }
}

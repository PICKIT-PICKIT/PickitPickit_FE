package com.example.pickitpickit

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 카카오 SDK 초기화
        KakaoSdk.init(this, "4597cdb7fc04bfb5aca3e2f07d375aa7")
    }
}

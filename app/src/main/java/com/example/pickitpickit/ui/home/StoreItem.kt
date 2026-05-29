package com.example.pickitpickit.ui.home

import com.example.pickitpickit.ui.map.MapCategory

data class StoreItem(
    val id: Int,
    val name: String,
    val category: MapCategory,
    val rating: Float,
    val reviewCount: Int,
    val address: String,
    val hours: String,
    val tags: List<String>,
    val distanceMeters: Int,
    val latitude: Double,
    val longitude: Double,
    val mainImageUrl: String? = null
)

// 더미 데이터 (백엔드 연동 전 UI 테스트용 및 미리보기 프리뷰용)
val dummyStores = listOf(
    StoreItem(
        id = 1,
        name = "캐치팡 홍대입구점",
        category = MapCategory.CLAW_MACHINE,
        rating = 4.5f,
        reviewCount = 120,
        address = "서울특별시 마포구 양화로 16",
        hours = "10:00 - 23:00",
        tags = listOf("포켓몬", "디즈니", "원피스", "+1"),
        distanceMeters = 90,
        latitude = 37.5565,
        longitude = 126.9235
    ),
    StoreItem(
        id = 2,
        name = "미니돌 홍대점",
        category = MapCategory.CLAW_MACHINE,
        rating = 4.3f,
        reviewCount = 90,
        address = "서울특별시 마포구 홍익로 4",
        hours = "11:00 - 24:00",
        tags = listOf("산리오", "원피스", "짱구", "+1"),
        distanceMeters = 125,
        latitude = 37.5570,
        longitude = 126.9240
    ),
    StoreItem(
        id = 3,
        name = "퍼니랜드 홍대2호점",
        category = MapCategory.MIXED,
        rating = 4.7f,
        reviewCount = 150,
        address = "서울특별시 마포구 어울마당로 7",
        hours = "10:00 - 23:00",
        tags = listOf("포켓몬", "귀멸의칼날"),
        distanceMeters = 231,
        latitude = 37.5560,
        longitude = 126.9228
    ),
    StoreItem(
        id = 4,
        name = "프라이즈존 홍대AK점",
        category = MapCategory.CLAW_MACHINE,
        rating = 4.5f,
        reviewCount = 120,
        address = "서울특별시 마포구 양화로 18",
        hours = "11:00 - 23:00",
        tags = listOf("BT21", "카카오프렌즈"),
        distanceMeters = 302,
        latitude = 37.5580,
        longitude = 126.9250
    ),
    StoreItem(
        id = 5,
        name = "가챠월드 홍대점",
        category = MapCategory.GACHA,
        rating = 4.2f,
        reviewCount = 74,
        address = "서울특별시 마포구 와우산로 11",
        hours = "12:00 - 22:00",
        tags = listOf("마블", "디즈니"),
        distanceMeters = 410,
        latitude = 37.5550,
        longitude = 126.9215
    )
)

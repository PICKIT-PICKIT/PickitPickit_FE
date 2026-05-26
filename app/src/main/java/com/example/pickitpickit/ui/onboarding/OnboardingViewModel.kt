package com.example.pickitpickit.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OnboardingState(
    val nickname: String = "",
    val selectedProfileId: Int? = null,
    val selectedTags: Set<String> = emptySet()
)

class OnboardingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingState())
    val uiState: StateFlow<OnboardingState> = _uiState.asStateFlow()

    private val adjectives = listOf(
        "졸린", "억울한", "삐진", "당당한", "신난", "야망 있는", "소심한", "뻔뻔한", "아련한", "친절한",
        "까칠한", "덤덤한", "의기소침한", "아장아장", "멍때리는", "늠름한", "쑥스러워하는", "행복한", "비장한", "게으른",
        "씩씩한", "낯가리는", "몽환적인", "짜릿한", "흥겨운", "말랑한", "촉촉한", "폭신한", "바삭한", "쫀득한",
        "노릇노릇한", "덜 익은", "갓 구운", "얼어붙은", "사르르", "흐물흐물한", "탱글한", "미끈한", "꾸덕한", "따끈한",
        "시원한", "끈적한", "부드러운", "거친", "반짝이는", "투명한", "은은한", "흐릿한", "번쩍이는", "노란",
        "푸르스름한", "뽀얀", "검붉은", "보랏빛", "핑크빛", "노을빛", "달빛 어린", "시린", "어두운", "새하얀",
        "알록달록한", "샛노란", "파스텔톤", "어스름한", "무지개색", "형광색", "칙칙한", "황금빛", "에메랄드빛", "잿빛",
        "선명한", "은빛", "칠흑 같은", "구르는", "춤추는", "눈물 흘리는", "퇴근길", "자퇴한", "숨겨진", "날아다니는",
        "거꾸로 서 있는", "길 잃은", "불타는", "물오른", "생각 많은", "벼락치기 중인", "가성비 좋은", "눈치 보는", "달리는", "멈춘",
        "숨바꼭질하는", "밤새 일한", "방금 깨어난", "여행 중인", "뚝딱거리는", "방구석", "대책 없는", "혼자 노는"
    )

    private val nouns = listOf(
        "쿼카", "카피바라", "수달", "햄찌", "다람쥐", "펭귄", "아기새", "판다", "고양이", "시바견",
        "너구리", "해파리", "거북이", "반딧불이", "도마뱀", "알파카", "고래", "병아리", "나무늘보", "물개",
        "우파루파", "미어캣", "토끼", "사막여우", "뱁새", "돌고래", "물고기", "사자", "아기곰", "고슴도치",
        "까눌레", "수플레", "휘낭시에", "샤베트", "타르트", "마카롱", "라떼", "감자", "브로콜리", "붕어빵",
        "탕후루", "단무지", "떡볶이", "만두", "마들렌", "젤리", "식빵", "고구마", "아보카도", "푸딩",
        "에이드", "와플", "옥수수", "완두콩", "모찌", "호떡", "츄러스", "파르페", "초콜릿", "도넛",
        "은하수", "성운", "서리", "파도", "초승달", "바람", "메아리", "안개", "아지랑이", "오로라",
        "혜성", "소나기", "뭉게구름", "들꽃", "단풍잎", "민들레씨", "밤하늘", "이슬비", "조약돌", "은빛모래",
        "먼지", "선인장", "뚝배기", "양말", "슬리퍼", "베개", "비눗방울", "텀블러", "연필심", "키보드",
        "종이비행기", "돗자리", "손난로", "헤드폰", "스탠드", "나침반", "유리병", "오르골", "돋보기", "장화"
    )

    fun updateNickname(nickname: String) {
        if (nickname.length <= 10) {
            _uiState.update { it.copy(nickname = nickname) }
        }
    }

    fun generateRandomNickname() {
        val randomNickname = "${adjectives.random()} ${nouns.random()}"
        _uiState.update { it.copy(nickname = randomNickname) }
    }

    fun selectProfileImage(imageId: Int) {
        _uiState.update { it.copy(selectedProfileId = imageId) }
    }

    fun toggleTag(tag: String) {
        _uiState.update { currentState ->
            val tags = currentState.selectedTags.toMutableSet()
            if (tags.contains(tag)) {
                tags.remove(tag)
            } else {
                tags.add(tag)
            }
            currentState.copy(selectedTags = tags)
        }
    }
}

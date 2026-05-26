package com.example.pickitpickit.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pickitpickit.core.model.DefaultProfileImageResponse
import com.example.pickitpickit.core.model.InterestTagResponse
import com.example.pickitpickit.core.network.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 화면의 전반적인 상태를 정의하는 데이터 클래스
 */
data class OnboardingState(
    val nickname: String = "",
    val selectedProfileUrl: String? = null,
    val selectedProfileType: String = "DEFAULT", // KAKAO, DEFAULT
    val selectedTagIds: Set<Long> = emptySet(),
    
    // API 조회 데이터
    val kakaoProfileUrl: String? = null,
    val defaultProfileImages: List<DefaultProfileImageResponse> = emptyList(),
    val availableTags: List<InterestTagResponse> = emptyList(),
    
    // UI 공통 관리 상태
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
    
    // 복원할 초기 페이지 (0: 닉네임, 1: 프로필 이미지, 2: 관심 태그)
    val initialPage: Int = 0
)

class OnboardingViewModel : ViewModel() {

    private val repository = OnboardingRepository()

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

    init {
        loadOnboardingMeta()
    }

    /**
     * 프로필 이미지 목록, 관심 태그 목록, 사용자 현재 온보딩 정보 조회 및 복원
     */
    private fun loadOnboardingMeta() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val imagesOption = repository.getProfileImages()
            val tags = repository.getInterestTags()
            val status = repository.getOnboardingStatus() // 현재까지 설정된 사용자 정보 조회
            
            if (imagesOption != null) {
                _uiState.update { currentState ->
                    // 1. 기존에 저장되어 있는 값들 복원
                    val savedNickname = status?.nickname ?: ""
                    val savedProfileUrl = status?.profileImageUrl ?: (imagesOption.defaultImages.firstOrNull()?.imageUrl)
                    val savedProfileType = status?.profileImageType ?: "DEFAULT"
                    val savedTagIds = status?.selectedTags?.map { it.id }?.toSet() ?: emptySet()
                    
                    // 2. 사용자의 진척도(닉네임, 프로필 이미지 유무)에 따라 복원할 첫 페이지(초기 Index) 결정
                    val targetPage = when {
                        savedNickname.isNotEmpty() && !status?.profileImageUrl.isNullOrEmpty() -> 2 // 3단계 (관심 태그)
                        savedNickname.isNotEmpty() -> 1 // 2단계 (프로필 이미지)
                        else -> 0 // 1단계 (닉네임)
                    }
                    
                    currentState.copy(
                        nickname = savedNickname,
                        selectedProfileUrl = savedProfileUrl,
                        selectedProfileType = savedProfileType,
                        selectedTagIds = savedTagIds,
                        kakaoProfileUrl = imagesOption.kakaoProfileImageUrl,
                        defaultProfileImages = imagesOption.defaultImages,
                        availableTags = tags,
                        initialPage = targetPage,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "온보딩 설정 데이터를 불러오는 데 실패했습니다."
                    )
                }
            }
        }
    }

    /**
     * 닉네임 입력 필드 갱신
     */
    fun updateNickname(nickname: String) {
        if (nickname.length <= 10) {
            _uiState.update { it.copy(nickname = nickname) }
        }
    }

    /**
     * 임의 닉네임 생성
     */
    fun generateRandomNickname() {
        val randomNickname = "${adjectives.random()} ${nouns.random()}"
        _uiState.update { it.copy(nickname = randomNickname) }
    }

    /**
     * 닉네임 유효성 검사 및 서버 저장
     */
    fun saveNickname(onSuccess: () -> Unit) {
        val currentNickname = _uiState.value.nickname.trim()
        if (currentNickname.length < 2) {
            _uiState.update { it.copy(errorMessage = "닉네임은 2자 이상 입력해주세요.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isSuccess = repository.updateNickname(currentNickname)
            _uiState.update { it.copy(isLoading = false) }
            
            if (isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = "닉네임 저장에 실패했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    /**
     * 프로필 이미지 선택 갱신
     */
    fun selectProfileImage(type: String, url: String) {
        _uiState.update { 
            it.copy(
                selectedProfileUrl = url,
                selectedProfileType = type
            )
        }
    }

    /**
     * 선택된 프로필 이미지 서버 저장
     */
    fun saveProfileImage(onSuccess: () -> Unit) {
        val url = _uiState.value.selectedProfileUrl
        val type = _uiState.value.selectedProfileType
        
        if (url.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "프로필 이미지를 선택해 주세요.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isSuccess = repository.updateProfileImage(type, url)
            _uiState.update { it.copy(isLoading = false) }
            
            if (isSuccess) {
                onSuccess()
            } else {
                _uiState.update { it.copy(errorMessage = "프로필 이미지 저장에 실패했습니다. 다시 시도해 주세요.") }
            }
        }
    }

    /**
     * 관심사 태그 토글 처리
     */
    fun toggleTag(tagId: Long) {
        _uiState.update { currentState ->
            val tags = currentState.selectedTagIds.toMutableSet()
            if (tags.contains(tagId)) {
                tags.remove(tagId)
            } else {
                tags.add(tagId)
            }
            currentState.copy(selectedTagIds = tags)
        }
    }

    /**
     * 관심사 태그 저장 및 온보딩 최종 승인 처리
     */
    fun saveInterestTagsAndComplete(onSuccess: () -> Unit) {
        val tagIds = _uiState.value.selectedTagIds.toList()
        if (tagIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "최소 1개 이상의 태그를 선택해주세요.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 1. 태그 목록 저장 PATCH
            val isTagSaved = repository.updateInterestTags(tagIds)
            if (!isTagSaved) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "관심 태그 저장에 실패했습니다."
                    )
                }
                return@launch
            }
            
            // 2. 온보딩 완료 처리 POST
            val completeResponse = repository.completeOnboarding()
            _uiState.update { it.copy(isLoading = false) }
            
            if (completeResponse != null && completeResponse.onboardingCompleted) {
                _uiState.update { it.copy(isCompleted = true) }
                onSuccess()
            } else {
                _uiState.update { 
                    it.copy(
                        errorMessage = "온보딩 최종 처리 도중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

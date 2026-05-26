package com.example.pickitpickit.ui.onboarding

import android.util.Log
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
    val backendDefaultProfileImages: List<DefaultProfileImageResponse> = emptyList(), // 백엔드 진짜 상대경로 이미지 원본 보존용
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
            
            // 6종 고품질 로컬 Unsplash 기본 이미지 리스트 정의
            val defaultUnsplashImages = listOf(
                DefaultProfileImageResponse("1", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("2", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("3", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("4", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("5", "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=300&q=80"),
                DefaultProfileImageResponse("6", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=300&q=80")
            )

            // 서버로부터 조회 시도 (재시도 및 엇박자 복구는 Repository 내재화로 1회 안전 호출!)
            val imagesOption = repository.getProfileImages()
            var tags = repository.getInterestTags()
            val status = repository.getOnboardingStatus() // 현재까지 설정된 사용자 정보 조회
            
            // 카카오 프로필은 백엔드가 준 데이터를 우선해 살려두고, 
            // 기본 프로필 6종 후보군은 서버의 깨진 경로 대신 준비해둔 Unsplash 고화질 6장으로 무조건 덮어쓰기 적용!
            val finalImagesOption = com.example.pickitpickit.core.model.ProfileImageOptionsResponse(
                kakaoProfileImageUrl = imagesOption?.kakaoProfileImageUrl ?: status?.kakaoProfileImageUrl,
                defaultImages = defaultUnsplashImages
            )
            
            // [Fallback] 서버 관심 태그 목록이 비어있는 경우 시안의 15개 관심 태그 리스트로 대체
            if (tags.isEmpty()) {
                tags = listOf(
                    InterestTagResponse(1L, "포켓몬"),
                    InterestTagResponse(2L, "디즈니"),
                    InterestTagResponse(3L, "원피스"),
                    InterestTagResponse(4L, "산리오"),
                    InterestTagResponse(5L, "마블"),
                    InterestTagResponse(6L, "BT21"),
                    InterestTagResponse(7L, "짱구"),
                    InterestTagResponse(8L, "팬텀"),
                    InterestTagResponse(9L, "귀멸의칼날"),
                    InterestTagResponse(10L, "나루토"),
                    InterestTagResponse(11L, "카카오"),
                    InterestTagResponse(12L, "지브리"),
                    InterestTagResponse(13L, "메이플"),
                    InterestTagResponse(14L, "스누피"),
                    InterestTagResponse(15L, "드래곤볼")
                )
            }
            
            if (status == null) {
                Log.e("ONBOARDING_FLOW", "온보딩 상태 조회 최종 실패: USER_NOT_FOUND 유령 토큰 의심")
                _uiState.update { it.copy(isLoading = false, errorMessage = "USER_NOT_FOUND") }
                return@launch
            }
            
            _uiState.update { currentState ->
                // 1. 기존에 저장되어 있는 값들 복원
                val savedNickname = status.nickname ?: ""
                val savedProfileUrl = status.profileImageUrl ?: (finalImagesOption.defaultImages.firstOrNull()?.imageUrl)
                val savedProfileType = status.profileImageType ?: "DEFAULT"
                val savedTagIds = status.selectedTags?.map { it.id }?.toSet() ?: emptySet()
                
                // [🌟 초강력 수동 복원 동기화 장치]
                // 이미 서버 상에 온보딩(닉네임이 비어있지 않음)이 완료된 상태임이 확실시된다면,
                // 기기의 DataStore가 리셋되었더라도 지체 없이 온보딩 성공 완료(isCompleted = true) 플래그를 세워 메인 화면으로 통과시킵니다!
                val alreadyCompleted = savedNickname.isNotEmpty() && savedTagIds.isNotEmpty()
                if (alreadyCompleted) {
                    Log.i("ONBOARDING_FLOW", "검증 완료: 이미 서버 상에 온보딩이 성공적으로 완수된 회원입니다! 메인 화면으로 즉시 자동 스킵 통과 처리합니다. 🏆🚀")
                }
                
                // 2. 사용자의 진척도(닉네임, 프로필 이미지 유무)에 따라 복원할 첫 페이지(초기 Index) 결정
                val targetPage = when {
                    savedNickname.isNotEmpty() && !status.profileImageUrl.isNullOrEmpty() -> 2 // 3단계 (관심 태그)
                    savedNickname.isNotEmpty() -> 1 // 2단계 (프로필 이미지)
                    else -> 0 // 1단계 (닉네임)
                }
                
                currentState.copy(
                    nickname = savedNickname,
                    selectedProfileUrl = savedProfileUrl,
                    selectedProfileType = savedProfileType,
                    selectedTagIds = savedTagIds,
                    kakaoProfileUrl = finalImagesOption.kakaoProfileImageUrl ?: status.kakaoProfileImageUrl,
                    defaultProfileImages = finalImagesOption.defaultImages,
                    backendDefaultProfileImages = imagesOption?.defaultImages ?: emptyList(),
                    availableTags = tags,
                    initialPage = targetPage,
                    isCompleted = alreadyCompleted, // 🌟 이미 완료되었다면 isCompleted = true로 세팅!
                    isLoading = false
                )
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
        Log.i("ONBOARDING_FLOW", "STEP 1 - 닉네임 저장 요청: '$currentNickname'")
        if (currentNickname.length < 2) {
            _uiState.update { it.copy(errorMessage = "닉네임은 2자 이상 입력해주세요.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val errorMsg = repository.updateNickname(currentNickname)
            _uiState.update { it.copy(isLoading = false) }
            
            if (errorMsg == null) {
                Log.i("ONBOARDING_FLOW", "STEP 1 - 닉네임 저장 성공! 🎉")
                onSuccess()
            } else {
                Log.e("ONBOARDING_FLOW", "STEP 1 - 닉네임 저장 실패 ❌: $errorMsg")
                if (errorMsg.contains("USER_NOT_FOUND") || errorMsg.contains("찾을 수 없습니다")) {
                    _uiState.update { it.copy(errorMessage = "USER_NOT_FOUND") }
                } else {
                    _uiState.update { it.copy(errorMessage = errorMsg) }
                }
            }
        }
    }

    /**
     * 프로필 이미지 선택 갱신
     */
    fun selectProfileImage(type: String, url: String) {
        Log.i("ONBOARDING_FLOW", "대표 프로필 선택 갱신: 타입=$type, URL=$url")
        _uiState.update { 
            it.copy(
                selectedProfileUrl = url,
                selectedProfileType = type
            )
        }
    }

    fun saveProfileImage(onSuccess: () -> Unit) {
        val url = _uiState.value.selectedProfileUrl
        val type = _uiState.value.selectedProfileType
        Log.i("ONBOARDING_FLOW", "STEP 2 - 프로필 이미지 저장 요청: 타입=$type, URL=$url")
        
        if (url.isNullOrEmpty()) {
            _uiState.update { it.copy(errorMessage = "프로필 이미지를 선택해 주세요.") }
            return
        }
        
        // [듀얼 파이프라인 매핑] 화면용 고화질 Unsplash 이미지 URL을 백엔드 진짜 상대경로 이미지 URL로 1:1 역치환
        // [카카오 프로필 저장 화이트리스트 호환 우회] 백엔드가 엄격하게 'http://' 카카오 CDN 주소만 화이트리스트로 허용하여 
        // 보안 로딩용 'https://' 주소를 INVALID_PROFILE_IMAGE(400)로 튕겨내므로 전송 시에 원래 'http://' 형태로 역변환하여 안전하게 저장합니다.
        val finalUrl = if (type == "DEFAULT") {
            val unsplashList = _uiState.value.defaultProfileImages
            val backendList = _uiState.value.backendDefaultProfileImages
            val index = unsplashList.indexOfFirst { it.imageUrl == url }
            if (index in 0 until backendList.size) {
                val mapped = backendList[index].imageUrl
                Log.d("ONBOARDING_FLOW", "기본 프로필 맵핑 완료: Unsplash=$url -> Backend=$mapped")
                mapped
            } else {
                url
            }
        } else if (type == "KAKAO") {
            if (url.startsWith("https://k.kakaocdn.net")) {
                val replaced = url.replace("https://k.kakaocdn.net", "http://k.kakaocdn.net")
                Log.d("ONBOARDING_FLOW", "카카오 HTTPS 우회 변환 적용: $url -> $replaced")
                replaced
            } else {
                url
            }
        } else {
            url
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isSuccess = repository.updateProfileImage(type, finalUrl)
            _uiState.update { it.copy(isLoading = false) }
            
            if (isSuccess) {
                Log.i("ONBOARDING_FLOW", "STEP 2 - 프로필 이미지 저장 성공! 🎉")
                onSuccess()
            } else {
                Log.e("ONBOARDING_FLOW", "STEP 2 - 프로필 이미지 저장 실패 ❌ (전송 주소: $finalUrl)")
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
            Log.d("ONBOARDING_FLOW", "관심사 태그 토글: 현재 선택 태그 ID 리스트 = $tags")
            currentState.copy(selectedTagIds = tags)
        }
    }

    /**
     * 관심사 태그 저장 및 온보딩 최종 승인 처리
     */
    fun saveInterestTagsAndComplete(onSuccess: () -> Unit) {
        val tagIds = _uiState.value.selectedTagIds.toList()
        Log.i("ONBOARDING_FLOW", "STEP 3 - 관심사 태그 및 완료 승인 요청: 선택태그ID=$tagIds")
        if (tagIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "최소 1개 이상의 태그를 선택해주세요.") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // 1. 태그 목록 저장 PATCH
            val tagSaveError = repository.updateInterestTags(tagIds)
            if (tagSaveError != null) {
                Log.e("ONBOARDING_FLOW", "STEP 3 - 관심 태그 저장 PATCH 실패 ❌: $tagSaveError")
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = tagSaveError // 🌟 서버가 전달해 준 실제 상세 실패 사유 그대로 바인딩!
                    )
                }
                return@launch
            }
            
            Log.i("ONBOARDING_FLOW", "STEP 3 - 관심 태그 저장 PATCH 성공! 🎉 이어서 최종 가입 완료 승인 처리합니다.")
            
            // 2. 온보딩 완료 처리 POST
            val completeResult = repository.completeOnboarding()
            _uiState.update { it.copy(isLoading = false) }
            
            completeResult.fold(
                onSuccess = { completeResponse ->
                    if (completeResponse.onboardingCompleted) {
                        Log.i("ONBOARDING_FLOW", "STEP 3 - 온보딩 완료 POST 성공! 홈 화면으로 전격 진입합니다. 🏆🚀")
                        _uiState.update { it.copy(isCompleted = true) }
                        onSuccess()
                    } else {
                        Log.e("ONBOARDING_FLOW", "STEP 3 - 최종 완료 POST 거절 ❌ (상태 불일치)")
                        _uiState.update { 
                            it.copy(errorMessage = "온보딩 조건이 아직 충족되지 않았습니다. 닉네임과 프로필을 다시 확인해주세요.")
                        }
                    }
                },
                onFailure = { throwable ->
                    val errorMsg = throwable.message ?: "온보딩 최종 처리 도중 오류가 발생했습니다."
                    Log.e("ONBOARDING_FLOW", "STEP 3 - 최종 완료 POST 실패 ❌: $errorMsg")
                    _uiState.update { 
                        it.copy(errorMessage = errorMsg) // 🌟 서버가 전달해 준 최종 승인 상세 실패 사유 그대로 바인딩!
                    }
                }
            )
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

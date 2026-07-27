# PickitPickit_FE
피킷피킷(PickitPickit) 안드로이드 프론트엔드 레포지토리입니다.

### 📌 협업 규칙
모든 기능 개발은 다음 흐름을 따릅니다:
1. 개발하고자 하는 기능에 대한 이슈를 등록하여 번호를 발급합니다.
2. `dev` 브랜치로부터 분기하여 이슈 번호를 사용해 이름을 붙인 `feat` 브랜치를 만든 후 작업합니다.
3. 작업이 완료되면 `dev` 브랜치에 풀 요청(Pull Request)을 작성하고, 팀원의 동의를 얻으면 병합합니다.

---

1. 초기 세팅
* 원하는 로컬 경로에 클론
    ```bash
    $ git clone https://github.com/PICKIT-PICKIT/PickitPickit_FE.git
    ```

2. Git Flow 브랜치 전략
* [`main(배포)` - `dev(통합)` - `feat(개발)·fix(수정)`] 세 단계
* 개발 완료 시 `PR`로 `dev`에 병합 → 주기적으로 `release/x.y`로 묶어 `QA` 후 `main`에 병합
* 브랜치 네이밍:
  `feat/#<이슈 번호>-<간단한 설명>` (예시: `feat/#1-kakao-login`)

3. 기능 개발
    ```bash
    # 1️⃣ 최신 통합 브랜치 받기
    $ git checkout dev
    $ git pull origin dev
    
    # 2️⃣ 기능 브랜치 생성
    $ git checkout -b feat/#<이슈 번호>-<간단 설명>
    
    # 3️⃣ 작업 & 단위 커밋
    $ git commit -m "feat: kakao oauth login (#<이슈 번호>)"
   ```
* 커밋 규칙: `type: subject`
    * `feat` | `fix` | `docs` | `refactor` | `test` | `chore` | `rename`

4. PR 작성 및 리뷰 규칙
* PR을 생성해서 양식에 맞게 작성
    * 예시: `feat: 카카오 로그인 연동 및 온보딩 화면 구현`
* 개발 중이라도 일찍 공유해 피드백 주고받기
    * 제안(코드 `suggestion`) → 대안 함께 제시하기
* 리뷰어 최소 `1`명 `Approve` → `Merge commit` 방식으로 병합

5. dev 최신화 & 추가 작업
    ```bash
    $ git checkout dev
    $ git pull origin dev            # dev 최신 상태 반영
    $ git branch -d feat/<name>      # 로컬 브랜치 정리
   ```
* 새 기능 구현 시 항상 최신 `dev` 기준으로 새 브랜치를 파서 충돌 최소화.

---

### 🖥️ 코드 컨벤션
> 운영 언어: Kotlin 
> 빌드 도구: Gradle (Kotlin DSL)
> UI 프레임워크: Jetpack Compose

1. 네이밍
   * 클래스 / 인터페이스 / 오브젝트: `PascalCase`
   * 함수 / 변수: `camelCase`
   * Compose 함수 (화면/컴포넌트): `PascalCase` (예: `MainScreen()`)
   * 상수는 `UPPER_SNAKE_CASE`
   * `Boolean` 변수 및 메서드는 `is/has/can` 패턴 사용 (예: `isOnboardingCompleted()`)
2. 형식
    * 들여쓰기: `4 spaces`
    * 패키지명은 소문자로만 구성하며, 단어 구분을 하지 않음
3. Compose 작성 규칙
    * `Modifier`는 항상 컴포저블 함수의 첫 번째 선택적(Optional) 매개변수로 전달
    * 상태를 가지지 않는(Stateless) 컴포저블을 지향하고, 필요 시 상태 끌어올리기(State Hoisting) 적극 활용
4. 아키텍처 & 상태 관리
    * ViewModel에서 UI 상태는 `StateFlow`를 통해 노출 (`_uiState` / `uiState` 백킹 프로퍼티 패턴 사용)
    * Data Layer(Repository/Network)와 UI Layer 철저히 분리
5. 네트워크 및 모델 (DTO)
    * 백엔드 API 통신은 `Retrofit2` 및 `OkHttp` 활용
    * 데이터 모델은 `data class`로 정의하고, 응답/요청/UI 모델 네이밍 명확히 구분
6. 로컬 데이터 관리
    * 로그인 토큰, 사용자 환경설정 등은 `Preferences DataStore` 활용

---

### 🏛️ 프로젝트 구조
```text
 PickitPickit_FE
  app/src/main/java/com/example/pickitpickit
  ├── core                  // 공통 모듈 및 비즈니스/데이터 로직
  │   ├── datastore         // 로컬 데이터 저장 (Preferences DataStore)
  │   ├── model             // 네트워크 및 도메인 데이터 모델 (DTO)
  │   └── network           // API 통신 (Retrofit Interface, Repository, Interceptor)
  │
  ├── ui                    // 화면(Feature) 단위 UI 패키지
  │   ├── admin             // 점주 전용 매장 관리 화면
  │   ├── home              // 메인 홈 화면
  │   ├── login             // 소셜 로그인 (Kakao)
  │   ├── map               // 지도 기반 매장 탐색 (Kakao Maps 연동)
  │   ├── mypage            // 마이페이지, 프로필
  │   ├── navigation        // 네비게이션 라우팅 및 하단 바 관리
  │   ├── onboarding        // 최초 진입 시 온보딩 프로세스
  │   ├── store             // 매장 상세 정보 및 리뷰
  │   └── theme             // 디자인 시스템 (Color, Typography 등)
  │
  ├── GlobalApplication.kt  // 전역 애플리케이션 클래스 (카카오 SDK 초기화 등)
  └── MainActivity.kt       // 앱의 진입점 및 Root NavGraph 설정
```
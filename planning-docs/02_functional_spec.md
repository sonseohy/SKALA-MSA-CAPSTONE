# 기능 명세서

## 1. 기능 개발 원칙

- 현재 백엔드 MSA 구조를 유지한다.
- 새로운 서비스 추가는 마지막 선택지로 둔다.
- 기존 테이블은 가능하면 유지하고, 화면과 DTO 문구를 HRD 도메인으로 재해석한다.
- 결제는 실제 PG 연동을 만들지 않고 현재 `payment-service`의 자동 성공 결제를 사용한다.
- AI 추천은 현재 recommend-service 기반 추천을 사용한다.
- 만족도 조사만 MVP 확장 기능으로 다룬다. 교육 결과 분석은 발표/기획서에서 향후 개발 예정으로 정리한다.

## 2. 사용자 유형

| 사용자 | 기존 Role | HRD 의미 | 주요 기능 |
|---|---|---|---|
| HRD 담당자 | STUDENT | 기업 교육을 탐색/계약하는 담당자 | 교육 니즈 입력, 추천 확인, 계약 신청, 내 교육 확인 |
| 교육 공급자 | INSTRUCTOR | 강사 또는 교육기관 | 교육 프로그램 등록, 공급자 프로필성 정보 노출 |
| 기업 임직원 | STUDENT | 교육 참여자 | 개설 교육 참여, 만족도 조사 |

현재 백엔드 Role은 `STUDENT`, `INSTRUCTOR`만 있으므로 DB enum을 대규모로 바꾸지 않는다. 프론트 화면에서는 `STUDENT`를 HRD 담당자/임직원 맥락으로 표현한다.

## 3. 기능 목록

### F-01. 회원가입/로그인

목적:

- HRD 담당자와 교육 공급자가 플랫폼에 접근한다.

현재 활용:

- `POST /api/users/register`
- OAuth2 로그인/토큰 발급 흐름
- `GET /api/users/me`

최소 수정 방향:

- 회원가입 화면 문구를 HRD 도메인으로 변경한다.
- Role 선택 문구를 `기업 HRD 담당자`, `교육 공급자`로 보여주되 내부 값은 기존 `STUDENT`, `INSTRUCTOR`를 사용한다.

완료 기준:

- 회원가입 후 로그인할 수 있다.
- 로그인 후 사용자 이름/역할이 화면에 표시된다.

### F-02. HRD 대시보드/랜딩

목적:

- 기존 B2C 강의 판매 서비스 느낌을 B2B HRD 플랫폼으로 전환한다.

현재 활용:

- Vue `LandingView.vue`, `CourseListView.vue`, `MyPageView.vue`

최소 수정 방향:

- 랜딩 카피를 기업 HRD 가치 중심으로 변경한다.
- 첫 화면에서 교육 니즈 입력/추천/교육 프로그램 탐색으로 이어지게 만든다.
- 백엔드 변경 없이 프론트에서 카테고리/문구를 HRD 용어로 매핑한다.

완료 기준:

- 사용자가 서비스 목적을 HRD 플랫폼으로 이해할 수 있다.
- 로그인 후 교육 프로그램 탐색으로 이동할 수 있다.

### F-03. 기업 교육 니즈 입력

목적:

- HRD 담당자가 기업 사업 방향 또는 교육 요구사항을 입력한다.

현재 활용:

- 신규 백엔드 저장은 필수 아님.
- MVP에서는 프론트 상태 또는 recommend-service 요청 전 필터 UI로 처리한다.

최소 수정 방향:

- Sprint 1에서는 프론트 입력 폼을 만들고, 입력 키워드에 따라 추천 카테고리 안내를 표시한다.
- 백엔드 저장이 꼭 필요하면 기존 `course.category` 또는 recommend-service 로직만 가볍게 활용한다.

예시 필드:

| 필드 | 설명 | MVP 처리 |
|---|---|---|
| companyGoal | 사업 방향/교육 목표 | 프론트 상태 |
| targetRole | 교육 대상 | 프론트 표시 |
| preferredCategory | 관심 교육 분야 | 기존 Course Category 매핑 |
| budget | 예산 | 화면 표시 또는 course price 비교 |
| deliveryType | 온라인/오프라인 | 향후 필드 |
| region | 교육 지역 | 향후 필드 |

완료 기준:

- HRD 담당자가 니즈를 입력할 수 있다.
- 입력 결과가 추천/탐색 화면의 맥락으로 사용된다.

### F-04. 교육 추천

목적:

- HRD 담당자 또는 사용자의 관심/이력 기반으로 추천 교육을 보여준다.

현재 활용:

- `recommend-service`
- `GET /api/recommend/{userId}`
- 내부적으로 enrollment history와 course category를 이용한다.

최소 수정 방향:

- 현재 추천 로직을 그대로 사용한다.
- 화면에서는 “AI 추천”이라고 과장하기보다 “AI 추천 후보(규칙 기반 MVP)” 또는 “사업 니즈 기반 추천”으로 표현한다.
- 발표에서는 향후 LLM/문서 분석 확장 예정이라고 설명한다.

완료 기준:

- 추천 API 결과가 프론트에 표시된다.
- 추천 근거 메시지를 화면에 노출한다.

### F-05. 교육 프로그램 목록/상세

목적:

- HRD 담당자가 교육 프로그램과 교육 공급자를 비교한다.

현재 활용:

- `GET /api/courses`
- `GET /api/courses/{id}`
- `GET /api/courses/category/{category}`

최소 수정 방향:

- `Course`를 기업 교육 프로그램으로 표현한다.
- `instructorId`를 교육 공급자 ID로 표현한다.
- `price`를 예상 교육비/계약 비용으로 표현한다.
- `enrollmentCount`를 신청/참여 수 또는 관심도 지표로 표현한다.

완료 기준:

- 교육 프로그램 리스트가 표시된다.
- 상세 화면에서 교육 목표, 커리큘럼, 교육 공급자 정보를 확인할 수 있다.

### F-06. 교육 공급자 프로그램 등록

목적:

- 교육 공급자가 제공 가능한 기업 교육 프로그램을 등록한다.

현재 활용:

- `POST /api/courses`
- `INSTRUCTOR` 권한

최소 수정 방향:

- 등록 화면 문구를 “강의 등록”에서 “기업 교육 프로그램 등록”으로 변경한다.
- 기존 필드만 우선 사용한다.
- `description`에 커리큘럼, 경력, 교육 방식, 지역 등을 구조화된 텍스트로 입력하게 한다.

완료 기준:

- 교육 공급자 계정으로 교육 프로그램을 등록할 수 있다.
- 등록된 프로그램이 목록/상세에 표시된다.

### F-07. 교육 계약/참여 신청

목적:

- HRD 담당자가 교육 프로그램을 선택해 계약 또는 참여 신청을 진행한다.

현재 활용:

- `POST /api/enrollments`
- 내부적으로 payment-service 호출
- payment 완료 이벤트 후 enrollment 상태 활성화

최소 수정 방향:

- `Enrollment`를 계약 신청 또는 교육 참여 신청으로 재해석한다.
- 실제 계약서/전자서명은 구현하지 않는다.
- 신청 버튼 문구를 “수강 신청”에서 “교육 계약 신청” 또는 “교육 참여 신청”으로 변경한다.

완료 기준:

- 상세 화면에서 신청할 수 있다.
- 신청 후 내 교육 목록에 나타난다.
- payment-service의 현재 자동 결제 흐름을 통해 상태 변경이 일어난다.

### F-08. 내 교육/계약 목록

목적:

- HRD 담당자 또는 임직원이 신청한 교육을 확인한다.

현재 활용:

- `GET /api/enrollments/my`

최소 수정 방향:

- 목록 화면 제목을 “내 수강 목록”에서 “내 교육/계약 목록”으로 변경한다.
- 상태값 `PENDING`, `ACTIVE`, `CANCELLED`를 HRD 문구로 매핑한다.

상태 문구 예시:

| 기존 상태 | 화면 문구 |
|---|---|
| PENDING | 계약/참여 신청 중 |
| ACTIVE | 교육 확정 |
| CANCELLED | 취소됨 |

완료 기준:

- 신청한 교육 목록과 상태가 표시된다.

### F-09. 만족도 조사

목적:

- 교육 종료 후 임직원이 만족도 조사를 제출한다.

현재 활용:

- 현재 백엔드에는 Review/Survey 기능이 없다.

최소 수정 방향:

- Sprint 2 후보로 둔다.
- 가장 작은 구현은 `survey-service` 신설보다 기존 서비스 중 하나에 단순 테이블/API를 추가하는 방식이다.
- 추천안은 `enrollment-service`에 `surveys` 테이블과 API를 추가하는 것이다. 교육 참여와 직접 연결되기 때문이다.

권장 최소 테이블:

```sql
surveys (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  enrollment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  education_score INT NOT NULL,
  instructor_score INT NOT NULL,
  usefulness_score INT NOT NULL,
  difficulty_score INT NOT NULL,
  comment TEXT,
  created_at DATETIME(6)
)
```

권장 API:

- `POST /api/enrollments/{enrollmentId}/survey`
- `GET /api/enrollments/{enrollmentId}/survey`
- `GET /api/enrollments/courses/{courseId}/surveys/summary`

완료 기준:

- 교육별 만족도 제출이 가능하다.
- HRD 담당자가 평균 만족도를 볼 수 있다.

## 4. 이번 MVP에서 하지 않는 것

- 실제 PG 결제 연동
- 별도 AI/ML 추천 모델 학습
- LLM 기반 문서 업로드/분석
- 복잡한 계약서/전자서명
- 공급자 매칭 점수 고도화
- 교육 결과 분석 대시보드 전체 구현

위 항목은 발표 기획서에서 “향후 개발 예정”으로 정리한다.

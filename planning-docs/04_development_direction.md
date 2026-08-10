# 개발 방향

## 1. 전체 방향

이번 개발은 “백엔드 구조 유지, 프론트 경험 전환”을 기본 전략으로 한다.

백엔드는 이미 다음 MSA로 나뉘어 있다.

```text
auth-server
api-gateway
eureka-server
user-service
course-service
enrollment-service
payment-service
recommend-service
MariaDB
Kafka
```

이 구조를 재설계하지 않는다. 각 서비스의 책임을 HRD 플랫폼 의미로 재해석하고, 필요한 경우에만 작은 필드나 API를 추가한다.

## 2. 현재 MSA 재해석

| 서비스 | 현재 책임 | HRD 플랫폼 책임 | 수정 수준 |
|---|---|---|---|
| auth-server | OAuth2 인증 | 로그인/토큰 발급 | 수정 없음 |
| api-gateway | API 라우팅/인증 헤더 전달 | 프론트 단일 진입점 | 수정 없음 |
| eureka-server | 서비스 디스커버리 | 서비스 등록/탐색 | 수정 없음 |
| user-service | 사용자 등록/조회 | HRD 담당자/임직원/공급자 계정 | 문구 중심 |
| course-service | 강의 등록/조회 | 기업 교육 프로그램/공급자 제공 교육 | 문구 + 필요 시 필드 |
| enrollment-service | 수강신청/상태 | 교육 계약/참여 신청 | 문구 + 만족도 API 후보 |
| payment-service | 결제 처리 | 계약 비용 처리 | 현재 자동 결제 유지 |
| recommend-service | 수강 이력 기반 추천 | HRD 교육 추천 MVP | 현재 로직 활용 |

## 3. 백엔드 최소 수정 전략

### 그대로 유지

- Docker Compose 구조
- Eureka/Gateway/Auth
- MariaDB/Kafka
- `users`, `courses`, `enrollments`, `payments` 기본 테이블
- 기존 API endpoint 대부분
- payment-service 자동 성공 결제
- recommend-service의 규칙 기반 추천

### 문구/프론트 매핑으로 해결

| 기존 값 | 화면 표시 |
|---|---|
| Course | 기업 교육 프로그램 |
| Instructor | 교육 공급자 |
| Student | HRD 담당자 / 임직원 |
| Enrollment | 교육 계약/참여 신청 |
| Payment | 계약 비용 처리 |
| 수강 신청 | 교육 계약 신청 |
| 내 수강 목록 | 내 교육/계약 목록 |

### 필요 시 작은 수정

아래 수정은 선택 사항이다. 시간 부족 시 프론트 문구와 `description` 활용으로 대체한다.

| 영역 | 선택 수정 | 이유 |
|---|---|---|
| Course | `deliveryType`, `region`, `targetAudience`, `durationDays` 필드 추가 | 공급자 비교 정보 강화 |
| User | `companyName`, `providerBio` 필드 추가 | HRD/공급자 프로필 강화 |
| Enrollment | `contractMemo` 필드 추가 | 계약 신청 의도 표시 |
| Enrollment | Survey 관련 Entity/API 추가 | 만족도 조사 구현 |

## 4. 추천 기능 개발 방향

현재 recommend-service는 다음 방식으로 동작한다.

```text
사용자 수강/신청 이력 조회
  -> 최빈 카테고리 계산
  -> 해당 카테고리의 미수강 교육 조회
  -> enrollmentCount 기준 정렬
```

이번 프로젝트에서는 이 구조를 AI 추천 MVP로 활용한다.

발표/기획상 표현:

- “최종 목표는 기업 전략 문서 분석 기반 AI 추천”
- “MVP에서는 키워드/카테고리 기반 규칙 매칭으로 구현”
- “현재 구현된 추천 서비스는 향후 AI 모델 또는 LLM 분석 결과를 입력으로 받을 수 있는 구조”

구현 우선순위:

1. 현재 추천 API를 프론트에서 잘 보여준다.
2. 추천 카드에 추천 근거 메시지를 표시한다.
3. 시간이 남으면 프론트에서 기업 니즈 키워드를 카테고리로 매핑한다.
4. 백엔드 recommend-service 키워드 API 추가는 선택 사항이다.

선택 API 예시:

```http
POST /api/recommend/needs
```

```json
{
  "userId": 1,
  "keywords": ["생성형 AI", "Cloud Native", "MSA"]
}
```

단, 이 API는 필수 구현이 아니다.

## 5. 결제 개발 방향

현재 payment-service는 실습용으로 내부 결제 요청을 받으면 UUID transactionId를 만들고 성공 처리한다.

이번 프로젝트에서는 이를 그대로 사용한다.

기획상 의미:

- 실제 PG 결제가 아니라 기업 교육 계약 비용 처리의 MVP 시뮬레이션
- HRD 담당자가 교육 계약을 신청하면 계약 비용이 처리되고 교육 상태가 확정되는 흐름

하지 않는 것:

- 카드 결제 UI
- PG사 API 연동
- 결제 취소/환불 고도화
- 세금계산서/정산 시스템

## 6. 만족도 조사 개발 방향

`PROJECT_CONTEXT.md`에는 만족도 조사/교육 결과 분석이 함께 나오지만, 이번 구현에서는 만족도 조사 정도만 다룬다.

권장 위치:

- `enrollment-service`

이유:

- 만족도는 교육 참여 결과와 연결된다.
- `Enrollment`가 userId/courseId를 이미 갖고 있다.
- 새로운 MSA를 만들지 않아도 된다.

최소 구현:

- Survey Entity
- Survey Repository
- Survey DTO
- EnrollmentController에 Survey API 추가 또는 SurveyController 추가
- 평균 점수 summary API

프론트:

- 내 교육 목록에서 `ACTIVE` 상태 교육에 “만족도 작성” 버튼 표시
- 1~5점 입력과 한 줄 후기
- 교육 상세 또는 마이페이지에서 평균 만족도 표시

## 7. 프론트 개발 방향

프론트는 기획에 맞게 적극적으로 변경한다.

우선 수정 화면:

| 파일 | 수정 방향 |
|---|---|
| `LandingView.vue` | HRD AX 플랫폼 랜딩으로 변경 |
| `CourseListView.vue` | 교육 프로그램 탐색/추천 목록으로 변경 |
| `CourseDetailView.vue` | 공급자/커리큘럼/계약 CTA 중심 |
| `CourseCreateView.vue` | 교육 공급자 프로그램 등록 |
| `EnrollmentView.vue` | 내 교육/계약 목록 |
| `MyPageView.vue` | HRD 담당자/공급자 프로필, 추천/신청 현황 |
| `LoginView.vue` | HRD/공급자 계정 흐름 |

추가 후보 화면:

| 화면 | 목적 | 우선순위 |
|---|---|---|
| NeedsInputView | 기업 교육 니즈 입력 | 높음 |
| RecommendView | 추천 교육 확인 | 높음 |
| SurveyView | 만족도 조사 | 중간 |
| HrdDashboardView | HRD 요약 대시보드 | 중간 |

## 8. 프론트 용어 사전

| 기존 UI 문구 | 변경 문구 |
|---|---|
| 강의 | 교육 프로그램 |
| 강사 | 교육 공급자 |
| 수강생 | 임직원/참여자 |
| 수강 신청 | 교육 계약 신청 |
| 수강 목록 | 내 교육/계약 |
| 추천 강의 | 추천 교육 |
| 가격 | 예상 교육비 |
| 카테고리 | 교육 분야 |

## 9. 아키텍처 흐름

```text
Vue Frontend
  -> API Gateway
      -> Auth Server
      -> User Service
      -> Course Service
      -> Enrollment Service
            -> Payment Service
            -> Kafka payment.completed
            -> Course Service enrollment-count 증가
      -> Recommend Service
            -> Enrollment Service 이력 조회
            -> Course Service 교육 조회
```

HRD 도메인 흐름:

```text
HRD 담당자
  -> 교육 니즈 입력
  -> 추천 교육 확인
  -> 교육 프로그램 상세/공급자 확인
  -> 계약 신청
  -> 계약 비용 처리
  -> 교육 확정
  -> 만족도 조사
```

## 10. 개발 시 주의사항

- `docker compose build` 또는 `docker compose up -d --build`는 공용 IP 제한 때문에 피한다.
- 배포받은 이미지를 사용하는 경우 `docker compose up -d --no-build --pull never`를 사용한다.
- `recommend-service` 문서는 `http://localhost:8085/docs`다.
- 8084/3379 포트 충돌이 자주 발생할 수 있으므로 기존 Java/MariaDB 프로세스를 확인한다.
- 프론트에서 개별 서비스 포트로 직접 호출하면 CORS 문제가 날 수 있으므로 Gateway 경유를 우선한다.

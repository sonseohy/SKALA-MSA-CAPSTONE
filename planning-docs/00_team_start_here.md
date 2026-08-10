# 팀원용 시작 가이드

이 문서를 먼저 읽고 각자 담당 문서로 넘어간다.

## 1. 결론

우리가 기획한 **AI 기반 기업 HRD 통합 플랫폼 MVP**는 기존 MSA 구조와 백엔드 아키텍처를 최대한 유지한 상태로 만들 수 있다.

단, 목표는 완성형 HRD SaaS가 아니라 다음 흐름을 보여주는 MVP다.

```text
HRD 담당자 로그인
  -> 기업 교육 니즈 입력
  -> 추천 교육 확인
  -> 교육 프로그램 상세 확인
  -> 교육 계약 신청
  -> 현재 payment-service 자동 처리
  -> 내 교육/계약 목록 확인
  -> 교육별 만족도 결과 확인
```

## 2. 백엔드 유지 전략

기존 백엔드 서비스를 새로 갈아엎지 않는다. 기존 도메인을 HRD 관점으로 재해석한다.

| 기존 서비스/도메인 | 우리 MVP 의미 |
|---|---|
| `user-service` | HRD 담당자 / 교육 공급자 / 임직원 계정 |
| `course-service` | 기업 교육 프로그램 / 공급자 제공 교육 |
| `enrollment-service` | 교육 계약 신청 / 교육 참여 신청 |
| `payment-service` | 계약 비용 처리 시뮬레이션 |
| `recommend-service` | 규칙 기반 교육 추천 |
| `auth-server` | 로그인/토큰 발급 |
| `api-gateway` | 프론트 API 단일 진입점 |
| `eureka-server` | 서비스 등록/탐색 |
| `Kafka` | 결제 완료/교육 확정 이벤트 흐름 |

백엔드는 대략 80~90% 유지하는 것을 목표로 한다. 새 MSA를 만들지 않는다.

## 3. 백엔드 수정이 거의 필요 없는 기능

아래 기능은 현재 API를 재사용한다.

- 회원가입/로그인
- 교육 프로그램 등록
- 교육 프로그램 목록/상세 조회
- 교육 계약/참여 신청
- 계약 비용 처리 흐름
- 내 교육/계약 목록 조회
- 추천 교육 조회

프론트에서 문구와 화면 흐름을 바꾸면 HRD 서비스처럼 보여줄 수 있다.

## 4. 백엔드 수정/확장 현황

현재 MVP에서는 새 MSA를 만들지 않고 `course-service` 중심의 작은 필드 확장만 반영했다.

반영 완료:

- `courses.duration_days`
- `courses.start_date`
- `courses.end_date`
- `courses.delivery_type`
- `courses.target_audience`
- `courses.region`
- `courses.difficulty`
- `enrollment-service`, `recommend-service` DTO/스키마 전달 필드 확장

향후 후보:

- 실제 Survey Entity/API
- 임직원 만족도 제출 흐름
- 교육 결과 분석 리포트

현재 프론트에서는 Survey API 없이 HRD 담당자용 교육별 만족도 결과 화면을 시연용 데이터로 제공한다.

## 5. AI 솔루션 표현 기준

기획서에는 AI/AX 방향을 충분히 담는다.

하지만 구현에서는 실제 LLM, 문서 분석, ML 추천 모델을 만들지 않는다.

발표 표현:

```text
최종적으로는 기업 전략 문서와 교육 니즈를 AI가 분석해 교육을 추천하는 플랫폼을 지향한다.
MVP에서는 현재 구현된 추천 서비스와 카테고리/키워드 기반 규칙 매칭으로 핵심 흐름을 검증한다.
```

## 6. 결제 표현 기준

실제 결제 시스템을 새로 만들지 않는다.

현재 `payment-service`는 실습용으로 결제를 자동 성공 처리한다. 우리는 이것을 “기업 교육 계약 비용 처리 시뮬레이션”으로 사용한다.

하지 않는 것:

- 카드 결제 UI
- PG사 연동
- 환불/정산 시스템
- 실제 과금 정책 구현

## 7. 프론트 개발 방향

프론트는 기획에 맞게 적극적으로 바꾼다.

| 기존 문구 | 변경 문구 |
|---|---|
| 강의 | 교육 프로그램 |
| 강사 | 교육 공급자 |
| 수강생 | 임직원/참여자 |
| 수강 신청 | 교육 계약 신청 |
| 내 수강 목록 | 내 교육/계약 목록 |
| 추천 강의 | 추천 교육 |
| 가격 | 예상 교육비 |

핵심은 백엔드를 크게 바꾸는 것이 아니라, 사용자가 보는 화면과 흐름을 HRD 플랫폼으로 바꾸는 것이다.

역할별 화면 구성:

| 역할 | 메뉴/화면 |
|---|---|
| HRD 담당자 (`STUDENT`) | 대시보드, 교육 니즈 입력, 추천 교육, 교육 카탈로그, 교육 공급자, 진행 중인 교육, 계약/신청, 만족도 결과 |
| 교육 공급자 (`INSTRUCTOR`) | 공급자 대시보드, 프로그램 등록, 교육 카탈로그, 공급자 프로필 |

`MyPage`는 MVP에서 제거했다.

## 8. 팀원별로 보면 되는 문서

| 담당 | 먼저 볼 문서 |
|---|---|
| 기획/발표 | `01_project_brief.md`, `06_submission_checklist.md` |
| 백엔드 Course/User | `02_functional_spec.md`, `03_api_spec.md`, `04_development_direction.md` |
| 백엔드 Enrollment/Payment | `03_api_spec.md`, `04_development_direction.md` |
| 추천/만족도 | `02_functional_spec.md`, `03_api_spec.md`, `04_development_direction.md` |
| 프론트 | `02_functional_spec.md`, `03_api_spec.md`, `04_development_direction.md` |
| QA/시연 | `05_sprint_plan_and_roles.md`, `06_submission_checklist.md` |

## 9. 가장 중요한 합의

팀원 모두 아래 원칙을 맞춰야 한다.

- 백엔드는 현재 MSA 구조를 유지한다.
- 새 기능은 최소 수정으로 붙인다.
- 프론트는 HRD 플랫폼처럼 보이도록 적극 변경한다.
- AI와 교육 결과 분석은 기획 방향으로 제시하되 MVP 구현은 자제한다.
- 결제는 현재 실습용 payment-service를 활용한다.
- 만족도는 HRD 담당자용 결과 조회 화면으로 시연하고, 실제 제출 API는 향후 개발 후보로 둔다.

# AI 기반 기업 HRD 통합 플랫폼 문서 묶음

이 폴더는 `PROJECT_CONTEXT.md`와 `Agile_MSA_실습_가이드_수정_v2.pdf`를 기준으로 팀 개발을 시작하기 위한 기획/명세 문서 모음이다.

이번 프로젝트의 핵심 원칙은 다음과 같다.

- 백엔드는 현재 MSA 구조를 최대한 유지한다.
- 신규 서비스를 크게 만들지 않고 기존 도메인을 HRD 관점으로 재해석한다.
- 테이블/필드/문구 수정은 최소화한다.
- 결제는 현재 구현된 실습용 자동 결제 흐름을 사용한다.
- AI 추천은 현재 구현된 추천 서비스와 하드코딩/규칙 기반 매칭을 활용한다.
- 프론트엔드는 B2B HRD 플랫폼 기획에 맞게 적극적으로 화면과 용어를 바꾼다.
- 만족도 조사/교육 결과 분석 중 MVP에서는 HRD 담당자용 만족도 결과 조회 화면까지 구현하고, 실제 Survey API는 향후 개발 후보로 둔다.

## 현재 구현 반영 사항

- `Course`에 교육 운영 필드가 추가됐다: `durationDays`, `startDate`, `endDate`, `deliveryType`, `targetAudience`, `region`, `difficulty`.
- `course-service`, `enrollment-service`, `recommend-service`는 위 Course 확장 필드를 DTO/스키마로 전달할 수 있다.
- 프론트는 HRD 담당자와 교육 공급자 역할에 따라 메뉴와 대시보드 구성을 다르게 보여준다.
- `MyPage`는 MVP 범위에서 제거했다.
- 만족도 화면은 임직원 입력 폼이 아니라 HRD 담당자가 교육별 결과와 세부 의견을 확인하는 화면으로 구성했다.
- 교육 계약/진행 목록의 교육명은 교육 상세 화면으로 이동한다.

## 문서 목록

| 문서 | 목적 |
|---|---|
| [00_team_start_here.md](00_team_start_here.md) | 팀원들이 가장 먼저 읽을 시작 가이드 |
| [01_project_brief.md](01_project_brief.md) | 발표 기획서 초안과 서비스 방향 |
| [02_functional_spec.md](02_functional_spec.md) | 기능 명세서 |
| [03_api_spec.md](03_api_spec.md) | API 명세서와 현재 Swagger URL |
| [04_development_direction.md](04_development_direction.md) | 백엔드/프론트 개발 방향과 최소 수정 전략 |
| [05_sprint_plan_and_roles.md](05_sprint_plan_and_roles.md) | Sprint 구분과 6인 팀 역할 분담 |
| [06_submission_checklist.md](06_submission_checklist.md) | PDF 가이드 기준 제출/발표 체크리스트 |

## 현재 서비스 포트

| 서비스 | 포트 | 문서 URL |
|---|---:|---|
| API Gateway | 8080 | Gateway 경유 API 호출 기준 |
| User Service | 8081 | http://localhost:8081/swagger-ui/index.html |
| Course Service | 8082 | http://localhost:8082/swagger-ui/index.html |
| Enrollment Service | 8083 | http://localhost:8083/swagger-ui/index.html |
| Payment Service | 8084 | http://localhost:8084/swagger-ui/index.html |
| Recommend Service | 8085 | http://localhost:8085/docs |
| Eureka | 8761 | http://localhost:8761 |
| Auth Server | 9000 | http://localhost:9000 |
| Frontend | 3000 | http://localhost:3000 |

프론트엔드는 CORS/인증 문제를 줄이기 위해 원칙적으로 `http://localhost:8080` API Gateway를 통해 호출한다.

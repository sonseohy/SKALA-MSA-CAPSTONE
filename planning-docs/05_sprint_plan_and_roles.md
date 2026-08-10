# Sprint 계획과 6인 역할 분담

## 1. Sprint 구분 기준

PDF 가이드 기준에 따라 “이 기능이 없으면 서비스 가치가 전달되지 않는가?”를 기준으로 Sprint 1과 Sprint 2를 나눈다.

Sprint 1은 HRD 담당자가 교육을 찾고 신청하는 핵심 가치에 집중한다.

Sprint 2는 결제 흐름 표현, 만족도 결과 조회, 향후 Survey API 설계처럼 있으면 서비스 완성도가 올라가는 기능에 집중한다.

## 2. Sprint 1 목표

목표:

```text
기업 HRD 담당자가 교육 니즈를 기반으로 교육 프로그램을 탐색/추천받고 계약 신청까지 할 수 있다.
```

필수 기능:

- HRD 플랫폼 랜딩
- 회원가입/로그인
- HRD/공급자 Role 문구 전환
- 교육 니즈 입력 UI
- 교육 프로그램 목록/상세
- 교육 공급자 프로그램 등록
- 교육 일정/운영 조건 입력
- 추천 교육 조회
- 교육 계약/참여 신청
- 내 교육/계약 목록
- 역할별 메뉴/대시보드 분리

백엔드:

- 기존 API 우선 사용
- 신규 API는 되도록 만들지 않음
- recommend-service는 현재 추천 로직 사용

프론트:

- B2B HRD 도메인으로 화면 전환
- API 연동 완성
- 발표용 동작 화면 캡처 가능 상태 만들기

## 3. Sprint 2 목표

목표:

```text
교육 계약 이후의 상태 흐름과 만족도 조사까지 보여준다.
```

확장 기능:

- 현재 payment-service를 활용한 계약 비용 처리 흐름 표시
- 내 교육 상태 표시 개선
- HRD 담당자용 교육별 만족도 결과 화면
- 교육별 평균 만족도 요약
- 실제 만족도 제출 API 설계

하지 않는 것:

- 실제 결제 시스템 구현
- 실제 AI 모델/LLM 분석 구현
- 정교한 교육 결과 분석

## 4. 6인 역할 분담안

### 1번: PM/기획/발표 문서 담당

주요 책임:

- Pain Point 정리
- 이해관계자 정의
- 도메인 매핑표 관리
- Sprint 1/2 범위 조정
- 발표 자료 전체 흐름 정리

산출물:

- 프로젝트 소개
- Pain Point/AI 솔루션/스프린트 구분 슬라이드
- 최종 발표 대본

### 2번: 백엔드 Course/User 담당

주요 책임:

- `user-service`, `course-service` API 확인
- HRD 도메인에 맞춘 Course/User 최소 수정 검토
- 교육 프로그램 등록/조회 API 안정화
- Course 일정/운영 필드 확인: `startDate`, `endDate`, `durationDays`, `deliveryType`, `targetAudience`, `region`, `difficulty`
- 필요 시 seed data 또는 description 구조 정리

산출물:

- 교육 프로그램 API 확인 결과
- 교육 공급자 등록 흐름
- Course 관련 Swagger 캡처

### 3번: 백엔드 Enrollment/Payment 담당

주요 책임:

- `enrollment-service`, `payment-service` 흐름 확인
- 교육 계약 신청 -> 결제 처리 -> 상태 변경 흐름 검증
- Kafka 이벤트 흐름은 결과 중심으로 확인
- 포트/컨테이너 트러블슈팅 문서화

산출물:

- 계약 신청 API 확인 결과
- payment completed 후 enrollment 상태 변화 캡처
- Enrollment/Payment Swagger 캡처

### 4번: 추천/만족도 담당

주요 책임:

- `recommend-service` API 확인
- 추천 로직을 HRD 교육 추천으로 설명할 수 있게 정리
- 만족도 결과 화면과 향후 Survey API 설계 관리
- 실제 Survey API는 구현 후보로 문서화

산출물:

- 추천 API 캡처
- 추천 근거 설명
- 만족도 결과 화면 캡처와 Survey API 후보 명세

### 5번: 프론트 핵심 화면 담당

주요 책임:

- Landing, CourseList, CourseDetail 변경
- HRD 니즈 입력 UI
- 추천 교육 카드 UI
- 계약 신청 버튼/흐름 연결

산출물:

- HRD 랜딩 화면
- 교육 탐색/상세/추천 화면
- API 연동 화면 캡처

### 6번: 프론트 상태/만족도/QA 담당

주요 책임:

- Login, EnrollmentView, SurveyView 변경
- 내 교육/계약 목록 상태 표시
- HRD 담당자용 만족도 결과 화면
- 전체 플로우 QA와 발표용 시나리오 캡처

산출물:

- 로그인/내 교육/만족도 결과 화면
- end-to-end 시연 체크리스트
- 오류/해결 기록

## 5. 팀 공통 작업 순서

1. Docker 컨테이너 정상 기동 확인
2. Swagger/FastAPI docs에서 각 API Try it out
3. API 명세서의 request/response 최신화
4. 프론트 화면 문구 변경
5. API 연동
6. 시연 시나리오 작성
7. 화면 스냅샷 캡처
8. 발표 자료 정리

## 6. 발표용 시연 시나리오

권장 시나리오:

```text
1. HRD 담당자 계정으로 로그인
2. 기업 교육 니즈 입력
3. 추천 교육 확인
4. 교육 프로그램 목록에서 상세 확인
5. 교육 계약 신청
6. 내 교육/계약 목록에서 상태 확인
7. 교육 확정 후 만족도 결과 화면 확인
```

교육 공급자 시나리오:

```text
1. 교육 공급자 계정으로 로그인
2. 기업 교육 프로그램 등록
3. HRD 담당자 화면에서 등록된 프로그램 확인
```

## 7. Definition of Done

Sprint 1 완료 기준:

- 프론트에서 로그인 후 주요 화면 접근 가능
- 교육 프로그램 목록/상세 API 연동
- 교육 일정/운영 필드 등록 및 표시
- 교육 추천 API 연동
- 교육 계약 신청 API 연동
- 내 교육/계약 목록 확인 가능
- 발표용 스크린샷 확보

Sprint 2 완료 기준:

- 계약 비용 처리 흐름을 설명/캡처 가능
- 교육별 만족도 결과 화면 또는 API 명세 완성
- 실제 만족도 제출 API는 향후 구현 후보로 정리
- 최종 발표 흐름과 산출물 정리 완료

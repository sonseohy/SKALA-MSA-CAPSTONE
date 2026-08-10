# 제출/발표 체크리스트

이 체크리스트는 `Agile_MSA_실습_가이드_수정_v2.pdf`의 발표 기획서 요구 흐름을 기준으로 정리했다.

## 1. 발표 기획서 필수 구성

### 1. 이해관계자 가치와 Pain Point

포함할 내용:

- 핵심 고객: 기업 HRD/HR 부서
- 보조 사용자: 교육 공급자, 기업 임직원
- Pain Point: 교육 니즈 정의, 공급자 탐색/비교, 계약/운영/만족도 관리가 수작업에 의존
- 왜 중요한가: 기업 전략 변화에 맞춰 빠르게 교육을 기획해야 하기 때문

준비물:

- Pain Point 슬라이드
- 기존 교육 기획 업무 흐름
- 우리 서비스가 줄이는 업무 단계

### 2. 이를 해결하기 위한 AI 솔루션

포함할 내용:

- 최종 지향점: 사업 방향/전략 문서 기반 교육 추천
- MVP 구현: 현재 recommend-service와 규칙 기반/키워드 기반 추천 활용
- 구현 한계: 실제 LLM/ML 모델은 향후 개발 예정
- 기획 의도: HRD 담당자의 교육 기획 의사결정을 보조하는 AX 서비스

주의:

- “실제 AI 모델을 완성했다”고 말하지 않는다.
- “AI 기반으로 확장 가능한 추천 구조를 MVP에서는 규칙 기반으로 구현했다”고 설명한다.

### 3. Sprint 구분

Sprint 1:

- 로그인
- 교육 니즈 입력
- 교육 프로그램 탐색
- 추천 교육 조회
- 교육 공급자/프로그램 상세 확인
- 교육 계약 신청
- 내 교육/계약 목록

Sprint 2:

- 현재 결제 흐름을 계약 비용 처리로 표현
- 교육별 만족도 결과 조회
- 실제 Survey API 설계
- 향후 교육 결과 분석 방향 제시

구분 이유:

- Sprint 1은 서비스 핵심 가치인 “교육 추천과 계약 신청”에 집중한다.
- Sprint 2는 있으면 좋지만 없어도 핵심 흐름은 가능한 기능이다.

### 4. 아키텍처 구성도

반드시 포함:

- Vue Frontend
- API Gateway
- Auth Server
- Eureka Server
- User Service
- Course Service
- Enrollment Service
- Payment Service
- Recommend Service
- MariaDB
- Kafka

권장 표현:

```text
Vue
  -> API Gateway
    -> User: HRD/공급자 계정
    -> Course: 기업 교육 프로그램
    -> Enrollment: 계약/참여 신청
    -> Payment: 계약 비용 처리
    -> Recommend: 추천 교육
```

이벤트 흐름:

```text
Enrollment 신청
  -> Payment 내부 요청
  -> payment.completed Kafka event
  -> Enrollment ACTIVE
  -> Course enrollmentCount 증가
```

### 5. API 명세

포함할 API:

- `POST /api/users/register`
- `GET /api/users/me`
- `GET /api/courses`
- `GET /api/courses/{id}`
- `POST /api/courses`
- `POST /api/enrollments`
- `GET /api/enrollments/my`
- `GET /api/recommend/{userId}`
- `GET /api/payments/{id}`
- Sprint 2 후보 Survey API 또는 현재 프론트 만족도 결과 화면 설명

각 API별로 포함:

- Method
- URL
- 인증 필요 여부
- Request 예시
- Response 예시
- HRD 도메인에서의 의미

주의:

- recommend-service 문서는 `http://localhost:8085/docs`
- Spring 서비스 Swagger는 `/swagger-ui/index.html`

### 6. 동작 화면 스냅샷

필수 캡처 후보:

- HRD 랜딩
- 로그인/회원가입
- 교육 니즈 입력
- 추천 교육 목록
- 교육 프로그램 목록
- 교육 프로그램 상세
- 교육 공급자 프로그램 등록 시 교육 일정/기간/방식 입력
- 교육 계약 신청 전/후
- 내 교육/계약 목록
- 교육별 만족도 결과 화면

캡처 기준:

- API 호출 전/후 상태 변화가 보이게 찍는다.
- 화면 제목/버튼 문구가 HRD 플랫폼 방향과 맞아야 한다.
- 가능하면 브라우저 Network 탭 또는 Swagger Try it out 결과도 함께 확보한다.

## 2. 실습 진행 체크리스트

- [ ] 팀 도메인 매핑표 확정
- [ ] Docker 컨테이너 전체 실행
- [ ] API Gateway 호출 가능 확인
- [ ] 각 서비스 Swagger/FastAPI docs 접속 확인
- [ ] 필요한 API Try it out 성공
- [ ] 프론트 로그인 토큰 저장 확인
- [ ] 프론트 API 호출이 Gateway를 경유하는지 확인
- [ ] 교육 프로그램 목록/상세 연동
- [ ] 교육 추천 연동
- [ ] 계약 신청 연동
- [ ] 내 교육/계약 목록 연동
- [ ] Course 일정/운영 필드가 화면/API에 반영됐는지 확인
- [ ] 만족도 결과 화면 또는 Survey API 범위 결정
- [ ] 발표용 스크린샷 수집
- [ ] 발표 자료에 Pain Point -> AI 솔루션 -> Sprint -> Architecture -> API -> 화면 흐름 반영

## 3. 개발 중 주의사항

### Docker

공용 IP 제한 때문에 다음 명령은 가급적 피한다.

```bash
docker compose build
docker compose up -d --build
```

이미지를 받은 경우 다음 방식으로 실행한다.

```bash
docker compose up -d --no-build --pull never
```

### 포트

| 포트 | 서비스 | 충돌 시 확인 |
|---:|---|---|
| 3379 | MariaDB | 다른 수업 MariaDB 컨테이너 |
| 8084 | payment-service | 로컬 Java 프로세스 |
| 8085 | recommend-service | FastAPI docs는 `/docs` |

### Frontend

macOS에서 `*.node` 파일 차단 팝업이 뜨면 다음 명령으로 quarantine을 제거한다.

```bash
xattr -dr com.apple.quarantine vue-frontend/node_modules
```

### API 호출

CORS 오류가 나면 개별 서비스 포트가 아니라 Gateway 주소를 쓰고 있는지 먼저 확인한다.

```text
권장: http://localhost:8080/api/...
주의: http://localhost:8081/api/... 직접 호출
```

## 4. 최종 제출 전 점검

- [ ] 프로젝트 기획 의도가 B2B HRD 플랫폼으로 설명되는가?
- [ ] 기존 MSA를 왜 유지했는지 설명되는가?
- [ ] AI 솔루션의 기획 방향과 MVP 구현 수준이 구분되는가?
- [ ] 결제를 실제 결제 시스템으로 과장하지 않았는가?
- [ ] 만족도 결과 화면과 향후 Survey API 범위가 구분되어 있는가?
- [ ] API 명세가 실제 현재 코드와 크게 어긋나지 않는가?
- [ ] 팀원별 역할과 산출물이 하나의 Sprint 목표로 연결되는가?
- [ ] 화면 캡처가 API 동작을 증명하는가?

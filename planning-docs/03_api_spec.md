# API 명세서

## 1. 공통 호출 원칙

프론트엔드는 가능하면 API Gateway를 경유한다.

```text
Frontend: http://localhost:3000
Gateway:  http://localhost:8080
```

Axios 기본 URL은 Vite proxy 또는 `VITE_API_BASE_URL=http://localhost:8080` 기준으로 맞춘다.

인증이 필요한 API는 다음 헤더를 사용한다.

```http
Authorization: Bearer {accessToken}
Content-Type: application/json
```

## 2. Swagger/문서 URL

| 서비스 | 포트 | 문서 URL |
|---|---:|---|
| user-service | 8081 | http://localhost:8081/swagger-ui/index.html |
| course-service | 8082 | http://localhost:8082/swagger-ui/index.html |
| enrollment-service | 8083 | http://localhost:8083/swagger-ui/index.html |
| payment-service | 8084 | http://localhost:8084/swagger-ui/index.html |
| recommend-service | 8085 | http://localhost:8085/docs |

`recommend-service`는 FastAPI라서 `/swagger-ui/index.html`이 아니라 `/docs`를 사용한다.

## 3. 응답 래퍼

Spring 서비스의 일반 응답은 대체로 다음 형태다.

```json
{
  "success": true,
  "message": "성공",
  "data": {}
}
```

내부 API 또는 recommend-service는 래퍼 없이 응답할 수 있으므로 프론트에서 API별 응답 형태를 확인한다.

## 4. User API

### 회원가입

```http
POST /api/users/register
```

HRD 의미:

- HRD 담당자 또는 교육 공급자 계정 생성

Request:

```json
{
  "email": "hrd@example.com",
  "password": "password123",
  "name": "김HRD",
  "role": "STUDENT"
}
```

Role 매핑:

| 화면 문구 | 실제 값 |
|---|---|
| 기업 HRD 담당자 / 임직원 | STUDENT |
| 교육 공급자 | INSTRUCTOR |

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 1,
    "email": "hrd@example.com",
    "name": "김HRD",
    "role": "STUDENT",
    "createdAt": "2026-08-10T10:00:00"
  }
}
```

### 내 정보 조회

```http
GET /api/users/me
Authorization: Bearer {token}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 1,
    "email": "hrd@example.com",
    "name": "김HRD",
    "role": "STUDENT",
    "createdAt": "2026-08-10T10:00:00"
  }
}
```

### 사용자 단건 조회

```http
GET /api/users/{id}
```

프론트에서는 공급자 이름 표시가 필요할 때 사용할 수 있다.

## 5. Course API

### 교육 프로그램 등록

```http
POST /api/courses
Authorization: Bearer {token}
```

HRD 의미:

- 교육 공급자가 기업 교육 프로그램을 등록한다.

Request:

```json
{
  "title": "생성형 AI 업무 활용 교육",
  "description": "대상: 사무직/기획자\n방식: 온라인/오프라인 가능\n커리큘럼: 프롬프트 작성, 업무 자동화, 보안 주의사항\n공급자 경력: 기업 AI 교육 20회",
  "category": "DATA_SCIENCE",
  "price": 99000
}
```

현재 Category enum:

```text
BACKEND, FRONTEND, DEVOPS, DATA_SCIENCE, MOBILE, SECURITY, DATABASE, OTHER
```

화면 표시 권장:

| enum | HRD 표시 문구 |
|---|---|
| BACKEND | 백엔드/서버 |
| FRONTEND | 프론트엔드 |
| DEVOPS | Cloud/DevOps |
| DATA_SCIENCE | AI/Data |
| MOBILE | 모바일 |
| SECURITY | 보안 |
| DATABASE | 데이터베이스 |
| OTHER | 기타 기업 교육 |

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 10,
    "title": "생성형 AI 업무 활용 교육",
    "description": "대상: 사무직/기획자...",
    "category": "DATA_SCIENCE",
    "price": 99000,
    "instructorId": 2,
    "enrollmentCount": 0,
    "status": "ACTIVE",
    "createdAt": "2026-08-10T10:00:00"
  }
}
```

### 교육 프로그램 목록

```http
GET /api/courses
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "id": 10,
      "title": "생성형 AI 업무 활용 교육",
      "description": "대상: 사무직/기획자...",
      "category": "DATA_SCIENCE",
      "price": 99000,
      "instructorId": 2,
      "enrollmentCount": 12,
      "status": "ACTIVE",
      "createdAt": "2026-08-10T10:00:00"
    }
  ]
}
```

### 교육 프로그램 상세

```http
GET /api/courses/{id}
```

### 카테고리별 교육 프로그램 조회

```http
GET /api/courses/category/{category}
```

예:

```http
GET /api/courses/category/DATA_SCIENCE
```

## 6. Enrollment API

### 교육 계약/참여 신청

```http
POST /api/enrollments
Authorization: Bearer {token}
```

HRD 의미:

- HRD 담당자가 교육 계약 신청을 한다.
- 임직원 관점에서는 교육 참여 신청으로 볼 수 있다.
- 현재 구현에서는 신청 후 payment-service로 내부 결제 요청이 발생한다.

Request:

```json
{
  "courseId": 10
}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 5,
    "userId": 1,
    "courseId": 10,
    "status": "PENDING",
    "createdAt": "2026-08-10T10:00:00",
    "course": null
  }
}
```

상태 매핑:

| 기존 상태 | HRD 화면 문구 |
|---|---|
| PENDING | 계약/참여 신청 중 |
| ACTIVE | 교육 확정 |
| CANCELLED | 취소 |

### 내 교육/계약 목록

```http
GET /api/enrollments/my
Authorization: Bearer {token}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": [
    {
      "id": 5,
      "userId": 1,
      "courseId": 10,
      "status": "ACTIVE",
      "createdAt": "2026-08-10T10:00:00",
      "course": {
        "id": 10,
        "title": "생성형 AI 업무 활용 교육",
        "description": "대상: 사무직/기획자...",
        "category": "AI/Data",
        "price": 99000,
        "thumbnail": null,
        "instructorName": null,
        "enrollmentCount": 12
      }
    }
  ]
}
```

### 특정 사용자 교육/계약 목록

```http
GET /api/enrollments/user/{userId}
```

## 7. Payment API

### 내부 결제 요청

```http
POST /api/payments/internal/request
```

프론트에서 직접 호출하지 않는다. `POST /api/enrollments` 호출 시 enrollment-service가 내부적으로 payment-service에 요청한다.

이번 프로젝트에서는 실제 결제 시스템을 구현하지 않고, 현재 실습용 자동 성공 결제를 계약 비용 처리로 활용한다.

### 결제 단건 조회

```http
GET /api/payments/{id}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "paymentId": 1,
    "userId": 1,
    "courseId": 10,
    "amount": 99000,
    "status": "COMPLETED",
    "transactionId": "uuid-value",
    "createdAt": "2026-08-10T10:00:00"
  }
}
```

### 사용자 결제 내역

```http
GET /api/payments/user/{userId}
```

## 8. Recommend API

### 교육 추천 조회

```http
GET /api/recommend/{userId}
Authorization: Bearer {token}
```

HRD 의미:

- MVP에서는 사용자 이력/카테고리 기반 추천을 교육 추천으로 사용한다.
- 향후 기업 니즈 키워드 기반 추천으로 확장할 수 있다.

Response:

```json
{
  "userId": 1,
  "recommendedCourses": [
    {
      "id": 10,
      "title": "생성형 AI 업무 활용 교육",
      "description": "대상: 사무직/기획자...",
      "category": "DATA_SCIENCE",
      "price": 99000,
      "instructorId": 2,
      "enrollmentCount": 12,
      "status": "ACTIVE",
      "createdAt": "2026-08-10T10:00:00"
    }
  ],
  "basedOnCategory": "DATA_SCIENCE",
  "message": "DATA_SCIENCE 카테고리 기반 추천 강의입니다"
}
```

### Recommend Health

```http
GET /health
GET /api/recommend/health
```

FastAPI 문서:

```text
http://localhost:8085/docs
```

## 9. Sprint 2 후보 API: 만족도 조사

현재 구현되어 있지 않으므로 신규 구현 후보이다. 최소 변경 원칙상 `enrollment-service`에 추가하는 방향을 권장한다.

### 만족도 제출

```http
POST /api/enrollments/{enrollmentId}/survey
Authorization: Bearer {token}
```

Request:

```json
{
  "educationScore": 5,
  "instructorScore": 5,
  "usefulnessScore": 4,
  "difficultyScore": 3,
  "comment": "실습 중심이라 업무 적용에 도움이 되었습니다."
}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "id": 1,
    "enrollmentId": 5,
    "courseId": 10,
    "userId": 1,
    "educationScore": 5,
    "instructorScore": 5,
    "usefulnessScore": 4,
    "difficultyScore": 3,
    "comment": "실습 중심이라 업무 적용에 도움이 되었습니다.",
    "createdAt": "2026-08-10T10:00:00"
  }
}
```

### 만족도 요약

```http
GET /api/enrollments/courses/{courseId}/surveys/summary
Authorization: Bearer {token}
```

Response:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "courseId": 10,
    "surveyCount": 20,
    "averageEducationScore": 4.6,
    "averageInstructorScore": 4.7,
    "averageUsefulnessScore": 4.4,
    "averageDifficultyScore": 3.1
  }
}
```

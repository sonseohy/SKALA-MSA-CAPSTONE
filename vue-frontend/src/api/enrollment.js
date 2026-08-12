import api from './index.js'

export const enrollmentApi = {
  // 내 계약 목록. 응답 data = {content, page, size, totalElements, totalPages, first, last,
  // summary:{active, pending, total}}. page 는 0 부터 세고, summary 는 상태 필터와 무관한 전체 집계다.
  // content 의 각 항목에는 만족도 제출 여부(surveySubmitted)가 실려 온다.
  // params: { page, size, status }
  getMy(params) {
    return api.get('/api/enrollments/my', { params })
  },

  enroll(courseId) {
    return api.post('/api/enrollments', { courseId })
  }
}

// 만족도 조사 — planning-docs/03_api_spec.md "9. Sprint 2 후보 API" 규격.
// 백엔드 구현 전에는 404/405 가 돌아오므로 호출부에서 미구현 상태로 안내한다.
export const surveyApi = {
  submit(enrollmentId, payload) {
    return api.post(`/api/enrollments/${enrollmentId}/survey`, payload)
  },

  getByEnrollment(enrollmentId) {
    return api.get(`/api/enrollments/${enrollmentId}/survey`)
  },

  getCourseSummary(courseId) {
    return api.get(`/api/enrollments/courses/${courseId}/surveys/summary`)
  }
}

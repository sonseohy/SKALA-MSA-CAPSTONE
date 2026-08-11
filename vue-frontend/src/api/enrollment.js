import api from './index.js'

export const enrollmentApi = {
  getMyEnrollments() {
    return api.get('/api/enrollments/my')
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

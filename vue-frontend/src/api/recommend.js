import api from './index.js'

// recommend-service (FastAPI, :8085) — planning-docs/03_api_spec.md "8. Recommend API"
// 응답은 래퍼 없이 { userId, recommendedCourses, basedOnCategory, message } 형태로 온다.
export const recommendApi = {
  getForUser(userId) {
    return api.get(`/api/recommend/${userId}`)
  }
}

import api from './index.js'

// 목록은 서버 페이징이다. 응답 data = PageResult
// {content, page, size, totalElements, totalPages, first, last}. page 는 0 부터 센다.
// params: { page, size, sort, category, deliveryType, keyword, region }
// sort: popular(기본) | latest | priceAsc | priceDesc
const list = (params) => api.get('/api/courses', { params })

export const courseApi = {
  getCourses: list,

  // 공급자 목록·대시보드·추천처럼 카탈로그 전량을 집계하는 화면용.
  // 서버 기본 size(12)에 잘리지 않게 한 번에 크게 받는다.
  // ponytail: 서버가 size 를 100(MAX_PAGE_SIZE)으로 자른다. 카탈로그가 100건을 넘으면
  //           집계가 앞 100건만 반영하므로, 그때는 페이지 순회로 바꿔야 한다.
  getAll: (params) => list({ size: 100, ...params }),

  getById(id) {
    return api.get(`/api/courses/${id}`)
  },

  getByCategory(category) {
    return api.get(`/api/courses/category/${category}`)
  },

  create(data) {
    return api.post('/api/courses', data)
  }
}

import { userApi } from '@/api/auth.js'

// course 응답에는 instructorId 만 있고 이름이 없다(normalizeCourse 의 `Provider #id` 폴백 참고).
// 여러 화면이 같은 공급자를 반복 조회하므로, 모듈 레벨 캐시로 재조회를 막는다.
const nameCache = new Map()

/**
 * course 목록의 instructorId 를 공급자 이름으로 채운다.
 * @returns {{ resolve: (courses: Array) => Promise<Array> }} resolve — course 배열을 받아
 *   providerName 이 채워진 새 배열을 반환한다. 이미 캐시된 id 는 재조회하지 않는다.
 */
export function useProviderNames() {
  /**
   * @param {Array<{instructorId?: number|string}>} courses
   * @returns {Promise<Array>} providerName 이 추가된 course 배열 (개별 조회 실패 시 해당 course 는 원본 그대로 반환)
   */
  async function resolve(courses) {
    const list = Array.isArray(courses) ? courses : []
    const idsToFetch = [...new Set(list.map(course => course.instructorId).filter(Boolean))]
      .filter(id => !nameCache.has(id))

    await Promise.all(idsToFetch.map(async id => {
      try {
        const res = await userApi.getById(id)
        const name = res.data?.data?.name
        if (name) nameCache.set(id, name)
      } catch (error) {
        // 개별 조회 실패는 무시한다 — normalizeCourse 의 `Provider #id` 폴백이 이미 있다.
        console.error('[useProviderNames] 공급자 조회 실패:', id, error)
      }
    }))

    return list.map(course => (
      course.instructorId && nameCache.has(course.instructorId)
        ? { ...course, providerName: nameCache.get(course.instructorId) }
        : course
    ))
  }

  return { resolve }
}

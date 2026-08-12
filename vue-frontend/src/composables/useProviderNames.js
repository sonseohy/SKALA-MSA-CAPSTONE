import { userApi } from '@/api/auth.js'
import { unwrapListResponse } from '@/utils/hrd.js'

// course 응답에는 instructorId 만 있고 이름이 없다(normalizeCourse 의 `Provider #id` 폴백 참고).
// 여러 화면이 같은 공급자를 반복 조회하므로, 모듈 레벨 캐시로 재조회를 막는다.
// 키는 숫자로 통일한다 — course.instructorId 가 문자열로 올 수 있어 응답 id 와 어긋난다.
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
    const idsToFetch = [...new Set(list.map(course => Number(course.instructorId)).filter(Number.isFinite))]
      .filter(id => !nameCache.has(id))

    // 공급자마다 따로 부르면 목록 길이만큼 요청이 나간다. 한 번에 모아 조회한다.
    if (idsToFetch.length) {
      try {
        for (const user of unwrapListResponse(await userApi.getUsers({ ids: idsToFetch }))) {
          if (user?.name) nameCache.set(Number(user.id), user.name)
        }
      } catch (error) {
        // 조회 실패는 무시한다 — normalizeCourse 의 `Provider #id` 폴백이 이미 있다.
        console.error('[useProviderNames] 공급자 조회 실패:', idsToFetch, error)
      }
    }

    return list.map(course => (
      nameCache.has(Number(course.instructorId))
        ? { ...course, providerName: nameCache.get(Number(course.instructorId)) }
        : course
    ))
  }

  return { resolve }
}

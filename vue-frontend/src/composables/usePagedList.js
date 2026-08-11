import { computed, ref, watch } from 'vue'

/**
 * 목록을 페이지 단위로 잘라 준다.
 *
 * 지금 백엔드는 목록 API 가 전량을 반환하므로(CourseController:39 등) 받아온 배열을
 * 화면에서 자른다. 나중에 서버 페이징(`?page=&size=`)이 생기면 이 파일의 items 계산만
 * API 호출로 바꾸면 되고, 화면 코드는 그대로 둔다.
 *
 * @param {import('vue').Ref<Array>} source 전체 목록 (필터·정렬까지 끝난 상태)
 * @param {number} pageSize 한 페이지에 보일 개수
 */
export function usePagedList(source, pageSize = 12) {
  const page = ref(1)

  const total = computed(() => source.value.length)
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

  const items = computed(() => {
    const start = (page.value - 1) * pageSize
    return source.value.slice(start, start + pageSize)
  })

  // 표시용 번호. 항목이 없으면 0 부터 0 까지로 둔다.
  const rangeStart = computed(() => (total.value ? (page.value - 1) * pageSize + 1 : 0))
  const rangeEnd = computed(() => Math.min(page.value * pageSize, total.value))

  // 검색·필터로 목록이 줄어 현재 페이지가 사라지면 첫 페이지로 되돌린다.
  watch(total, () => {
    if (page.value > totalPages.value) page.value = 1
  })

  function go(next) {
    page.value = Math.min(Math.max(1, next), totalPages.value)
  }

  // 페이지가 많아도 버튼이 넘치지 않게 현재 위치 주변만 보여 준다.
  // 생략 구간은 null 로 표시한다.
  const pageNumbers = computed(() => {
    const last = totalPages.value
    if (last <= 7) return Array.from({ length: last }, (_, i) => i + 1)

    const current = page.value
    const numbers = new Set([1, last, current])
    for (let offset = 1; offset <= 1; offset += 1) {
      if (current - offset > 1) numbers.add(current - offset)
      if (current + offset < last) numbers.add(current + offset)
    }
    if (current <= 3) [2, 3, 4].forEach(n => n < last && numbers.add(n))
    if (current >= last - 2) [last - 3, last - 2, last - 1].forEach(n => n > 1 && numbers.add(n))

    const sorted = [...numbers].sort((a, b) => a - b)
    const withGaps = []
    sorted.forEach((n, index) => {
      if (index && n - sorted[index - 1] > 1) withGaps.push(null)
      withGaps.push(n)
    })
    return withGaps
  })

  return { page, items, total, totalPages, rangeStart, rangeEnd, pageNumbers, go }
}

import { computed, ref, watch } from 'vue'

/**
 * 페이지 번호·표시 구간 계산. 클라이언트 페이징(usePagedList)과
 * 서버 페이징(useServerPager)이 같은 규칙을 쓰도록 여기에 모아 둔다.
 *
 * @param {import('vue').Ref<number>} page 현재 페이지 (1 부터)
 * @param {import('vue').Ref<number>|import('vue').ComputedRef<number>} total 전체 건수
 * @param {number} pageSize 한 페이지에 보일 개수
 */
function usePagerNumbers(page, total, pageSize) {
  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize)))

  // 표시용 번호. 항목이 없으면 0 부터 0 까지로 둔다.
  const rangeStart = computed(() => (total.value ? (page.value - 1) * pageSize + 1 : 0))
  const rangeEnd = computed(() => Math.min(page.value * pageSize, total.value))

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

  return { totalPages, rangeStart, rangeEnd, pageNumbers, go }
}

/**
 * 목록을 페이지 단위로 잘라 준다.
 *
 * 서버가 전량을 주는 목록(추천, 공급자 상세의 등록 프로그램 등)에 쓴다.
 * 서버 페이징(`?page=&size=`)이 있는 목록은 useServerPager 를 쓴다.
 *
 * @param {import('vue').Ref<Array>} source 전체 목록 (필터·정렬까지 끝난 상태)
 * @param {number} pageSize 한 페이지에 보일 개수
 */
export function usePagedList(source, pageSize = 12) {
  const page = ref(1)
  const total = computed(() => source.value.length)
  const { totalPages, ...rest } = usePagerNumbers(page, total, pageSize)

  const items = computed(() => {
    const start = (page.value - 1) * pageSize
    return source.value.slice(start, start + pageSize)
  })

  // 검색·필터로 목록이 줄어 현재 페이지가 사라지면 첫 페이지로 되돌린다.
  watch(total, () => {
    if (page.value > totalPages.value) page.value = 1
  })

  return { page, items, total, totalPages, ...rest }
}

/**
 * 서버 페이징 목록의 페이지 상태.
 * 항목은 화면이 직접 들고, 여기서는 페이지 번호와 전체 건수만 다룬다.
 * total 에 응답의 totalElements 를 넣으면 PagerBar 가 그대로 쓴다.
 * 서버 page 는 0 부터 세므로 요청 시 `page.value - 1` 을 보낸다.
 *
 * @param {number} pageSize 요청에 실을 size
 */
export function useServerPager(pageSize = 12) {
  const page = ref(1)
  const total = ref(0)

  return { page, total, size: pageSize, ...usePagerNumbers(page, total, pageSize) }
}

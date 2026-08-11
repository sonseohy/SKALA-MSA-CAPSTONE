<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Program Catalog</p>
        <h1>기업 교육 프로그램 카탈로그</h1>
        <p>교육 분야, 공급자, 예상 비용을 비교하고 계약 신청까지 진행합니다.</p>
      </div>
      <router-link v-if="isInstructor" to="/courses/new" class="btn btn-primary">새 프로그램 등록</router-link>
    </section>

    <section class="filter-panel">
      <button
        v-for="cat in categories"
        :key="cat.value"
        type="button"
        class="filter-chip"
        :class="{ active: selectedCategory === cat.value }"
        @click="selectedCategory = cat.value"
      >
        {{ cat.label }}
      </button>
    </section>

    <form class="list-toolbar" @submit.prevent="applySearch">
      <div class="toolbar-search">
        <label for="courseSearch">검색</label>
        <input
          id="courseSearch"
          v-model.trim="keyword"
          type="search"
          placeholder="교육명, 공급자, 교육 대상으로 검색"
        />
      </div>
      <button type="submit" class="btn btn-primary toolbar-search-btn">검색</button>
      <div class="toolbar-field">
        <label for="deliveryFilter">교육 방식</label>
        <!-- 라벨을 화면에서 숨겼으므로 첫 항목이 필터 이름을 대신한다 -->
        <select id="deliveryFilter" v-model="deliveryFilter">
          <option value="">교육 방식 전체</option>
          <option v-for="option in deliveryTypeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </div>
      <div class="toolbar-field">
        <label for="regionFilter">교육 지역</label>
        <select id="regionFilter" v-model="regionFilter">
          <option value="">교육 지역 전체</option>
          <option v-for="region in knownRegions" :key="region" :value="region">{{ region }}</option>
        </select>
      </div>
      <div class="toolbar-field">
        <label for="sortBy">정렬</label>
        <select id="sortBy" v-model="sortBy">
          <option v-for="option in sortOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
      </div>
    </form>

    <div v-if="loading" class="program-grid">
      <div v-for="i in 6" :key="i" class="panel skeleton-card"></div>
    </div>

    <template v-else-if="filteredCourses.length">
      <section class="program-grid">
        <ProgramCard v-for="course in pager.items.value" :key="course.id" :course="course" />
      </section>
      <PagerBar
        :page="pager.page.value"
        :total-pages="pager.totalPages.value"
        :total="pager.total.value"
        :range-start="pager.rangeStart.value"
        :range-end="pager.rangeEnd.value"
        :page-numbers="pager.pageNumbers.value"
        unit="개 프로그램"
        @go="pager.go"
      />
    </template>

    <section v-else class="empty-panel">
      <h2>{{ loadError ? '교육 목록을 불러오지 못했습니다.' : '조건에 맞는 프로그램이 없습니다.' }}</h2>
      <p v-if="loadError">백엔드 서비스가 기동되어 있는지 확인한 뒤 다시 시도해 주세요.</p>
      <p v-else-if="hasCondition">검색어나 필터를 바꿔 보세요.</p>
      <p v-else>공급자 계정으로 첫 교육 프로그램을 등록해 주세요.</p>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import ProgramCard from '@/components/ProgramCard.vue'
import PagerBar from '@/components/PagerBar.vue'
import { courseApi } from '@/api/course.js'
import { useAuthStore } from '@/store/auth.js'
import { usePagedList } from '@/composables/usePagedList.js'
import { categoryOptions, deliveryTypeOptions, normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const loading = ref(true)
const loadError = ref(false)
const courses = ref([])

const selectedCategory = ref('ALL')
const keyword = ref('')
const appliedKeyword = ref('')
const deliveryFilter = ref('')
const regionFilter = ref('')
const sortBy = ref('POPULAR')

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')
const categories = [{ value: 'ALL', label: '전체' }, ...categoryOptions]

const sortOptions = [
  { value: 'POPULAR', label: '신청 많은 순' },
  { value: 'RECENT', label: '최근 등록순' },
  { value: 'PRICE_ASC', label: '교육비 낮은 순' },
  { value: 'PRICE_DESC', label: '교육비 높은 순' },
  { value: 'TITLE', label: '교육명 순' }
]

// 필터 후보는 실제 등록된 값에서만 만든다.
const knownRegions = computed(() => [...new Set(
  courses.value.flatMap(course => (course.region || '').split(',').map(r => r.trim()))
)].filter(Boolean).sort())

const hasCondition = computed(() => (
  selectedCategory.value !== 'ALL' || appliedKeyword.value || deliveryFilter.value || regionFilter.value
))

// 검색어는 버튼(또는 Enter)을 눌렀을 때만 반영한다.
// 카테고리·필터·정렬은 고른 즉시 반영한다.
function applySearch() {
  appliedKeyword.value = keyword.value
}

const filteredCourses = computed(() => {
  const word = appliedKeyword.value.trim().toLowerCase()

  const matched = courses.value.filter(course => {
    if (selectedCategory.value !== 'ALL' && course.category !== selectedCategory.value) return false
    if (deliveryFilter.value && course.deliveryType !== deliveryFilter.value) return false
    if (regionFilter.value && !(course.region || '').includes(regionFilter.value)) return false
    if (!word) return true
    return [course.title, course.providerName, course.targetAudience, course.categoryLabel]
      .some(field => (field || '').toLowerCase().includes(word))
  })

  const sorted = [...matched]
  const num = value => Number(value) || 0
  if (sortBy.value === 'POPULAR') sorted.sort((a, b) => num(b.enrollmentCount) - num(a.enrollmentCount))
  else if (sortBy.value === 'RECENT') sorted.sort((a, b) => String(b.createdAt || '').localeCompare(String(a.createdAt || '')))
  else if (sortBy.value === 'PRICE_ASC') sorted.sort((a, b) => num(a.price) - num(b.price))
  else if (sortBy.value === 'PRICE_DESC') sorted.sort((a, b) => num(b.price) - num(a.price))
  else if (sortBy.value === 'TITLE') sorted.sort((a, b) => String(a.title || '').localeCompare(String(b.title || ''), 'ko'))
  return sorted
})

const pager = usePagedList(filteredCourses, 12)

onMounted(async () => {
  try {
    const res = await courseApi.getAll()
    courses.value = unwrapListResponse(res).map(normalizeCourse)
  } catch (error) {
    // catch 가 없으면 조회 실패가 "결과 없음" 으로 보여 원인을 숨긴다.
    console.error('[CourseList] 교육 목록 조회 실패:', error)
    courses.value = []
    loadError.value = true
  } finally {
    loading.value = false
  }
})
</script>

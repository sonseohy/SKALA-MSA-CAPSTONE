<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Contracts &amp; Enrollments</p>
        <h1>{{ isTrainingPage ? '진행 중인 교육' : '내 교육/계약 목록' }}</h1>
        <p>{{ isTrainingPage ? '교육 확정 상태의 프로그램과 참여 현황을 확인합니다.' : '신청한 교육 계약과 참여 상태를 확인합니다.' }}</p>
      </div>
      <router-link to="/courses" class="btn btn-primary">새 교육 찾기</router-link>
    </section>

    <section v-if="loading" class="panel">계약 목록을 불러오는 중입니다.</section>

    <!-- 상태 필터가 0건이어도 필터를 되돌릴 수 있어야 하므로, 화면 전환은 요약(필터와 무관)으로 판단한다. -->
    <template v-else-if="hasAnyContract">
      <form class="list-toolbar" @submit.prevent="applySearch">
        <div class="toolbar-search">
          <label for="enrollSearch">검색</label>
          <input id="enrollSearch" v-model.trim="keyword" type="search" placeholder="교육명, 계약 번호로 검색" />
        </div>
        <button type="submit" class="btn btn-primary toolbar-search-btn">검색</button>
        <div class="toolbar-field">
          <label for="categoryFilter">교육 분야</label>
          <select id="categoryFilter" v-model="categoryFilter">
            <option value="">교육 분야 전체</option>
            <option v-for="cat in knownCategories" :key="cat" :value="cat">{{ normalizeCategory(cat) }}</option>
          </select>
        </div>
        <div v-if="!isTrainingPage" class="toolbar-field">
          <label for="statusFilter">상태</label>
          <select id="statusFilter" v-model="statusFilter">
            <option value="">상태 전체</option>
            <option v-for="status in statusOptions" :key="status" :value="status">{{ statusLabel(status) }}</option>
          </select>
        </div>
        <div class="toolbar-field">
          <label for="enrollSort">정렬</label>
          <select id="enrollSort" v-model="sortBy">
            <option v-for="option in sortOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
      </form>

      <!-- 상태·페이지는 서버가 처리한다. 서버가 지원하지 않는 조건은 받아 온 페이지 안에서만 적용된다. -->
      <p v-if="hasPageOnlyCondition" class="muted-note">
        검색어·교육 분야·정렬은 현재 페이지 안에서만 적용됩니다. 전체에서 찾으려면 상태 필터와 페이지 이동을 함께 사용해 주세요.
      </p>

      <template v-if="filteredItems.length">
        <section class="table-panel">
          <table>
            <thead>
              <tr>
                <th>교육 프로그램</th>
                <th>교육 분야</th>
                <th>일정/방식</th>
                <th>비용</th>
                <th>상태</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in filteredItems" :key="item.id">
                <td>
                  <router-link :to="`/courses/${item.courseId}`" class="table-title-link">
                    {{ item.course?.title || `Program #${item.courseId}` }}
                  </router-link>
                  <small>Contract #{{ item.id }}</small>
                </td>
                <td>{{ item.course?.category ? normalizeCategory(item.course.category) : '-' }}</td>
                <td>
                  {{ formatSchedule(item.course?.startDate, item.course?.endDate) }}
                  <small>{{ formatDuration(item.course?.durationDays) }} · {{ deliveryTypeLabel(item.course?.deliveryType) }}</small>
                </td>
                <td>{{ formatPrice(item.course?.price) }}</td>
                <td><span class="status-badge" :class="item.status === 'ACTIVE' ? 'active' : 'pending'">{{ statusLabel(item.status) }}</span></td>
                <td>
                  <router-link :to="`/courses/${item.courseId}`" class="text-link">상세</router-link>
                  <router-link v-if="item.status === 'ACTIVE'" :to="`/surveys?courseId=${item.courseId}`" class="text-link">만족도 결과</router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </section>
        <PagerBar
          :page="pager.page.value"
          :total-pages="pager.totalPages.value"
          :total="pager.total.value"
          :range-start="pager.rangeStart.value"
          :range-end="pager.rangeEnd.value"
          :page-numbers="pager.pageNumbers.value"
          unit="건"
          @go="pager.go"
        />
      </template>

      <section v-else class="empty-panel">
        <h2>조건에 맞는 계약이 없습니다.</h2>
        <p>검색어나 필터를 바꿔 보세요.</p>
      </section>
    </template>

    <section v-else class="empty-panel">
      <h2 v-if="loadError">계약 목록을 불러오지 못했습니다.</h2>
      <h2 v-else>{{ isTrainingPage ? '진행 중인 교육이 없습니다.' : '아직 신청한 교육이 없습니다.' }}</h2>
      <p>{{ isTrainingPage ? '교육 계약이 확정되면 이곳에서 진행 중인 교육으로 확인할 수 있습니다.' : '카탈로그에서 기업 니즈에 맞는 교육을 찾아 계약을 신청해 보세요.' }}</p>
      <router-link to="/courses" class="btn btn-primary">Program Catalog</router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import PagerBar from '@/components/PagerBar.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { useServerPager } from '@/composables/usePagedList.js'
import {
  deliveryTypeLabel,
  formatDuration,
  formatPrice,
  formatSchedule,
  normalizeCategory,
  statusLabel,
  unwrapObjectResponse
} from '@/utils/hrd.js'

const loading = ref(true)
const loadError = ref(false)
const items = ref([])
// 상태 필터·페이지와 무관한 전체 집계. 화면 전환(빈 화면 여부) 판단에 쓴다.
const summary = ref({ active: 0, pending: 0, total: 0 })
const route = useRoute()

const keyword = ref('')
const appliedKeyword = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const sortBy = ref('RECENT')

const sortOptions = [
  { value: 'RECENT', label: '최근 신청순' },
  { value: 'OLDEST', label: '오래된 순' },
  { value: 'TITLE', label: '교육명 순' },
  { value: 'PRICE_DESC', label: '비용 높은 순' }
]

const isTrainingPage = computed(() => route.path === '/trainings')

// 상태는 서버가 걸러 준다(진행 중인 교육 화면은 ACTIVE 고정).
const serverStatus = computed(() => (isTrainingPage.value ? 'ACTIVE' : (statusFilter.value || undefined)))

// 진행 중인 교육 화면은 확정 건만 다루므로 확정 건수로 판단한다.
const hasAnyContract = computed(() => (isTrainingPage.value ? summary.value.active : summary.value.total))

// 상태 후보는 enum 전체를 쓴다. 서버 페이징이라 현재 페이지에 없는 상태도 고를 수 있어야 한다.
const statusOptions = ['PENDING', 'ACTIVE', 'CANCELLED', 'COMPLETED', 'FAILED']
// 분야 필터는 서버가 지원하지 않아 받아 온 페이지 안에서만 적용된다. 후보도 그 페이지에서 만든다.
const knownCategories = computed(() => [...new Set(items.value.map(i => i.course?.category).filter(Boolean))])

const hasPageOnlyCondition = computed(() => Boolean(appliedKeyword.value || categoryFilter.value || sortBy.value !== 'RECENT'))

// 검색어는 버튼(또는 Enter)을 눌렀을 때만 반영한다. 필터·정렬은 고른 즉시 반영한다.
function applySearch() {
  appliedKeyword.value = keyword.value
}

const filteredItems = computed(() => {
  const word = appliedKeyword.value.trim().toLowerCase()

  const matched = items.value.filter(item => {
    if (categoryFilter.value && item.course?.category !== categoryFilter.value) return false
    if (!word) return true
    return [item.course?.title, `contract #${item.id}`, String(item.id)]
      .some(field => (field || '').toLowerCase().includes(word))
  })

  const sorted = [...matched]
  const created = item => String(item.createdAt || '')
  if (sortBy.value === 'RECENT') sorted.sort((a, b) => created(b).localeCompare(created(a)))
  else if (sortBy.value === 'OLDEST') sorted.sort((a, b) => created(a).localeCompare(created(b)))
  else if (sortBy.value === 'TITLE') sorted.sort((a, b) => String(a.course?.title || '').localeCompare(String(b.course?.title || ''), 'ko'))
  else if (sortBy.value === 'PRICE_DESC') sorted.sort((a, b) => (Number(b.course?.price) || 0) - (Number(a.course?.price) || 0))
  return sorted
})

const pager = useServerPager(15)

// 상태·페이지는 서버 질의로 처리한다.
async function load() {
  loading.value = true
  loadError.value = false
  try {
    const res = await enrollmentApi.getMy({
      page: pager.page.value - 1, // 서버 page 는 0 부터
      size: pager.size,
      status: serverStatus.value
    })
    const result = unwrapObjectResponse(res) || {}
    items.value = Array.isArray(result.content) ? result.content : []
    pager.total.value = Number(result.totalElements) || 0
    if (result.summary) summary.value = result.summary
  } catch (error) {
    console.error('[Enrollment] 계약 목록 조회 실패:', error)
    items.value = []
    pager.total.value = 0
    summary.value = { active: 0, pending: 0, total: 0 }
    loadError.value = true
  } finally {
    loading.value = false
  }
}

// 상태를 바꾸면 첫 페이지부터 다시 본다. 페이지가 실제로 바뀌면 아래 watch 가 조회한다.
watch(serverStatus, () => {
  if (pager.page.value !== 1) pager.page.value = 1
  else load()
})

watch(pager.page, load)

onMounted(load)
</script>

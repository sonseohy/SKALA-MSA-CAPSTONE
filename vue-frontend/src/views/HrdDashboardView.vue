<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">HRD Dashboard</p>
        <h1>{{ dashboardTitle }}</h1>
        <p>{{ dashboardDescription }}</p>
      </div>
      <router-link :to="primaryAction.to" class="btn btn-primary">
        <span>+</span>
        {{ primaryAction.label }}
      </router-link>
    </section>

    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card">
        <div class="metric-top">
          <span>{{ metric.label }}</span>
          <b>{{ metric.icon }}</b>
        </div>
        <strong>{{ metric.value }}</strong>
        <small :class="metric.tone">{{ metric.note }}</small>
      </article>
    </section>

    <section class="dashboard-grid">
      <article class="planning-card">
        <span class="pill danger">{{ planningCard.badge }}</span>
        <h2>{{ planningCard.title }}</h2>
        <p v-for="line in planningCard.description" :key="line" class="planning-line">{{ line }}</p>
        <router-link :to="planningCard.to" class="btn btn-primary">
          {{ planningCard.button }}
          <span>→</span>
        </router-link>
      </article>

      <aside class="task-panel">
        <div class="panel-title">
          <h2>오늘 할 일</h2>
          <span class="pill danger">{{ tasks.length }} Pending</span>
        </div>
        <div v-if="tasks.length" class="task-list">
          <router-link
            v-for="task in tasks"
            :key="task.title"
            :to="task.to"
            class="task-item"
            :class="{ urgent: task.urgent }"
          >
            <span class="task-icon">{{ task.icon }}</span>
            <div>
              <strong>{{ task.title }}</strong>
              <p>{{ task.description }}</p>
              <small v-if="task.due">{{ task.due }}</small>
            </div>
          </router-link>
        </div>
        <p v-else class="task-empty">지금 처리할 항목이 없습니다.</p>
        <router-link :to="taskListTarget" class="panel-link">전체 할 일 보기</router-link>
      </aside>
    </section>

    <!-- 좌우 항목 수가 달라도 높이를 맞춘다 -->
    <section class="content-grid two equal">
      <article class="panel">
        <div class="panel-title">
          <h2>{{ coursePanelTitle }}</h2>
          <router-link :to="coursePanelTarget">전체 보기</router-link>
        </div>
        <div v-if="loading" class="skeleton-list">
          <div v-for="i in 3" :key="i" class="skeleton-line"></div>
        </div>
        <p v-else-if="loadError" class="task-empty">교육 목록을 불러오지 못했습니다. 백엔드 상태를 확인해 주세요.</p>
        <p v-else-if="!previewCourses.length" class="task-empty">등록된 교육 프로그램이 없습니다.</p>
        <div v-else class="compact-list">
          <router-link
            v-for="course in previewCourses"
            :key="course.id"
            :to="`/courses/${course.id}`"
            class="compact-row"
          >
            <span class="program-token">{{ course.categoryShort }}</span>
            <div>
              <strong>{{ course.title }}</strong>
              <small>{{ course.providerName }} · {{ course.categoryLabel }}</small>
            </div>
          </router-link>
        </div>
      </article>

      <article class="panel">
        <div class="panel-title">
          <h2>{{ stepPanelTitle }}</h2>
        </div>
        <ol class="step-list">
          <li v-for="step in workflowSteps" :key="step.label" :class="{ done: step.done }">{{ step.label }}</li>
        </ol>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import { courseApi } from '@/api/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'
import { useProviderNames } from '@/composables/useProviderNames.js'
import { isProviderRole, normalizeCourse, unwrapListResponse, unwrapObjectResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const courses = ref([])
// 내 계약은 건수만 필요하다. 서버가 주는 요약(상태 필터·페이지와 무관한 전체 집계)을 그대로 쓴다.
const enrollmentSummary = ref({ active: 0, pending: 0, total: 0 })
const loading = ref(true)
const loadError = ref(false)
const { resolve: resolveProviderNames } = useProviderNames()

const displayName = computed(() => auth.user?.name || '담당자')
const isProvider = computed(() => isProviderRole(auth.user?.role))

// 공급자 화면의 지표·할 일·단계는 "본인이 등록한 프로그램"만 세어야 한다.
// 전체 카탈로그를 쓰면 남이 올린 강의가 본인 실적으로 잡힌다.
const myCourses = computed(() => (
  isProvider.value
    ? courses.value.filter(course => Number(course.instructorId) === Number(auth.user?.id))
    : courses.value
))

const previewCourses = computed(() => myCourses.value.slice(0, 4).map(normalizeCourse))
const activeCount = computed(() => enrollmentSummary.value.active)
const pendingCount = computed(() => enrollmentSummary.value.pending)
const enrollmentCount = computed(() => enrollmentSummary.value.total)

const dashboardTitle = computed(() => (
  isProvider.value ? `공급자 대시보드, ${displayName.value}님` : `${displayName.value}님, 반갑습니다.`
))
const dashboardDescription = computed(() => (
  isProvider.value
    ? '등록 프로그램, 카탈로그 노출, 기업 교육 요청 흐름을 한 화면에서 확인합니다.'
    : '기업 교육 기획, 추천, 계약 현황을 한 화면에서 확인합니다.'
))
const primaryAction = computed(() => (
  isProvider.value
    ? { to: '/courses/new', label: '프로그램 등록' }
    : { to: '/needs', label: '교육 기획 요청' }
))
const planningCard = computed(() => (
  isProvider.value
    ? {
        badge: 'Provider Program Setup',
        title: '기업 교육 프로그램 등록',
        description: ['교육 기간, 방식, 대상 직무, 가능 지역을 입력해',
                      'HRD 담당자가 비교할 수 있는 프로그램 정보를 구성합니다.'],
        button: '프로그램 등록하기',
        to: '/courses/new'
      }
    : {
        badge: 'Q3 Planning Cycle Open',
        title: '기업 교육 니즈 입력',
        description: ['사업계획과 현재 역량을 입력하면 키워드와 교육 분야를 기준으로',
                      '필요한 기업 교육 프로그램을 추천합니다.'],
        button: '교육 기획 시작',
        to: '/needs'
      }
))
// 할 일은 실제 상태에서 만든다. 해당 사항이 없으면 그 항목은 빼서 빈 목록이 되게 둔다.
const tasks = computed(() => {
  if (isProvider.value) {
    const list = []
    if (!myCourses.value.length) {
      list.push({ icon: 'CR', title: '첫 프로그램 등록', description: '아직 등록한 교육 프로그램이 없습니다.', due: '', to: '/courses/new', urgent: true })
    }
    const missingSchedule = myCourses.value.filter(c => !c.startDate && !c.durationDays).length
    if (missingSchedule) {
      list.push({ icon: 'PC', title: '일정 정보 미입력', description: `${missingSchedule}개 프로그램에 교육 기간·시작일이 없습니다.`, due: '', to: '/courses', urgent: false })
    }
    return list
  }

  const list = []
  if (pendingCount.value) {
    list.push({ icon: 'CT', title: '계약 신청 진행 중', description: `${pendingCount.value}건이 확정 대기 상태입니다.`, due: '', to: '/enrollments', urgent: true })
  }
  if (activeCount.value) {
    list.push({ icon: 'SV', title: '만족도 조사 대상', description: `확정된 교육 ${activeCount.value}건에 만족도를 남길 수 있습니다.`, due: '', to: '/surveys', urgent: false })
  }
  if (!enrollmentCount.value) {
    list.push({ icon: 'AI', title: '추천 교육 검토', description: '아직 신청한 교육이 없습니다. 추천 후보부터 확인해 보세요.', due: '', to: '/recommendations', urgent: false })
  }
  return list
})
const taskListTarget = computed(() => (isProvider.value ? '/courses' : '/enrollments'))
const coursePanelTitle = computed(() => (isProvider.value ? '카탈로그 등록 현황' : '추천 교육'))
const coursePanelTarget = computed(() => (isProvider.value ? '/courses' : '/recommendations'))
const stepPanelTitle = computed(() => (isProvider.value ? '공급자 운영 단계' : '교육 기획 단계'))
// 진행 표시는 실제 상태에서 판정한다.
const hasNeeds = computed(() => !!sessionStorage.getItem('hrd_needs_analysis'))
const workflowSteps = computed(() => (
  isProvider.value
    ? [
        { label: '프로그램 등록', done: myCourses.value.length > 0 },
        { label: '운영 조건 입력', done: myCourses.value.some(c => c.startDate || c.durationDays) },
        { label: '계약 요청 확인', done: false },
        { label: '교육 진행', done: false },
        { label: '만족도 개선', done: false }
      ]
    : [
        { label: 'Needs 분석', done: hasNeeds.value },
        { label: '과정 추천', done: hasNeeds.value },
        { label: '계약 신청', done: enrollmentCount.value > 0 },
        { label: '교육 확정', done: activeCount.value > 0 },
        { label: '만족도 조사', done: false }
      ]
))

// 지표는 실제 응답에서만 만든다. 추정치·목표치를 섞지 않는다.
const metrics = computed(() => {
  const categoryCount = new Set(courses.value.map(c => c.category).filter(Boolean)).size
  const myCategoryCount = new Set(myCourses.value.map(c => c.category).filter(Boolean)).size

  if (isProvider.value) {
    return [
      { label: '내 등록 프로그램', value: myCourses.value.length, note: `전체 카탈로그 ${courses.value.length}건 중`, icon: 'PC', tone: 'positive' },
      { label: '교육 분야', value: myCategoryCount, note: '내가 노출 중인 분야 수', icon: 'OP', tone: '' },
      { label: '일정 미입력', value: myCourses.value.filter(c => !c.startDate && !c.durationDays).length, note: '보완 필요', icon: 'CT', tone: 'danger-text' },
      { label: '만족도', value: '집계 예정', note: '만족도 API 배포 후 제공', icon: 'ST', tone: '' }
    ]
  }

  return [
    { label: '확정된 교육', value: activeCount.value, note: 'ACTIVE 상태', icon: 'TR', tone: 'positive' },
    { label: '계약/참여 신청 중', value: pendingCount.value, note: 'PENDING 상태', icon: 'CT', tone: pendingCount.value ? 'danger-text' : '' },
    { label: '카탈로그 프로그램', value: courses.value.length, note: `${categoryCount}개 분야`, icon: 'AI', tone: '' },
    { label: '만족도', value: '집계 예정', note: '만족도 API 배포 후 제공', icon: 'ST', tone: '' }
  ]
})

onMounted(async () => {
  const [courseRes, enrollRes] = await Promise.allSettled([
    courseApi.getAll(),
    // 목록은 쓰지 않고 요약만 쓰므로 한 건만 받는다.
    enrollmentApi.getMy({ page: 0, size: 1 })
  ])

  if (courseRes.status === 'fulfilled') {
    courses.value = await resolveProviderNames(unwrapListResponse(courseRes.value).map(normalizeCourse))
  } else {
    console.error('[HrdDashboard] 교육 목록 조회 실패:', courseRes.reason)
    loadError.value = true
  }

  if (enrollRes.status === 'fulfilled') {
    const summary = unwrapObjectResponse(enrollRes.value)?.summary
    if (summary) enrollmentSummary.value = summary
  } else {
    console.error('[HrdDashboard] 내 계약 조회 실패:', enrollRes.reason)
  }

  loading.value = false
})
</script>

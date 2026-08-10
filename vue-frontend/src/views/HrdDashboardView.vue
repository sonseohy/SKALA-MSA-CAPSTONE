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
        <p>{{ planningCard.description }}</p>
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
        <div class="task-list">
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
              <small>{{ task.due }}</small>
            </div>
          </router-link>
        </div>
        <router-link :to="taskListTarget" class="panel-link">전체 할 일 보기</router-link>
      </aside>
    </section>

    <section class="content-grid two">
      <article class="panel">
        <div class="panel-title">
          <h2>{{ coursePanelTitle }}</h2>
          <router-link :to="coursePanelTarget">전체 보기</router-link>
        </div>
        <div v-if="loading" class="skeleton-list">
          <div v-for="i in 3" :key="i" class="skeleton-line"></div>
        </div>
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
import { useAuthStore } from '@/store/auth.js'
import { isProviderRole, normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const courses = ref([])
const loading = ref(true)

const displayName = computed(() => auth.user?.name || 'Sarah')
const isProvider = computed(() => isProviderRole(auth.user?.role))
const previewCourses = computed(() => courses.value.slice(0, 4).map(normalizeCourse))

const dashboardTitle = computed(() => (
  isProvider.value ? `공급자 대시보드, ${displayName.value}` : `Good Morning, ${displayName.value}.`
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
        description: '교육 기간, 방식, 대상 직무, 가능 지역을 입력해 HRD 담당자가 비교할 수 있는 프로그램 정보를 구성합니다.',
        button: '프로그램 등록하기',
        to: '/courses/new'
      }
    : {
        badge: 'Q3 Planning Cycle Open',
        title: '기업 교육 니즈 입력',
        description: '사업계획과 현재 역량을 입력하면 키워드와 교육 분야를 기준으로 필요한 기업 교육 프로그램을 추천합니다.',
        button: '교육 기획 시작',
        to: '/needs'
      }
))
const tasks = computed(() => (
  isProvider.value
    ? [
        { icon: 'CR', title: '프로그램 정보 보완', description: '교육 기간과 대상 직무를 최신 기준으로 입력', due: '오늘 권장', to: '/courses/new', urgent: true },
        { icon: 'PC', title: '카탈로그 노출 확인', description: 'HRD 담당자에게 보이는 프로그램 카드 점검', due: '이번 주', to: '/courses', urgent: false },
        { icon: 'PV', title: '공급자 프로필 확인', description: '전문 분야와 가능 지역 정보 점검', due: '2일 후', to: '/providers/1', urgent: false }
      ]
    : [
        { icon: 'CT', title: '계약 신청 상태 확인', description: '신청한 기업 교육 계약 진행 상태 점검', due: '오늘 마감', to: '/enrollments', urgent: true },
        { icon: 'SV', title: '만족도 조사 마감 임박', description: '진행 중인 교육의 만족도 응답률 확인', due: '2일 후 마감', to: '/surveys', urgent: false },
        { icon: 'AI', title: '추천 교육 검토', description: '최근 입력한 교육 니즈 기반 추천 후보 확인', due: '이번 주', to: '/recommendations', urgent: false }
      ]
))
const taskListTarget = computed(() => (isProvider.value ? '/courses' : '/enrollments'))
const coursePanelTitle = computed(() => (isProvider.value ? '카탈로그 등록 현황' : '추천 교육'))
const coursePanelTarget = computed(() => (isProvider.value ? '/courses' : '/recommendations'))
const stepPanelTitle = computed(() => (isProvider.value ? '공급자 운영 단계' : '교육 기획 단계'))
const workflowSteps = computed(() => (
  isProvider.value
    ? [
        { label: '프로그램 등록', done: true },
        { label: '운영 조건 입력', done: true },
        { label: '계약 요청 확인', done: false },
        { label: '교육 진행', done: false },
        { label: '만족도 개선', done: false }
      ]
    : [
        { label: 'Needs 분석', done: true },
        { label: '과정 추천', done: false },
        { label: '강사 매칭', done: false },
        { label: '교육 개설', done: false },
        { label: '운영·결제', done: false }
      ]
))

const metrics = computed(() => (
  isProvider.value
    ? [
        { label: '등록 프로그램', value: courses.value.length || 1, note: '카탈로그 노출 기준', icon: 'PC', tone: 'positive' },
        { label: '계약 요청', value: '2', note: '확인 필요', icon: 'CT', tone: 'danger-text' },
        { label: '운영 가능 방식', value: '3', note: '온라인/오프라인/병행', icon: 'OP', tone: '' },
        { label: '평균 만족도', value: '4.8/5.0', note: '등록 교육 기준', icon: 'ST', tone: '' }
      ]
    : [
        { label: '진행 중인 교육', value: '12', note: '지난달 대비 +2', icon: 'TR', tone: 'positive' },
        { label: '계약 요청', value: '5', note: '2건 확인 필요', icon: 'CT', tone: 'danger-text' },
        { label: '추천 교육', value: Math.max(courses.value.length, 8), note: '최근 교육 니즈 기반', icon: 'AI', tone: '' },
        { label: '평균 만족도', value: '4.8/5.0', note: '진행 교육 전체 기준', icon: 'ST', tone: '' }
      ]
))

onMounted(async () => {
  try {
    const res = await courseApi.getAll()
    courses.value = unwrapListResponse(res)
  } finally {
    loading.value = false
  }
})
</script>

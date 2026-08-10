<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">HRD Dashboard</p>
        <h1>Good Morning, {{ displayName }}.</h1>
        <p>기업 교육 기획, 추천, 계약 현황을 한 화면에서 확인합니다.</p>
      </div>
      <router-link to="/needs" class="btn btn-primary">
        <span>+</span>
        교육 기획 요청
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
        <span class="pill danger">Q3 Planning Cycle Open</span>
        <h2>Input Enterprise Education Needs</h2>
        <p>
          사업계획과 현재 역량을 입력하면 키워드와 교육 분야를 기준으로 필요한
          기업 교육 프로그램을 추천합니다.
        </p>
        <router-link to="/needs" class="btn btn-primary">
          Start Planning Process
          <span>→</span>
        </router-link>
      </article>

      <aside class="task-panel">
        <div class="panel-title">
          <h2>오늘 할 일</h2>
          <span class="pill danger">3 Pending</span>
        </div>
        <div class="task-list">
          <div class="task-item urgent">
          <span class="task-icon">CT</span>
          <div>
              <strong>계약 승인 필요</strong>
              <p>Fast Lane 리더십 교육 모듈</p>
              <small>오늘 마감</small>
            </div>
          </div>
          <div class="task-item">
            <span class="task-icon">SV</span>
            <div>
              <strong>만족도 조사 마감 임박</strong>
              <p>Q2 만족도 조사 - 응답률 85%</p>
              <small>2일 후 마감</small>
            </div>
          </div>
        </div>
        <router-link to="/surveys" class="panel-link">전체 할 일 보기</router-link>
      </aside>
    </section>

    <section class="content-grid two">
      <article class="panel">
        <div class="panel-title">
          <h2>추천 교육</h2>
          <router-link to="/recommendations">전체 보기</router-link>
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
          <h2>교육 기획 단계</h2>
        </div>
        <ol class="step-list">
          <li class="done">Needs 분석</li>
          <li>과정 추천</li>
          <li>강사 매칭</li>
          <li>교육 개설</li>
          <li>운영·결제</li>
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
import { normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const courses = ref([])
const loading = ref(true)

const displayName = computed(() => auth.user?.name || 'Sarah')
const previewCourses = computed(() => courses.value.slice(0, 4).map(normalizeCourse))

const metrics = computed(() => [
  { label: '진행 중인 교육', value: '12', note: '지난달 대비 +2', icon: 'TR', tone: 'positive' },
  { label: '계약 요청', value: '5', note: '2건 확인 필요', icon: 'CT', tone: 'danger-text' },
  { label: '추천 교육', value: Math.max(courses.value.length, 8), note: '최근 교육 니즈 기반', icon: 'AI', tone: '' },
  { label: '평균 만족도', value: '4.8/5.0', note: '진행 교육 전체 기준', icon: 'ST', tone: '' }
])

onMounted(async () => {
  try {
    const res = await courseApi.getAll()
    courses.value = unwrapListResponse(res)
  } finally {
    loading.value = false
  }
})
</script>

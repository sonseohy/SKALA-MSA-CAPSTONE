<template>
  <HrdLayout>
    <div v-if="loading" class="empty-panel">프로그램 정보를 불러오는 중입니다.</div>

    <template v-else-if="course">
      <section class="detail-hero">
        <div>
          <span class="pill">{{ normalized.categoryLabel }}</span>
          <h1>{{ course.title }}</h1>
          <p>{{ course.description || '기업 역량 강화를 위한 실무 중심 교육 프로그램입니다.' }}</p>
        </div>
        <aside class="contract-card">
          <span>예상 교육비</span>
          <strong>{{ formatPrice(course.price) }}</strong>
          <button type="button" class="btn btn-primary" :disabled="isInstructor || submitting" @click="requestContract">
            {{ buttonText }}
          </button>
          <small>{{ helperText }}</small>
        </aside>
      </section>

      <section class="content-grid two">
        <article class="panel">
          <h2>교육 구성</h2>
          <div class="info-grid">
            <div><span>교육 분야</span><strong>{{ normalized.categoryLabel }}</strong></div>
            <div><span>교육 공급자</span><strong>{{ normalized.providerName }}</strong></div>
            <div><span>교육 일정</span><strong>{{ normalized.scheduleLabel }}</strong></div>
            <div><span>교육 기간</span><strong>{{ normalized.durationLabel }}</strong></div>
            <div><span>교육 방식</span><strong>{{ normalized.deliveryTypeLabel }}</strong></div>
            <div><span>교육 대상</span><strong>{{ course.targetAudience || '협의' }}</strong></div>
            <div><span>교육 지역</span><strong>{{ course.region || '협의' }}</strong></div>
            <div><span>난이도</span><strong>{{ normalized.difficultyLabel }}</strong></div>
            <div><span>신청 수</span><strong>{{ course.enrollmentCount ?? 0 }}건</strong></div>
            <div><span>상태</span><strong>{{ course.status || 'ACTIVE' }}</strong></div>
          </div>
          <ul class="curriculum-list">
            <li>기업 니즈 진단 및 사례 기반 개념 정리</li>
            <li>직무별 실습 과제와 적용 시나리오 설계</li>
            <li>교육 종료 후 만족도 조사와 개선 포인트 수집</li>
          </ul>
        </article>

        <article class="panel">
          <div class="panel-title">
            <h2>Provider Snapshot</h2>
            <router-link to="/providers/1">상세 보기</router-link>
          </div>
          <div class="provider-card mini">
            <div class="provider-avatar">{{ provider.name.charAt(0) }}</div>
            <div>
              <strong>{{ provider.name }}</strong>
              <p>{{ provider.specialty }} · {{ provider.experience }}</p>
              <span class="pill success">만족도 {{ provider.satisfaction }}</span>
            </div>
          </div>
        </article>
      </section>
    </template>

    <section v-else class="empty-panel">
      <h2>프로그램을 찾지 못했습니다.</h2>
      <router-link to="/courses" class="btn btn-primary">카탈로그로 이동</router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { courseApi } from '@/api/course.js'
import { enrollmentApi } from '@/api/enrollment.js'
import { useAuthStore } from '@/store/auth.js'
import { formatPrice, normalizeCourse, sampleProviders, unwrapObjectResponse } from '@/utils/hrd.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const course = ref(null)
const loading = ref(true)
const submitting = ref(false)
const status = ref('NONE')
const provider = sampleProviders[0]

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')
const normalized = computed(() => normalizeCourse(course.value))
const buttonText = computed(() => {
  if (isInstructor.value) return '공급자 계정은 신청 불가'
  if (status.value === 'ACTIVE') return '내 계약으로 이동'
  if (status.value === 'PENDING') return '신청 검토 중'
  return submitting.value ? '신청 중...' : '교육 계약 신청'
})
const helperText = computed(() => {
  if (isInstructor.value) return '교육 공급자는 본인 프로그램을 신청할 수 없습니다.'
  if (status.value === 'ACTIVE') return '이미 확정된 교육입니다.'
  if (status.value === 'PENDING') return '계약 신청이 접수되었습니다.'
  return '현재 실습용 payment-service가 계약 비용 처리를 시뮬레이션합니다.'
})

async function loadStatus() {
  if (!auth.user?.id || !course.value?.id || isInstructor.value) return
  try {
    const res = await enrollmentApi.getMyEnrollments()
    const items = Array.isArray(res.data?.data) ? res.data.data : []
    const matched = items.find(item => Number(item.courseId) === Number(course.value.id))
    status.value = matched?.status || 'NONE'
  } catch {
    status.value = 'NONE'
  }
}

async function requestContract() {
  if (status.value === 'ACTIVE') {
    router.push('/enrollments')
    return
  }
  if (!course.value?.id || status.value === 'PENDING') return

  submitting.value = true
  try {
    await enrollmentApi.enroll(course.value.id)
    status.value = 'PENDING'
    setTimeout(() => router.push('/enrollments'), 600)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await courseApi.getById(route.params.id)
    course.value = normalizeCourse(unwrapObjectResponse(res))
    await loadStatus()
  } finally {
    loading.value = false
  }
})
</script>

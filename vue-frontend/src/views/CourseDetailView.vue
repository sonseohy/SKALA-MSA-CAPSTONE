<template>
  <HrdLayout>
    <div v-if="loading" class="empty-panel">프로그램 정보를 불러오는 중입니다.</div>

    <template v-else-if="course">
      <section class="detail-hero">
        <div>
          <span class="pill">{{ normalized.categoryLabel }}</span>
          <h1>{{ course.title }}</h1>
          <p>{{ course.description || '등록된 프로그램 설명이 없습니다.' }}</p>
        </div>
        <aside class="contract-card">
          <span>예상 교육비</span>
          <strong>{{ formatPrice(course.price) }}</strong>
          <button type="button" class="btn btn-primary" :disabled="isInstructor || submitting" @click="requestContract">
            {{ buttonText }}
          </button>
          <small>{{ helperText }}</small>
          <div v-if="submitError" class="notice-box error">{{ submitError }}</div>
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
          <h3 class="sub-title">커리큘럼</h3>
          <ul v-if="curriculumItems.length" class="curriculum-list">
            <li v-for="(item, index) in curriculumItems" :key="index">{{ item }}</li>
          </ul>
          <p v-else class="muted-note">
            등록된 커리큘럼이 없습니다. 교육 공급자가 프로그램 설명에 커리큘럼을 입력하면 이곳에 표시됩니다.
          </p>
        </article>

        <article class="panel">
          <div class="panel-title">
            <h2>교육 공급자</h2>
            <router-link v-if="course.instructorId" :to="`/providers/${course.instructorId}`">상세 보기</router-link>
          </div>
          <div class="provider-card mini">
            <div class="provider-avatar" :style="{ background: avatarColor(normalized.providerName) }">{{ providerInitial }}</div>
            <div>
              <strong>{{ normalized.providerName }}</strong>
              <p>{{ normalized.categoryLabel }} · 공급자 ID {{ course.instructorId ?? '-' }}</p>
            </div>
          </div>
          <!-- 명세 03_api_spec.md:142 — 공급자 경력은 description 에 담긴다 -->
          <div v-if="providerCareer" class="info-grid single">
            <div><span>공급자 경력</span><strong>{{ providerCareer }}</strong></div>
          </div>
        </article>
      </section>
    </template>

    <section v-else class="empty-panel">
      <h2>{{ loadError ? '프로그램 정보를 불러오지 못했습니다.' : '프로그램을 찾지 못했습니다.' }}</h2>
      <p v-if="loadError">백엔드 서비스가 기동되어 있는지 확인한 뒤 다시 시도해 주세요.</p>
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
import { avatarColor, formatPrice, normalizeCourse, parseCourseDescription, unwrapObjectResponse } from '@/utils/hrd.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const course = ref(null)
const loading = ref(true)
const submitting = ref(false)
const status = ref('NONE')
const loadError = ref(false)
const submitError = ref('')

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')
const normalized = computed(() => normalizeCourse(course.value))
const providerInitial = computed(() => normalized.value?.providerName?.charAt(0)?.toUpperCase() || 'P')

// 명세 F-06·03_api_spec.md:142 상 커리큘럼과 공급자 경력은 description 에 담긴다.
// 없는 내용을 지어내지 않는다.
const parsedDescription = computed(() => parseCourseDescription(course.value?.description))
const curriculumItems = computed(() => parsedDescription.value.curriculum)
const providerCareer = computed(() => parsedDescription.value.providerCareer)
const buttonText = computed(() => {
  if (isInstructor.value) return '공급자 계정은 신청 불가'
  if (status.value === 'ACTIVE') return '내 계약으로 이동'
  if (status.value === 'PENDING') return '계약/참여 신청 중'
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

  submitError.value = ''
  submitting.value = true
  try {
    await enrollmentApi.enroll(course.value.id)
    status.value = 'PENDING'
    setTimeout(() => router.push('/enrollments'), 600)
  } catch (error) {
    // 실패를 삼키면 버튼만 원복되어 사용자가 원인을 알 수 없다.
    console.error('[CourseDetail] 계약 신청 실패:', error)
    submitError.value = error.response?.data?.message || '계약 신청에 실패했습니다. 잠시 후 다시 시도해 주세요.'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const res = await courseApi.getById(route.params.id)
    course.value = normalizeCourse(unwrapObjectResponse(res))
    await loadStatus()
  } catch (error) {
    console.error('[CourseDetail] 프로그램 조회 실패:', error)
    course.value = null
    loadError.value = true
  } finally {
    loading.value = false
  }
})
</script>

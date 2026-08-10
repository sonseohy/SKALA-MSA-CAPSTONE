<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">02 · Program Recommendation</p>
        <h1>추천 교육</h1>
        <p>수강 이력과 교육 니즈를 기준으로 recommend-service가 계산한 추천 후보입니다.</p>
      </div>
      <router-link to="/needs" class="btn btn-ghost">Needs 다시 입력</router-link>
    </section>

    <section class="insight-banner">
      <div>
        <span class="pill danger">{{ sourceLabel }}</span>
        <h2>{{ categoryLabel }} 중심의 추천 교육</h2>
        <p>{{ reasonText }}</p>
        <p v-if="budgetNote">{{ budgetNote }}</p>
      </div>
    </section>

    <div v-if="loading" class="program-grid">
      <div v-for="i in 6" :key="i" class="panel skeleton-card"></div>
    </div>
    <section v-else-if="programs.length" class="program-grid">
      <ProgramCard v-for="course in programs" :key="course.id" :course="course" />
    </section>
    <section v-else class="empty-panel">
      <h2>{{ emptyTitle }}</h2>
      <p>{{ emptyDescription }}</p>
      <router-link to="/courses" class="btn btn-primary">Program Catalog</router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import ProgramCard from '@/components/ProgramCard.vue'
import { courseApi } from '@/api/course.js'
import { recommendApi } from '@/api/recommend.js'
import { useAuthStore } from '@/store/auth.js'
import { categoryLabelMap, formatPrice, normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const loading = ref(true)
const programs = ref([])
const analysis = ref(null)
// 'recommend' = recommend-service 응답, 'category' = 니즈 카테고리 폴백, 'error' = 둘 다 실패
const source = ref('recommend')
const serviceMessage = ref('')
const basedOnCategory = ref(null)

const categoryLabel = computed(() => {
  const key = basedOnCategory.value || analysis.value?.category
  return categoryLabelMap[key] || analysis.value?.categoryLabel || '기업 교육 니즈'
})

const sourceLabel = computed(() => ({
  recommend: 'recommend-service 추천',
  category: '카테고리 기반 (추천 서비스 응답 없음)',
  error: '조회 실패'
}[source.value]))

const reasonText = computed(() => {
  if (source.value === 'recommend') {
    return serviceMessage.value || '수강 이력의 최빈 카테고리를 기준으로 미수강 교육을 추천했습니다.'
  }
  if (source.value === 'category') {
    return '추천 서비스에 연결하지 못해 입력한 교육 니즈의 카테고리로 대신 조회했습니다.'
  }
  return '추천 서비스와 카탈로그 모두 응답하지 않았습니다. 잠시 후 다시 시도해 주세요.'
})

// F-03 예산 필드의 MVP 처리 = "course price 비교". 입력했을 때만 표시한다.
const budgetNote = computed(() => {
  const budget = Number(analysis.value?.budget)
  if (!budget || !programs.value.length) return ''
  const over = programs.value.filter(course => Number(course.price) > budget).length
  return over
    ? `1인당 예산 ${formatPrice(budget)} 기준 · 예산을 넘는 과정 ${over}건`
    : `1인당 예산 ${formatPrice(budget)} 기준 · 예산을 넘는 과정 없음`
})

const emptyTitle = computed(() => (
  source.value === 'error' ? '추천 정보를 불러오지 못했습니다.' : '추천할 교육 프로그램이 아직 없습니다.'
))
const emptyDescription = computed(() => (
  source.value === 'error'
    ? '백엔드 서비스가 모두 기동되어 있는지 확인해 주세요.'
    : '수강 이력이 없으면 인기 교육이 우선 노출됩니다. 카탈로그에서 전체 교육을 확인해 보세요.'
))

async function loadByCategory() {
  const category = analysis.value?.category
  const res = category ? await courseApi.getByCategory(category) : await courseApi.getAll()
  programs.value = unwrapListResponse(res).map(normalizeCourse)
  source.value = 'category'
}

onMounted(async () => {
  try {
    analysis.value = JSON.parse(sessionStorage.getItem('hrd_needs_analysis') || 'null')
  } catch {
    analysis.value = null
  }

  const userId = auth.user?.id
  try {
    if (!userId) throw new Error('사용자 정보가 없습니다.')
    const res = await recommendApi.getForUser(userId)
    programs.value = unwrapListResponse(res).map(normalizeCourse)
    basedOnCategory.value = res.data?.basedOnCategory ?? null
    serviceMessage.value = res.data?.message ?? ''
    source.value = 'recommend'
  } catch (error) {
    console.error('[RecommendedPrograms] recommend-service 호출 실패:', error)
    try {
      await loadByCategory()
    } catch (fallbackError) {
      console.error('[RecommendedPrograms] 카테고리 폴백도 실패:', fallbackError)
      programs.value = []
      source.value = 'error'
    }
  } finally {
    loading.value = false
  }
})
</script>

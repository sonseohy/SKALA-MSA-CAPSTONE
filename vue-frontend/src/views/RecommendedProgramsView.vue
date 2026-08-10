<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">02 · Program Recommendation</p>
        <h1>추천 교육</h1>
        <p>입력한 사업 니즈와 기존 교육 카테고리를 매핑해 추천 후보를 보여줍니다.</p>
      </div>
      <router-link to="/needs" class="btn btn-ghost">Needs 다시 입력</router-link>
    </section>

    <section class="insight-banner">
      <div>
        <span class="pill danger">규칙 기반 MVP</span>
        <h2>{{ categoryLabel }} 중심의 추천 교육</h2>
        <p>{{ analysis?.reason || '수강 이력과 카테고리 기반 추천을 함께 활용할 수 있습니다.' }}</p>
      </div>
    </section>

    <div v-if="loading" class="program-grid">
      <div v-for="i in 6" :key="i" class="panel skeleton-card"></div>
    </div>
    <section v-else-if="programs.length" class="program-grid">
      <ProgramCard v-for="course in programs" :key="course.id" :course="course" />
    </section>
    <section v-else class="empty-panel">
      <h2>추천할 교육 프로그램이 아직 없습니다.</h2>
      <p>카탈로그에서 전체 교육을 확인하거나 교육 공급자 계정으로 프로그램을 등록해 주세요.</p>
      <router-link to="/courses" class="btn btn-primary">Program Catalog</router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import ProgramCard from '@/components/ProgramCard.vue'
import { courseApi } from '@/api/course.js'
import { categoryLabelMap, normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const loading = ref(true)
const programs = ref([])
const analysis = ref(null)

const categoryLabel = computed(() => analysis.value?.categoryLabel || '기업 교육 니즈')

onMounted(async () => {
  try {
    analysis.value = JSON.parse(sessionStorage.getItem('hrd_needs_analysis') || 'null')
    const category = analysis.value?.category
    const res = category ? await courseApi.getByCategory(category) : await courseApi.getAll()
    programs.value = unwrapListResponse(res).map(normalizeCourse)
  } catch (error) {
    console.error('[RecommendedPrograms] failed:', error)
    programs.value = []
  } finally {
    loading.value = false
  }
})
</script>

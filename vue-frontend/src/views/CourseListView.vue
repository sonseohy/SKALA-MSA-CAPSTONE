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

    <div v-if="loading" class="program-grid">
      <div v-for="i in 6" :key="i" class="panel skeleton-card"></div>
    </div>

    <section v-else-if="filteredCourses.length" class="program-grid">
      <ProgramCard v-for="course in filteredCourses" :key="course.id" :course="course" />
    </section>

    <section v-else class="empty-panel">
      <h2>조건에 맞는 프로그램이 없습니다.</h2>
      <p>필터를 변경하거나 공급자 계정으로 첫 교육 프로그램을 등록해 주세요.</p>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import ProgramCard from '@/components/ProgramCard.vue'
import { courseApi } from '@/api/course.js'
import { useAuthStore } from '@/store/auth.js'
import { categoryOptions, normalizeCourse, unwrapListResponse } from '@/utils/hrd.js'

const auth = useAuthStore()
const loading = ref(true)
const courses = ref([])
const selectedCategory = ref('ALL')

const isInstructor = computed(() => auth.user?.role === 'INSTRUCTOR')
const categories = [{ value: 'ALL', label: 'All Programs' }, ...categoryOptions]
const filteredCourses = computed(() => {
  if (selectedCategory.value === 'ALL') return courses.value
  return courses.value.filter(course => course.category === selectedCategory.value)
})

onMounted(async () => {
  try {
    const res = await courseApi.getAll()
    courses.value = unwrapListResponse(res).map(normalizeCourse)
  } finally {
    loading.value = false
  }
})
</script>

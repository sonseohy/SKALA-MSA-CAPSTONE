<template>
  <router-link :to="`/courses/${course.id}`" class="program-card">
    <div class="program-card-head">
      <div class="program-token">{{ normalized.categoryShort }}</div>
      <span class="program-category">{{ normalized.categoryLabel }}</span>
    </div>
    <h3>{{ course.title }}</h3>
    <p>{{ description }}</p>
    <div class="program-badges">
      <span>{{ normalized.scheduleLabel }}</span>
      <span>{{ normalized.durationLabel }}</span>
      <span>{{ normalized.deliveryTypeLabel }}</span>
      <span>{{ normalized.difficultyLabel }}</span>
    </div>
    <div class="program-meta">
      <span>{{ normalized.providerName }}</span>
      <strong>{{ formatPrice(course.price) }}</strong>
    </div>
    <div class="program-footer">
      <span>신청 {{ count }}건</span>
      <span>상세 보기</span>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { formatPrice, normalizeCourse } from '@/utils/hrd.js'

const props = defineProps({
  course: { type: Object, required: true }
})

const normalized = computed(() => normalizeCourse(props.course))
const description = computed(() => props.course.description || '기업 교육 목적에 맞춘 실무형 커리큘럼입니다.')
const count = computed(() => Number(props.course.enrollmentCount ?? props.course.enrollment_count ?? 0).toLocaleString())
</script>

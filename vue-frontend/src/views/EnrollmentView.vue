<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Contracts & Enrollments</p>
        <h1>{{ isTrainingPage ? '진행 중인 교육' : '내 교육/계약 현황' }}</h1>
        <p>{{ isTrainingPage ? '교육 확정 상태의 프로그램과 참여 현황을 확인합니다.' : '신청한 교육 계약과 참여 상태를 확인합니다.' }}</p>
      </div>
      <router-link to="/courses" class="btn btn-primary">새 교육 찾기</router-link>
    </section>

    <section v-if="loading" class="panel">계약 목록을 불러오는 중입니다.</section>

    <section v-else-if="visibleItems.length" class="table-panel">
      <table>
        <thead>
          <tr>
            <th>교육 프로그램</th>
            <th>교육 분야</th>
            <th>기간/방식</th>
            <th>비용</th>
            <th>상태</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in visibleItems" :key="item.id">
            <td>
              <router-link :to="`/courses/${item.courseId}`" class="table-title-link">
                {{ item.course?.title || `Program #${item.courseId}` }}
              </router-link>
              <small>Contract #{{ item.id }}</small>
            </td>
            <td>{{ item.course?.category || '-' }}</td>
            <td>
              {{ formatDuration(item.course?.durationDays) }}
              <small>{{ deliveryTypeLabel(item.course?.deliveryType) }}</small>
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

    <section v-else class="empty-panel">
      <h2>{{ isTrainingPage ? '진행 중인 교육이 없습니다.' : '아직 신청한 교육이 없습니다.' }}</h2>
      <p>{{ isTrainingPage ? '교육 계약이 확정되면 이곳에서 진행 중인 교육으로 확인할 수 있습니다.' : '카탈로그에서 기업 니즈에 맞는 교육을 찾아 계약을 신청해 보세요.' }}</p>
      <router-link to="/courses" class="btn btn-primary">Program Catalog</router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { enrollmentApi } from '@/api/enrollment.js'
import { deliveryTypeLabel, formatDuration, formatPrice, statusLabel } from '@/utils/hrd.js'

const loading = ref(true)
const items = ref([])
const route = useRoute()

const isTrainingPage = computed(() => route.path === '/trainings')
const visibleItems = computed(() => {
  if (!isTrainingPage.value) return items.value
  return items.value.filter(item => item.status === 'ACTIVE')
})

onMounted(async () => {
  try {
    const res = await enrollmentApi.getMyEnrollments()
    items.value = Array.isArray(res.data?.data) ? res.data.data : []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">My Page</p>
        <h1>{{ auth.user?.name || '사용자' }}님의 워크스페이스</h1>
        <p>계정 정보와 빠른 이동 메뉴를 확인합니다.</p>
      </div>
    </section>

    <section class="profile-panel">
      <div class="profile-avatar-lg">{{ initial }}</div>
      <div>
        <h2>{{ auth.user?.name || '사용자' }}</h2>
        <p>{{ auth.user?.email || '-' }}</p>
        <span class="pill">{{ roleLabel(auth.user?.role) }}</span>
      </div>
    </section>

    <section class="content-grid three">
      <router-link v-for="item in quickLinks" :key="item.to" :to="item.to" class="panel quick-card">
        <span>{{ item.code }}</span>
        <h2>{{ item.title }}</h2>
        <p>{{ item.desc }}</p>
      </router-link>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import { useAuthStore } from '@/store/auth.js'
import { roleLabel } from '@/utils/hrd.js'

const auth = useAuthStore()
const initial = computed(() => auth.user?.name?.charAt(0)?.toUpperCase() || 'U')

const quickLinks = [
  { to: '/needs', code: 'EN', title: '교육 니즈 입력', desc: '사업계획과 역량 Gap을 분석합니다.' },
  { to: '/enrollments', code: 'CT', title: '내 교육/계약', desc: '신청한 교육과 계약 상태를 확인합니다.' },
  { to: '/surveys', code: 'SV', title: '만족도 조사', desc: '교육 종료 후 만족도와 활용도를 기록합니다.' }
]
</script>

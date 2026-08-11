<template>
  <div class="hrd-shell">
    <aside class="hrd-sidebar">
      <router-link to="/hrd" class="hrd-brand" aria-label="HRD Dashboard">
        <div class="hrd-brand-mark">SK</div>
        <div>
          <strong>Enterprise HRD</strong>
          <span>Management Portal</span>
        </div>
      </router-link>

      <nav class="hrd-nav" aria-label="Primary">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="hrd-nav-item"
          :class="{ active: isActive(item) }"
        >
          <span class="hrd-nav-icon">{{ item.icon }}</span>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

    </aside>

    <div class="hrd-main-wrap">
      <header class="hrd-topbar">
        <router-link to="/hrd" class="hrd-top-title">HRD Integrated Platform</router-link>
        <div class="hrd-top-actions">
          <span class="hrd-role">{{ currentRole }}</span>
          <span class="hrd-avatar" :style="{ background: avatarColor(auth.user?.name) }" :title="auth.user?.name || currentRole">
            {{ initial }}
          </span>
          <button type="button" class="hrd-logout" @click="handleLogout">로그아웃</button>
        </div>
      </header>

      <main class="hrd-main">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { avatarColor, isProviderRole, roleLabel } from '@/utils/hrd.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const hrdNavItems = [
  { to: '/hrd', label: '대시보드', icon: 'DB', match: ['/hrd'] },
  { to: '/needs', label: '교육 니즈 입력', icon: 'EN', match: ['/needs'] },
  { to: '/recommendations', label: '추천 교육', icon: 'AI', match: ['/recommendations'] },
  { to: '/courses', label: '교육 카탈로그', icon: 'PC', match: ['/courses'] },
  // 명세 F-05 목적 "교육 프로그램과 교육 공급자를 비교한다".
  // 특정 공급자를 고정으로 가리키지 않고 등록된 공급자 목록으로 보낸다.
  { to: '/providers', label: '교육 공급자', icon: 'PV', match: ['/providers'] },
  { to: '/trainings', label: '진행 중인 교육', icon: 'TR', match: ['/trainings'] },
  { to: '/enrollments', label: '계약/신청', icon: 'CT', match: ['/enrollments'] },
  { to: '/surveys', label: '만족도 조사', icon: 'SV', match: ['/surveys'] }
]

const providerNavItems = [
  { to: '/hrd', label: '공급자 대시보드', icon: 'DB', match: ['/hrd'] },
  { to: '/courses/new', label: '프로그램 등록', icon: 'CR', match: ['/courses/new'] },
  { to: '/courses', label: '교육 카탈로그', icon: 'PC', match: ['/courses'] },
  { to: 'PROVIDER_SELF', label: '공급자 프로필', icon: 'PV', match: ['/providers'] }
]

const isProvider = computed(() => isProviderRole(auth.user?.role))
const navItems = computed(() => {
  const items = isProvider.value ? providerNavItems : hrdNavItems
  // 공급자 프로필은 로그인한 사용자 자신을 가리켜야 한다.
  return items.map(item => (
    item.to === 'PROVIDER_SELF'
      ? { ...item, to: auth.user?.id ? `/providers/${auth.user.id}` : '/courses' }
      : item
  ))
})
const currentRole = computed(() => roleLabel(auth.user?.role))
const initial = computed(() => auth.user?.name?.charAt(0)?.toUpperCase() || 'U')

function isActive(item) {
  if (route.path === '/courses/new') {
    return item.to === '/courses/new'
  }
  return item.match.some(prefix => route.path === prefix || route.path.startsWith(`${prefix}/`))
}

function handleLogout() {
  auth.logout(false)
  router.push('/login')
}
</script>

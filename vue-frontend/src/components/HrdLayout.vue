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

      <div class="hrd-sidebar-footer">
        <router-link to="/mypage" class="hrd-nav-item" :class="{ active: $route.path === '/mypage' }">
          <span class="hrd-nav-icon">MP</span>
          <span>My Page</span>
        </router-link>
      </div>
    </aside>

    <div class="hrd-main-wrap">
      <header class="hrd-topbar">
        <router-link to="/hrd" class="hrd-top-title">HRD Integrated Platform</router-link>
        <div class="hrd-top-actions">
          <label class="hrd-search">
            <span>Search</span>
            <input type="search" placeholder="Search programs, providers..." />
          </label>
          <span class="hrd-role">{{ currentRole }}</span>
          <button type="button" class="hrd-icon-btn" aria-label="Notifications">!</button>
          <button type="button" class="hrd-icon-btn" aria-label="Help">?</button>
          <router-link to="/mypage" class="hrd-avatar" :title="auth.user?.name || 'My Page'">
            {{ initial }}
          </router-link>
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
import { roleLabel } from '@/utils/hrd.js'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { to: '/hrd', label: 'Dashboard', icon: 'DB', match: ['/hrd'] },
  { to: '/needs', label: 'Education Needs', icon: 'EN', match: ['/needs'] },
  { to: '/recommendations', label: '추천 교육', icon: 'AI', match: ['/recommendations'] },
  { to: '/courses', label: 'Program Catalog', icon: 'PC', match: ['/courses'] },
  { to: '/providers/1', label: 'Providers', icon: 'PV', match: ['/providers'] },
  { to: '/trainings', label: '진행 중인 교육', icon: 'TR', match: ['/trainings'] },
  { to: '/enrollments', label: '계약/신청', icon: 'CT', match: ['/enrollments'] },
  { to: '/surveys', label: 'Surveys', icon: 'SV', match: ['/surveys'] }
]

const currentRole = computed(() => roleLabel(auth.user?.role))
const initial = computed(() => auth.user?.name?.charAt(0)?.toUpperCase() || 'U')

function isActive(item) {
  return item.match.some(prefix => route.path === prefix || route.path.startsWith(`${prefix}/`))
}

function handleLogout() {
  auth.logout(false)
  router.push('/login')
}
</script>

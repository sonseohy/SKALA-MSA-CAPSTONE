import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'

const routes = [
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/LandingView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/callback',
    name: 'Callback',
    component: () => import('@/views/CallbackView.vue')
  },
  {
    path: '/hrd',
    name: 'HrdDashboard',
    component: () => import('@/views/HrdDashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/needs',
    name: 'EducationNeeds',
    component: () => import('@/views/EducationNeedsView.vue'),
    meta: { requiresAuth: true, hrdOnly: true }
  },
  {
    path: '/recommendations',
    name: 'RecommendedPrograms',
    component: () => import('@/views/RecommendedProgramsView.vue'),
    meta: { requiresAuth: true, hrdOnly: true }
  },
  {
    path: '/courses',
    name: 'CourseList',
    component: () => import('@/views/CourseListView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/courses/new',
    name: 'CourseCreate',
    component: () => import('@/views/CourseCreateView.vue'),
    meta: { requiresAuth: true, instructorOnly: true }
  },
  {
    path: '/courses/:id(\\d+)',
    name: 'CourseDetail',
    component: () => import('@/views/CourseDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/providers/:id',
    name: 'ProviderDetail',
    component: () => import('@/views/ProviderDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/enrollments',
    name: 'Enrollment',
    component: () => import('@/views/EnrollmentView.vue'),
    meta: { requiresAuth: true, hrdOnly: true }
  },
  {
    path: '/trainings',
    name: 'Trainings',
    component: () => import('@/views/EnrollmentView.vue'),
    meta: { requiresAuth: true, hrdOnly: true }
  },
  {
    path: '/surveys',
    name: 'Survey',
    component: () => import('@/views/SurveyView.vue'),
    meta: { requiresAuth: true, hrdOnly: true }
  },
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('@/views/MyPageView.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'Login' }
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return { name: 'HrdDashboard' }
  }

  if (to.meta.instructorOnly && auth.user?.role !== 'INSTRUCTOR') {
    return { name: 'CourseList' }
  }

  if (to.meta.hrdOnly && auth.user?.role === 'INSTRUCTOR') {
    return { name: 'HrdDashboard' }
  }
})

export default router

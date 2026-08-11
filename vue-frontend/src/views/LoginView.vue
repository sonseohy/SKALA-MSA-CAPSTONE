<template>
  <div class="login-page">
    <section class="login-story">
      <router-link to="/" class="public-brand">
        <span class="brand-mark">SK</span>
        <span>LearnNexus HRD</span>
      </router-link>

      <div class="login-story-body">
        <span class="pill danger">Enterprise HRD</span>
        <!-- 「통합 워크스페이스」가 잘려 내려가지 않도록 줄을 직접 나눈다 -->
        <h1>기업 교육 운영을 위한<br />통합 워크스페이스</h1>
        <p>HRD 담당자, 교육 공급자, 임직원이 같은 교육 흐름 안에서 협업합니다.</p>

        <ul class="login-points">
          <li v-for="point in points" :key="point.title">
            <span class="login-point-mark">{{ point.mark }}</span>
            <div>
              <strong>{{ point.title }}</strong>
              <small>{{ point.body }}</small>
            </div>
          </li>
        </ul>
      </div>

      <p class="login-story-foot">SK AX · SKALA MSA Capstone</p>
    </section>

    <section class="login-panel">
      <div class="auth-card">
        <router-link to="/" class="text-link auth-back">← 홈으로</router-link>

        <div v-if="!showRegister" class="auth-box">
          <div>
            <h2>로그인</h2>
            <p>OAuth2 인증 서버로 이동해 로그인한 뒤 워크스페이스로 돌아옵니다.</p>
          </div>

          <p v-if="expired" class="notice-box error">로그인 유효 시간이 지나 자동으로 로그아웃되었습니다. 다시 로그인해 주세요.</p>
          <p v-else-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</p>

          <button type="button" class="btn btn-primary full" @click="handleOAuth">인증 서버로 이동해 로그인</button>

          <p class="auth-switch">계정이 없으신가요? <button type="button" @click="openRegister">회원가입</button></p>
        </div>

        <form v-else class="auth-box" @submit.prevent="handleRegister">
          <div>
            <h2>회원가입</h2>
            <p>가입한 계정으로 곧바로 로그인할 수 있습니다.</p>
          </div>

          <label for="regName">
            이름
            <input id="regName" v-model.trim="registerForm.name" required placeholder="홍길동" autocomplete="name" />
          </label>

          <label for="regEmail">
            이메일
            <input
              id="regEmail"
              v-model.trim="registerForm.email"
              type="email"
              required
              placeholder="user@example.com"
              autocomplete="email"
            />
          </label>

          <label for="regPassword">
            비밀번호
            <input
              id="regPassword"
              v-model="registerForm.password"
              type="password"
              required
              minlength="8"
              placeholder="8자 이상"
              autocomplete="new-password"
            />
            <small class="field-help">8자 이상 입력해 주세요.</small>
          </label>

          <label for="regRole">
            역할
            <select id="regRole" v-model="registerForm.role">
              <option value="STUDENT">기업 HRD 담당자 / 임직원</option>
              <option value="INSTRUCTOR">교육 공급자</option>
            </select>
            <small class="field-help">{{ roleHelp }}</small>
          </label>

          <p v-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</p>

          <button type="submit" class="btn btn-primary full" :disabled="loading">
            {{ loading ? '가입 중...' : '회원가입' }}
          </button>

          <p class="auth-switch">이미 계정이 있으신가요? <button type="button" @click="showRegister = false">로그인</button></p>
        </form>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'

const auth = useAuthStore()
const route = useRoute()
const showRegister = ref(false)
const loading = ref(false)
const message = ref('')
const hasError = ref(false)

// api/index.js 가 401 을 받으면 expired=1 을 붙여 이 화면으로 보낸다.
const expired = computed(() => route.query.expired === '1' && !message.value)

const points = [
  { mark: '01', title: '사업계획에서 교육 니즈로', body: '입력한 사업 방향과 역량에서 필요한 교육 분야를 도출합니다.' },
  { mark: '02', title: '프로그램과 공급자 비교', body: '일정·방식·지역·교육비를 나란히 놓고 검토합니다.' },
  { mark: '03', title: '계약부터 만족도까지', body: '신청 상태와 교육 결과를 한 화면에서 관리합니다.' }
]

const registerForm = reactive({
  name: '',
  email: '',
  password: '',
  role: 'STUDENT'
})

const roleHelp = computed(() => (
  registerForm.role === 'INSTRUCTOR'
    ? '교육 프로그램을 등록하고 공급자 프로필을 노출합니다.'
    : '교육 니즈를 입력하고 프로그램을 신청·관리합니다.'
))

function handleOAuth() {
  auth.redirectToLogin()
}

function openRegister() {
  message.value = ''
  hasError.value = false
  showRegister.value = true
}

async function handleRegister() {
  loading.value = true
  message.value = ''
  hasError.value = false
  try {
    await authApi.register(registerForm)
    message.value = '회원가입이 완료되었습니다. 로그인해 주세요.'
    showRegister.value = false
  } catch (error) {
    hasError.value = true
    message.value = error.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

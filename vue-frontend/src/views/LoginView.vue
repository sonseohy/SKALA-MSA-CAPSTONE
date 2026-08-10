<template>
  <div class="login-page">
    <section class="login-story">
      <router-link to="/" class="public-brand">
        <span class="brand-mark">SK</span>
        <span>LearnNexus HRD</span>
      </router-link>
      <div>
        <span class="pill danger">Enterprise HRD</span>
        <h1>기업 교육 운영을 위한 통합 워크스페이스</h1>
        <p>HRD 담당자, 교육 공급자, 임직원이 같은 교육 흐름 안에서 협업합니다.</p>
      </div>
    </section>

    <section class="login-panel">
      <router-link to="/" class="text-link">← 홈으로</router-link>
      <div v-if="!showRegister" class="auth-box">
        <h2>로그인</h2>
        <p>OAuth2 인증 서버를 통해 HRD 워크스페이스에 접속합니다.</p>
        <button class="btn btn-primary full" @click="handleOAuth">로그인</button>
        <p class="auth-switch">계정이 없으신가요? <button @click="showRegister = true">회원가입</button></p>
      </div>

      <form v-else class="auth-box" @submit.prevent="handleRegister">
        <h2>회원가입</h2>
        <label>이름<input v-model="registerForm.name" required placeholder="홍길동" /></label>
        <label>이메일<input v-model="registerForm.email" type="email" required placeholder="user@example.com" /></label>
        <label>비밀번호<input v-model="registerForm.password" type="password" required placeholder="8자 이상" /></label>
        <label>
          역할
          <select v-model="registerForm.role">
            <option value="STUDENT">기업 HRD 담당자 / 임직원</option>
            <option value="INSTRUCTOR">교육 공급자</option>
          </select>
        </label>
        <p v-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</p>
        <button class="btn btn-primary full" :disabled="loading">{{ loading ? '가입 중...' : '회원가입' }}</button>
        <p class="auth-switch">이미 계정이 있으신가요? <button type="button" @click="showRegister = false">로그인</button></p>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useAuthStore } from '@/store/auth.js'
import { authApi } from '@/api/auth.js'

const auth = useAuthStore()
const showRegister = ref(false)
const loading = ref(false)
const message = ref('')
const hasError = ref(false)

const registerForm = reactive({
  name: '',
  email: '',
  password: '',
  role: 'STUDENT'
})

function handleOAuth() {
  auth.redirectToLogin()
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

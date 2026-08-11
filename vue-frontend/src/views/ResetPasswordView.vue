<template>
  <div class="login-panel reset-page">
    <div class="auth-card">
      <router-link to="/login" class="text-link auth-back">← 로그인으로</router-link>

      <div class="auth-box">
        <div>
          <h2>비밀번호 재설정</h2>
          <p>새 비밀번호를 입력해 주세요.</p>
        </div>

        <p v-if="!token" class="notice-box error">
          재설정 링크가 올바르지 않습니다. 비밀번호 찾기를 다시 요청해 주세요.
        </p>

        <template v-else-if="!done">
          <form class="auth-box" @submit.prevent="handleSubmit">
            <label for="newPassword">
              새 비밀번호
              <input
                id="newPassword"
                v-model="newPassword"
                type="password"
                required
                minlength="8"
                placeholder="8자 이상"
                autocomplete="new-password"
              />
              <small class="field-help">8자 이상 입력해 주세요.</small>
            </label>

            <label for="confirmPassword">
              비밀번호 확인
              <input
                id="confirmPassword"
                v-model="confirmPassword"
                type="password"
                required
                minlength="8"
                placeholder="다시 입력해 주세요"
                autocomplete="new-password"
              />
            </label>

            <p v-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</p>

            <button type="submit" class="btn btn-primary full" :disabled="loading">
              {{ loading ? '변경 중...' : '비밀번호 변경' }}
            </button>
          </form>
        </template>

        <template v-else>
          <p class="notice-box">비밀번호가 변경되었습니다. 로그인해 주세요.</p>
          <router-link to="/login" class="text-link">로그인하러 가기</router-link>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { passwordApi } from '@/api/auth.js'

const route = useRoute()
// 이메일 링크로 진입하므로 쿼리스트링에서 토큰을 읽는다.
const token = route.query.token

const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const message = ref('')
const hasError = ref(false)
const done = ref(false)

async function handleSubmit() {
  if (newPassword.value !== confirmPassword.value) {
    hasError.value = true
    message.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  loading.value = true
  message.value = ''
  hasError.value = false
  try {
    await passwordApi.resetConfirm(token, newPassword.value)
    done.value = true
  } catch (error) {
    hasError.value = true
    message.value = error.response?.data?.message || '비밀번호 변경에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
}
</style>

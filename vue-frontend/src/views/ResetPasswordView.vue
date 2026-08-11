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
                placeholder="8자 이상, 대문자·특수문자 포함"
                autocomplete="new-password"
              />
              <ul class="pw-rules" aria-label="비밀번호 조건">
                <li v-for="rule in passwordCheck.rules" :key="rule.key" :class="{ ok: rule.passed }">
                  <span class="pw-mark" aria-hidden="true">{{ rule.passed ? '✓' : '○' }}</span>{{ rule.label }}
                </li>
              </ul>
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

            <button type="submit" class="btn btn-primary full" :disabled="loading || !passwordCheck.valid">
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
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { passwordApi } from '@/api/auth.js'
import { checkPassword } from '@/utils/password.js'

const route = useRoute()
// 이메일 링크로 진입하므로 쿼리스트링에서 토큰을 읽는다.
const token = route.query.token

const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const message = ref('')
const hasError = ref(false)
const done = ref(false)

// 입력마다 정책 충족 여부를 재계산해 체크리스트와 제출 버튼 활성화에 사용한다.
const passwordCheck = computed(() => checkPassword(newPassword.value))

async function handleSubmit() {
  // 버튼 비활성화만으로는 우회될 수 있으므로 서버 호출 전 정책을 한 번 더 확인한다.
  if (!passwordCheck.value.valid) {
    hasError.value = true
    message.value = '비밀번호 조건을 모두 충족해 주세요.'
    return
  }
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
/* 비밀번호 정책 체크리스트 — 미충족은 회색 원, 충족은 초록 체크로 즉시 피드백한다. */
.pw-rules {
  list-style: none;
  margin: 0.5rem 0 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem 0.9rem;
  font-size: 0.8rem;
  color: #6b7280;
}
.pw-rules li {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}
.pw-rules li.ok {
  color: #16a34a;
}
.pw-mark {
  font-weight: 700;
}
</style>

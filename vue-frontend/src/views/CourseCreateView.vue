<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Provider Management</p>
        <h1>기업 교육 프로그램 등록</h1>
        <p>교육 공급자가 제공 가능한 커리큘럼과 예상 비용을 등록합니다.</p>
      </div>
    </section>

    <form class="panel needs-form" @submit.prevent="handleSubmit">
      <div class="form-grid">
        <div class="form-row">
          <label for="title">프로그램명</label>
          <input id="title" v-model.trim="form.title" placeholder="생성형 AI 업무 활용 교육" />
        </div>
        <div class="form-row">
          <label for="category">교육 분야</label>
          <select id="category" v-model="form.category">
            <option disabled value="">교육 분야 선택</option>
            <option v-for="cat in categoryOptions" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="price">예상 교육비</label>
          <input id="price" v-model.number="form.price" type="number" min="0" step="1000" placeholder="99000" />
        </div>
        <div class="form-row">
          <label for="delivery">교육 방식</label>
          <input id="delivery" v-model.trim="extra.delivery" placeholder="온라인/오프라인 병행" />
        </div>
      </div>

      <div class="form-row full">
        <label for="description">커리큘럼/공급자 소개</label>
        <textarea
          id="description"
          v-model.trim="form.description"
          rows="8"
          placeholder="교육 대상, 주요 경력, 기업교육 이력, 커리큘럼, 가능 지역을 함께 입력하세요."
        />
      </div>

      <div v-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary" :disabled="submitting">{{ submitting ? '등록 중...' : '프로그램 등록' }}</button>
        <router-link to="/courses" class="btn btn-ghost">취소</router-link>
      </div>
    </form>
  </HrdLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { courseApi } from '@/api/course.js'
import { categoryOptions } from '@/utils/hrd.js'

const router = useRouter()
const submitting = ref(false)
const message = ref('')
const hasError = ref(false)

const form = reactive({
  title: '',
  description: '',
  category: '',
  price: null
})

const extra = reactive({ delivery: '' })

async function handleSubmit() {
  message.value = ''
  hasError.value = false
  if (!form.title || !form.category || form.price === null) {
    message.value = '프로그램명, 교육 분야, 예상 교육비를 입력해 주세요.'
    hasError.value = true
    return
  }

  submitting.value = true
  try {
    const description = [form.description, extra.delivery && `교육 방식: ${extra.delivery}`].filter(Boolean).join('\n')
    await courseApi.create({ ...form, description, price: Number(form.price) })
    message.value = '교육 프로그램이 등록되었습니다.'
    setTimeout(() => router.push('/courses'), 600)
  } catch (error) {
    message.value = error.response?.data?.message || '등록에 실패했습니다.'
    hasError.value = true
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Survey & Satisfaction</p>
        <h1>교육 만족도 조사</h1>
        <p>MVP에서는 간단한 만족도 입력과 요약 화면을 먼저 제공합니다.</p>
      </div>
    </section>

    <section class="content-grid two">
      <form class="panel survey-form" @submit.prevent="submitted = true">
        <h2>만족도 작성</h2>
        <div v-for="field in fields" :key="field.key" class="score-row">
          <span>{{ field.label }}</span>
          <div class="score-options">
            <button
              v-for="score in 5"
              :key="score"
              type="button"
              :class="{ active: form[field.key] === score }"
              @click="form[field.key] = score"
            >
              {{ score }}
            </button>
          </div>
        </div>
        <label class="form-row full">
          <span>후기</span>
          <textarea v-model="form.comment" rows="5" placeholder="업무 적용에 도움이 된 점을 적어주세요." />
        </label>
        <button class="btn btn-primary" type="submit">만족도 제출</button>
        <p v-if="submitted" class="notice-box">만족도 조사가 저장된 것으로 시연합니다. 백엔드 Survey API는 Sprint 2 후보입니다.</p>
      </form>

      <article class="panel">
        <h2>HRD Summary</h2>
        <div class="metric-grid single">
          <div class="metric-card"><span>응답 수</span><strong>28</strong><small>30명 중 93%</small></div>
          <div class="metric-card"><span>평균 만족도</span><strong>4.6</strong><small>5점 만점</small></div>
        </div>
        <ul class="curriculum-list">
          <li>실습 중심 교육에 대한 만족도가 높습니다.</li>
          <li>난이도는 실무 과정 기준 적정 수준입니다.</li>
          <li>향후 AI 분석 리포트로 확장 예정입니다.</li>
        </ul>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'

const submitted = ref(false)
const form = reactive({
  educationScore: 5,
  instructorScore: 5,
  usefulnessScore: 4,
  difficultyScore: 3,
  comment: ''
})

const fields = [
  { key: 'educationScore', label: '교육 만족도' },
  { key: 'instructorScore', label: '강사 만족도' },
  { key: 'usefulnessScore', label: '업무 활용도' },
  { key: 'difficultyScore', label: '난이도 적절성' }
]
</script>

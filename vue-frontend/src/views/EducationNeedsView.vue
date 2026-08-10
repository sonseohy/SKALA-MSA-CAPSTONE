<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">01 · Needs & Gap</p>
        <h1>사업계획과 교육 조건 입력</h1>
        <p>외부 AI 없이도 키워드와 역량 사전을 이용해 교육 분야를 추천합니다.</p>
      </div>
      <span class="pill success">ACTIVE</span>
    </section>

    <section class="workflow-tabs">
      <span class="active">1 Needs 분석</span>
      <span>2 과정 추천</span>
      <span>3 강사 매칭</span>
      <span>4 교육 개설</span>
      <span>5 운영·결제</span>
    </section>

    <form class="panel needs-form" @submit.prevent="analyzeNeeds">
      <div class="form-row full">
        <label for="businessPlan">기업 사업계획서 내용</label>
        <textarea
          id="businessPlan"
          v-model="form.businessPlan"
          rows="7"
          placeholder="예: 2027년 생성형 AI 기반 고객지원 자동화와 클라우드 네이티브 전환을 추진한다. 개발 조직의 AI 활용, 데이터 보안, Kubernetes 운영 역량을 강화한다."
        />
        <small>PDF 한글 문서는 텍스트를 복사해 넣거나, TXT/MD 파일을 불러온 것처럼 시연할 수 있습니다.</small>
      </div>

      <div class="form-grid">
        <div class="form-row">
          <label for="targetJob">교육 대상 직무</label>
          <input id="targetJob" v-model="form.targetJob" placeholder="백엔드 개발자, DevOps 엔지니어" />
        </div>
        <div class="form-row">
          <label for="currentSkills">현재 보유 역량</label>
          <input id="currentSkills" v-model="form.currentSkills" placeholder="Java, SQL, Linux" />
        </div>
        <div class="form-row">
          <label for="desiredSkills">희망 역량</label>
          <input id="desiredSkills" v-model="form.desiredSkills" placeholder="생성형 AI, Kubernetes, 보안" />
        </div>
        <div class="form-row">
          <label for="difficulty">희망 난이도</label>
          <select id="difficulty" v-model="form.difficulty">
            <option value="AUTO">자동 선택</option>
            <option value="BASIC">입문</option>
            <option value="INTERMEDIATE">실무</option>
            <option value="ADVANCED">심화</option>
          </select>
        </div>
      </div>

      <div class="analysis-box" v-if="analysis">
        <div>
          <span class="pill">추천 분야</span>
          <strong>{{ analysis.categoryLabel }}</strong>
          <p>{{ analysis.reason }}</p>
        </div>
        <router-link to="/recommendations" class="btn btn-primary">추천 과정 보기</router-link>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn btn-primary">Needs 분석 실행</button>
        <router-link to="/courses" class="btn btn-ghost">카탈로그 먼저 보기</router-link>
      </div>
    </form>
  </HrdLayout>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { categoryLabelMap, inferCategoryFromNeeds } from '@/utils/hrd.js'

const router = useRouter()
const analysis = ref(null)

const form = reactive({
  businessPlan: '',
  targetJob: '',
  currentSkills: '',
  desiredSkills: '',
  difficulty: 'AUTO'
})

function analyzeNeeds() {
  const category = inferCategoryFromNeeds(form)
  const payload = {
    ...form,
    category,
    categoryLabel: categoryLabelMap[category],
    reason: `${categoryLabelMap[category]} 키워드와 교육 대상 역량이 가장 강하게 감지되었습니다.`
  }

  analysis.value = payload
  sessionStorage.setItem('hrd_needs_analysis', JSON.stringify(payload))
  setTimeout(() => router.push('/recommendations'), 450)
}
</script>

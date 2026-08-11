<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">01 · Needs & Gap</p>
        <h1>사업계획과 교육 조건 입력</h1>
        <p>외부 AI 없이도 키워드와 역량 사전을 이용해 교육 분야를 추천합니다.</p>
      </div>
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
        <small>사업계획서나 조직 역량 진단 내용을 붙여 넣으면 키워드에서 교육 분야를 도출합니다.</small>
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
        <div class="form-row">
          <label for="budget">1인당 예산 (원)</label>
          <input id="budget" v-model.number="form.budget" type="number" min="0" placeholder="150000" />
          <small>입력하면 추천 화면에서 교육비가 예산을 넘는 과정을 표시합니다.</small>
        </div>
        <div class="form-row">
          <label for="deliveryType">희망 교육 방식</label>
          <select id="deliveryType" v-model="form.deliveryType">
            <option value="">무관</option>
            <!-- option 안에 줄바꿈을 넣으면 공백이 내용으로 잡혀 select 높이가 커진다 -->
            <option v-for="option in deliveryTypeOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="preferredCategory">관심 교육 분야</label>
          <select id="preferredCategory" v-model="form.preferredCategory">
            <option value="">키워드로 자동 판단</option>
            <option v-for="option in categoryOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
          </select>
        </div>
        <div class="form-row">
          <label for="region">희망 교육 지역</label>
          <!-- 지역은 명세상 자유 문자열이지만, 오타가 나면 카탈로그와 매칭되지 않는다.
               등록된 지역을 고르게 하고 없는 곳은 직접 입력으로 연다. -->
          <select id="region" v-model="regionChoice">
            <option value="">무관</option>
            <option v-for="region in knownRegions" :key="region" :value="region">{{ region }}</option>
            <option value="__custom">직접 입력</option>
          </select>
          <input
            v-if="regionChoice === '__custom'"
            id="regionCustom"
            v-model.trim="form.region"
            class="stacked-input"
            placeholder="예: 대전, 온라인"
          />
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
import { onMounted, reactive, ref, watch } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import { courseApi } from '@/api/course.js'
import {
  categoryLabelMap,
  categoryOptions,
  unwrapListResponse,
  deliveryTypeLabel,
  deliveryTypeOptions,
  difficultyLabel,
  formatPrice,
  inferCategoryFromNeeds
} from '@/utils/hrd.js'

const analysis = ref(null)

// 필드 구성은 02_functional_spec.md F-03 "예시 필드" 표를 따른다.
const form = reactive({
  businessPlan: '',
  targetJob: '',
  currentSkills: '',
  desiredSkills: '',
  difficulty: 'AUTO',
  budget: null,
  deliveryType: '',
  region: '',
  preferredCategory: ''
})

// 지역 선택값. '__custom' 이면 아래 입력칸의 값을 쓴다.
const regionChoice = ref('')
const knownRegions = ref([])

watch(regionChoice, value => {
  form.region = value === '__custom' ? '' : value
})

// 카탈로그에 실제로 등록된 지역만 후보로 보여 준다. 없는 값을 지어내지 않는다.
onMounted(async () => {
  try {
    const courses = unwrapListResponse(await courseApi.getAll())
    knownRegions.value = [...new Set(
      courses.flatMap(course => (course.region || '').split(',').map(r => r.trim()))
    )].filter(Boolean).sort()
  } catch (error) {
    console.error('[EducationNeeds] 지역 후보 조회 실패:', error)
  }
})

function analyzeNeeds() {
  // 분야를 직접 골랐으면 그 값을 쓰고, 아니면 키워드에서 도출한다.
  const category = form.preferredCategory || inferCategoryFromNeeds(form)

  // 입력한 조건은 전부 근거 문구에 드러낸다. 화면에 안 쓰이는 입력칸을 두지 않는다.
  const conditions = []
  if (form.difficulty !== 'AUTO') conditions.push(`난이도 ${difficultyLabel(form.difficulty)}`)
  if (form.deliveryType) conditions.push(`교육 방식 ${deliveryTypeLabel(form.deliveryType)}`)
  if (form.region) conditions.push(`교육 지역 ${form.region}`)
  if (form.budget > 0) conditions.push(`1인당 예산 ${formatPrice(form.budget)}`)

  const payload = {
    ...form,
    category,
    categoryLabel: categoryLabelMap[category],
    reason: `${categoryLabelMap[category]} 키워드가 가장 강하게 감지되었습니다.`
      + (conditions.length ? ` 지정한 조건은 ${conditions.join(', ')}입니다.` : '')
  }

  analysis.value = payload
  sessionStorage.setItem('hrd_needs_analysis', JSON.stringify(payload))
}
</script>

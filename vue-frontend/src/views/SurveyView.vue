<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Survey & Satisfaction</p>
        <h1>교육별 만족도 결과</h1>
        <p>진행 교육의 응답률, 평균 점수, 개선 의견을 교육 단위로 확인합니다.</p>
      </div>
      <router-link to="/trainings" class="btn btn-ghost">진행 중인 교육</router-link>
    </section>

    <section class="metric-grid">
      <article v-for="metric in summaryMetrics" :key="metric.label" class="metric-card">
        <div class="metric-top">
          <span>{{ metric.label }}</span>
          <b>{{ metric.icon }}</b>
        </div>
        <strong>{{ metric.value }}</strong>
        <small :class="metric.tone">{{ metric.note }}</small>
      </article>
    </section>

    <section class="content-grid two survey-results-grid">
      <article class="table-panel">
        <div class="panel-title">
          <h2>교육별 결과</h2>
          <span class="pill">{{ surveyResults.length }}개 교육</span>
        </div>
        <table>
          <thead>
            <tr>
              <th>교육명</th>
              <th>응답률</th>
              <th>평균</th>
              <th>상태</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="result in surveyResults"
              :key="result.courseId"
              class="clickable-row"
              :class="{ selected: selectedResult.courseId === result.courseId }"
              @click="selectResult(result.courseId)"
            >
              <td>
                <strong>{{ result.courseTitle }}</strong>
                <small>{{ result.providerName }}</small>
              </td>
              <td>{{ result.responseRate }}%</td>
              <td>{{ result.averageScore }}/5.0</td>
              <td>
                <span class="status-badge" :class="result.responseRate >= 85 ? 'active' : 'pending'">
                  {{ result.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel survey-detail-panel">
        <div class="panel-title">
          <h2>세부 결과</h2>
          <router-link to="/courses">카탈로그 보기</router-link>
        </div>

        <div class="survey-detail-head">
          <span class="pill success">{{ selectedResult.status }}</span>
          <h3>{{ selectedResult.courseTitle }}</h3>
          <p>{{ selectedResult.period }} · {{ selectedResult.respondents }}명 응답</p>
        </div>

        <div class="survey-score-grid">
          <div v-for="score in selectedResult.scores" :key="score.label">
            <span>{{ score.label }}</span>
            <strong>{{ score.value }}</strong>
          </div>
        </div>

        <div class="survey-section">
          <h3>주요 의견</h3>
          <ul class="curriculum-list">
            <li v-for="comment in selectedResult.comments" :key="comment">{{ comment }}</li>
          </ul>
        </div>

        <div class="survey-section">
          <h3>후속 조치</h3>
          <ul class="curriculum-list">
            <li v-for="action in selectedResult.actions" :key="action">{{ action }}</li>
          </ul>
        </div>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'

const route = useRoute()
const router = useRouter()

const surveyResults = [
  {
    courseId: 1,
    courseTitle: '생성형 AI 업무 자동화 과정',
    providerName: 'AI Works Lab',
    period: '2026.08.01 - 2026.08.03',
    responseRate: 93,
    respondents: 28,
    averageScore: '4.7',
    status: '결과 안정',
    scores: [
      { label: '교육 만족도', value: '4.8' },
      { label: '강사 만족도', value: '4.7' },
      { label: '업무 활용도', value: '4.6' },
      { label: '난이도 적절성', value: '4.5' }
    ],
    comments: [
      '실제 업무 문서와 유사한 실습 구성이 좋았습니다.',
      '부서별 적용 사례를 조금 더 늘리면 좋겠습니다.',
      '실습 시간이 충분해 바로 적용 가능하다는 의견이 많았습니다.'
    ],
    actions: [
      '후속 심화 과정 후보로 등록',
      '부서별 프롬프트 템플릿 자료 보강',
      '다음 기수 대상 난이도는 중급 유지'
    ]
  },
  {
    courseId: 2,
    courseTitle: 'MSA 전환과 Spring Cloud 실습',
    providerName: 'MSA Enablement Partners',
    period: '2026.08.05 - 2026.08.09',
    responseRate: 82,
    respondents: 23,
    averageScore: '4.3',
    status: '추가 확인',
    scores: [
      { label: '교육 만족도', value: '4.4' },
      { label: '강사 만족도', value: '4.5' },
      { label: '업무 활용도', value: '4.2' },
      { label: '난이도 적절성', value: '4.0' }
    ],
    comments: [
      '서비스 분리와 Gateway 흐름 이해에 도움이 되었습니다.',
      'Docker/Kafka 사전 지식 편차가 있어 일부 실습 속도가 빨랐습니다.',
      '현업 시스템 전환 사례가 더 있으면 좋겠습니다.'
    ],
    actions: [
      '사전 학습 자료를 교육 시작 전 배포',
      'DevOps 기초 과정과 연계 추천',
      '실습 난이도 안내 문구 보강'
    ]
  },
  {
    courseId: 3,
    courseTitle: '클라우드 네이티브 운영 기초',
    providerName: 'Fast Lane Korea',
    period: '2026.08.12 - 2026.08.13',
    responseRate: 76,
    respondents: 19,
    averageScore: '4.1',
    status: '응답 수집 중',
    scores: [
      { label: '교육 만족도', value: '4.2' },
      { label: '강사 만족도', value: '4.3' },
      { label: '업무 활용도', value: '4.0' },
      { label: '난이도 적절성', value: '3.9' }
    ],
    comments: [
      'Kubernetes 운영 용어를 정리하는 데 도움이 되었습니다.',
      '실습 환경 준비 시간이 조금 더 필요했습니다.',
      '초급자 대상 보충 세션 요청이 있었습니다.'
    ],
    actions: [
      '응답률 85%까지 리마인드',
      '실습 환경 체크리스트 추가',
      '초급 보충 세션 개설 검토'
    ]
  }
]

const selectedCourseId = ref(Number(route.query.courseId) || surveyResults[0].courseId)
const selectedResult = computed(() => (
  surveyResults.find(result => result.courseId === selectedCourseId.value) || surveyResults[0]
))

const summaryMetrics = computed(() => {
  const averageRate = Math.round(
    surveyResults.reduce((sum, result) => sum + result.responseRate, 0) / surveyResults.length
  )
  const averageScore = (
    surveyResults.reduce((sum, result) => sum + Number(result.averageScore), 0) / surveyResults.length
  ).toFixed(1)

  return [
    { label: '분석 교육 수', value: surveyResults.length, note: '진행/완료 교육 기준', icon: 'TR', tone: 'positive' },
    { label: '평균 응답률', value: `${averageRate}%`, note: '85% 미만 교육 확인', icon: 'RS', tone: averageRate >= 85 ? 'positive' : 'danger-text' },
    { label: '평균 만족도', value: `${averageScore}/5.0`, note: '전체 응답 평균', icon: 'ST', tone: '' },
    { label: '후속 조치', value: '7', note: '교육 개선 과제', icon: 'AC', tone: '' }
  ]
})

function selectResult(courseId) {
  selectedCourseId.value = courseId
  router.replace({ query: { courseId } })
}

watch(
  () => route.query.courseId,
  value => {
    const nextId = Number(value)
    if (nextId) selectedCourseId.value = nextId
  }
)
</script>

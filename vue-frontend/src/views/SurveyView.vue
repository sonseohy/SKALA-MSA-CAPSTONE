<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Survey &amp; Satisfaction</p>
        <h1>만족도 결과 조회</h1>
        <p>교육별 응답률과 평균 점수, 개선 의견과 후속 조치를 확인합니다.</p>
      </div>
      <router-link to="/trainings" class="btn btn-ghost">진행 중인 교육</router-link>
    </section>

    <section v-if="notImplemented" class="insight-banner">
      <div>
        <span class="pill danger">백엔드 미구현</span>
        <h2>만족도 API가 아직 배포되지 않았습니다.</h2>
        <p>
          화면과 요청 형식은 <code>planning-docs</code>의 Sprint 2 규격에 맞춰 준비되어 있습니다.
          enrollment-service에 만족도 API가 추가되면 프론트 수정 없이 그대로 동작합니다.
          아래 수치는 채워지지 않은 상태이며, 임의의 값을 대신 표시하지 않습니다.
        </p>
      </div>
    </section>

    <section v-if="loading" class="panel">교육 목록을 불러오는 중입니다.</section>

    <section v-else-if="!activeEnrollments.length" class="empty-panel">
      <h2>만족도를 확인할 교육이 없습니다.</h2>
      <p>교육이 확정(ACTIVE)되면 이곳에서 만족도 결과를 확인할 수 있습니다.</p>
      <router-link to="/courses" class="btn btn-primary">Program Catalog</router-link>
    </section>

    <section v-else class="content-grid two survey-results-grid">
      <article class="table-panel">
        <div class="panel-title">
          <h2>확정 교육</h2>
          <!-- 카탈로그의 카테고리 칩과 같은 방식. 누르면 바로 전환된다. -->
          <div class="chip-row" role="tablist">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              role="tab"
              class="filter-chip"
              :aria-selected="activeTab === tab.value"
              :class="{ active: activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              {{ tab.label }} {{ tabCounts[tab.value] }}
            </button>
          </div>
        </div>

        <p v-if="checkingSubmission" class="muted-note">제출 여부를 확인하는 중입니다.</p>

        <p v-if="!tabItems.length" class="muted-note">이 구분에 해당하는 교육이 없습니다.</p>

        <table v-else>
          <thead>
            <tr>
              <th>교육명</th>
              <th>응답</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in pager.items.value"
              :key="item.id"
              class="clickable-row"
              :class="{ selected: selectedId === item.id }"
              @click="select(item.id)"
            >
              <td>
                <strong>{{ item.course?.title || `Program #${item.courseId}` }}</strong>
                <small>계약 #{{ item.id }}</small>
              </td>
              <td>
                <span class="status-badge" :class="submitted[item.id] ? 'active' : 'pending'">
                  {{ submitted[item.id] ? '제출 완료' : '미제출' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>

        <PagerBar
          :page="pager.page.value"
          :total-pages="pager.totalPages.value"
          :total="pager.total.value"
          :range-start="pager.rangeStart.value"
          :range-end="pager.rangeEnd.value"
          :page-numbers="pager.pageNumbers.value"
          unit="건"
          @go="pager.go"
        />
      </article>

      <article class="panel survey-detail-panel">
        <!-- 어떤 교육에 대한 결과인지 먼저 밝힌다 -->
        <header class="survey-subject">
          <p class="eyebrow">만족도 결과</p>
          <h2>{{ selected?.course?.title || '왼쪽에서 교육을 선택해 주세요' }}</h2>
          <p v-if="selected" class="survey-subject-meta">
            계약 #{{ selected.id }} · {{ formatSchedule(selected.course?.startDate, selected.course?.endDate) }}
            · {{ deliveryTypeLabel(selected.course?.deliveryType) }}
            · 신청 {{ selected.course?.enrollmentCount ?? 0 }}건
          </p>
        </header>

        <template v-if="selected">
          <div v-if="summary" class="survey-section">
            <div class="survey-score-grid">
              <div><span>응답률</span><strong>{{ responseRate }}</strong></div>
              <div><span>응답 수</span><strong>{{ summary.surveyCount ?? 0 }}건</strong></div>
              <div><span>교육 만족도</span><strong>{{ scoreText(summary.averageEducationScore) }}</strong></div>
              <div><span>강사 만족도</span><strong>{{ scoreText(summary.averageInstructorScore) }}</strong></div>
              <div><span>업무 활용도</span><strong>{{ scoreText(summary.averageUsefulnessScore) }}</strong></div>
              <div><span>난이도 적절성</span><strong>{{ scoreText(summary.averageDifficultyScore) }}</strong></div>
            </div>
          </div>
          <p v-else class="muted-note">
            {{ notImplemented ? '만족도 API 배포 후 집계 결과가 표시됩니다.' : '이 교육의 만족도 집계가 아직 없습니다.' }}
          </p>

          <h3 class="sub-title">주요 의견</h3>
          <ul v-if="opinions.length" class="curriculum-list">
            <li v-for="(opinion, index) in opinions" :key="index">{{ opinion }}</li>
          </ul>
          <p v-else class="muted-note">등록된 의견이 없습니다. 만족도가 제출되면 이곳에 표시됩니다.</p>

          <h3 class="sub-title">후속 조치</h3>
          <ul v-if="followUps.length" class="curriculum-list">
            <li v-for="(action, index) in followUps" :key="index">{{ action }}</li>
          </ul>
          <p v-else class="muted-note">
            {{ summary ? '조치가 필요한 지표가 없습니다.' : '집계 결과가 있어야 후속 조치를 도출할 수 있습니다.' }}
          </p>

          <h3 class="sub-title">{{ isSubmitted ? '내가 제출한 만족도' : '만족도 제출' }}</h3>
          <p class="muted-note">
            <strong>{{ selected.course?.title || `Program #${selected.courseId}` }}</strong> 교육에 대한 만족도입니다.
            명세상 Sprint 2 후보 기능이며 백엔드 배포 후 저장됩니다.
          </p>

          <!-- 이미 제출한 건은 잠가 둔다. 고치려면 수정을 눌러야 한다. -->
          <template v-if="isSubmitted && !editing">
            <div class="survey-section">
              <div class="survey-score-grid">
                <div v-for="field in scoreFields" :key="field.key">
                  <span>{{ field.label }}</span>
                  <strong>{{ mySurvey?.[field.key] ?? '-' }}점</strong>
                </div>
              </div>
              <p class="muted-note">
                <strong>의견</strong> — {{ mySurvey?.comment || '남긴 의견이 없습니다.' }}
              </p>
              <p v-if="mySurvey?.createdAt" class="muted-note">제출 시각 {{ formatDate(mySurvey.createdAt) }}</p>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-ghost" @click="startEditing">수정</button>
            </div>
          </template>

          <form v-else class="needs-form" @submit.prevent="handleSubmit">
            <div class="survey-score-grid">
              <div v-for="field in scoreFields" :key="field.key" class="form-row">
                <label :for="field.key">{{ field.label }}</label>
                <select :id="field.key" v-model.number="form[field.key]">
                  <option v-for="n in 5" :key="n" :value="n">{{ n }}점</option>
                </select>
              </div>
            </div>

            <div class="form-row full">
              <label for="comment">의견</label>
              <textarea
                id="comment"
                v-model.trim="form.comment"
                rows="4"
                placeholder="교육에서 좋았던 점과 개선이 필요한 점을 적어 주세요."
              />
            </div>

            <div v-if="message" class="notice-box" :class="{ error: hasError }">{{ message }}</div>

            <div class="form-actions">
              <button type="submit" class="btn btn-primary" :disabled="submitting">
                {{ submitting ? '저장 중...' : (editing ? '수정 내용 저장' : '만족도 제출') }}
              </button>
              <button v-if="editing" type="button" class="btn btn-ghost" @click="cancelEditing">취소</button>
            </div>
          </form>
        </template>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import PagerBar from '@/components/PagerBar.vue'
import { enrollmentApi, surveyApi } from '@/api/enrollment.js'
import { usePagedList } from '@/composables/usePagedList.js'
import { deliveryTypeLabel, formatDate, formatSchedule, unwrapObjectResponse } from '@/utils/hrd.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const checkingSubmission = ref(false)
const enrollments = ref([])
const selectedId = ref(null)
const message = ref('')
const hasError = ref(false)
const notImplemented = ref(false)
const submitted = reactive({})
const summary = ref(null)
const mySurvey = ref(null)
const activeTab = ref('ALL')
const editing = ref(false)

const tabs = [
  { value: 'ALL', label: '전체' },
  { value: 'PENDING', label: '미제출' },
  { value: 'DONE', label: '제출 완료' }
]

const scoreFields = [
  { key: 'educationScore', label: '교육 만족도' },
  { key: 'instructorScore', label: '강사 만족도' },
  { key: 'usefulnessScore', label: '업무 활용도' },
  { key: 'difficultyScore', label: '난이도 적절성' }
]

const form = reactive({
  educationScore: 5,
  instructorScore: 5,
  usefulnessScore: 4,
  difficultyScore: 3,
  comment: ''
})

const activeEnrollments = computed(() => enrollments.value.filter(item => item.status === 'ACTIVE'))
const selected = computed(() => activeEnrollments.value.find(item => item.id === selectedId.value) || null)

const tabItems = computed(() => {
  if (activeTab.value === 'DONE') return activeEnrollments.value.filter(item => submitted[item.id])
  if (activeTab.value === 'PENDING') return activeEnrollments.value.filter(item => !submitted[item.id])
  return activeEnrollments.value
})

const tabCounts = computed(() => {
  const done = activeEnrollments.value.filter(item => submitted[item.id]).length
  return { ALL: activeEnrollments.value.length, DONE: done, PENDING: activeEnrollments.value.length - done }
})

const pager = usePagedList(tabItems, 10)

// 응답률 = 응답 수 / 신청 수. 신청 수를 모르면 계산하지 않고 그 사실을 표시한다.
const responseRate = computed(() => {
  const responses = Number(summary.value?.surveyCount)
  const enrolled = Number(selected.value?.course?.enrollmentCount)
  if (!Number.isFinite(responses) || !enrolled) return '신청 수 미확인'
  return `${Math.round((responses / enrolled) * 100)}% (${responses}/${enrolled})`
})

const opinions = computed(() => {
  const comment = mySurvey.value?.comment?.trim()
  return comment ? [comment] : []
})

// 후속 조치는 집계 값에서 도출한다. 값이 없으면 아무것도 만들지 않는다.
const followUps = computed(() => {
  if (!summary.value) return []
  const actions = []
  const { averageEducationScore, averageInstructorScore, averageUsefulnessScore, averageDifficultyScore } = summary.value

  if (averageEducationScore < 4) actions.push('교육 내용 개편 검토 (교육 만족도 4점 미만)')
  if (averageInstructorScore < 4) actions.push('강사 피드백 공유 및 보완 (강사 만족도 4점 미만)')
  if (averageUsefulnessScore < 4) actions.push('실무 과제 비중 확대 검토 (업무 활용도 4점 미만)')
  if (averageDifficultyScore > 3.5) actions.push('난이도 하향 또는 선수 학습 제공 (난이도 3.5점 초과)')
  if (averageDifficultyScore && averageDifficultyScore < 2.5) actions.push('난이도 상향 검토 (난이도 2.5점 미만)')

  const responses = Number(summary.value.surveyCount)
  const enrolled = Number(selected.value?.course?.enrollmentCount)
  if (enrolled && responses / enrolled < 0.5) actions.push('설문 응답 독려 (응답률 50% 미만)')

  return actions
})

function scoreText(value) {
  return Number.isFinite(Number(value)) ? `${value}점` : '집계 없음'
}

const isSubmitted = computed(() => Boolean(selectedId.value && submitted[selectedId.value]))

function startEditing() {
  // 제출한 값을 그대로 불러와 고치게 한다.
  if (mySurvey.value) {
    scoreFields.forEach(field => {
      const value = Number(mySurvey.value[field.key])
      if (Number.isFinite(value)) form[field.key] = value
    })
    form.comment = mySurvey.value.comment || ''
  }
  message.value = ''
  hasError.value = false
  editing.value = true
}

function cancelEditing() {
  editing.value = false
  message.value = ''
  hasError.value = false
}

// 집계 API 가 없으면 만족도 기능 자체가 배포 전이라고 본다.
function isNotImplemented(error) {
  return [404, 405, 501].includes(error?.response?.status)
}

// 개별 조회(GET /enrollments/{id}/survey)의 404 는 "아직 제출하지 않음" 이라는 정상 응답이다.
// 이것까지 미구현으로 보면 미제출 계약 하나에 화면 전체가 미구현 모드로 넘어간다.
function isEndpointMissing(error) {
  return [405, 501].includes(error?.response?.status)
}

function select(id) {
  selectedId.value = id
  const courseId = activeEnrollments.value.find(item => item.id === id)?.courseId
  if (courseId) router.replace({ query: { courseId } })
}

async function loadSummary(courseId) {
  summary.value = null
  if (!courseId || notImplemented.value) return
  try {
    summary.value = unwrapObjectResponse(await surveyApi.getCourseSummary(courseId))
  } catch (error) {
    if (isNotImplemented(error)) notImplemented.value = true
    else console.error('[Survey] 요약 조회 실패:', error)
  }
}

// 명세 02_functional_spec.md:259 의 향후 권장 API. 제출한 의견을 결과 화면에 되싣는다.
async function loadMySurvey(enrollmentId) {
  mySurvey.value = null
  if (!enrollmentId || notImplemented.value) return
  try {
    mySurvey.value = unwrapObjectResponse(await surveyApi.getByEnrollment(enrollmentId))
    if (mySurvey.value) submitted[enrollmentId] = true
  } catch (error) {
    // 404 = 아직 제출하지 않음. 오류가 아니다.
    if (error?.response?.status === 404) submitted[enrollmentId] = false
    else if (isEndpointMissing(error)) notImplemented.value = true
    else console.error('[Survey] 제출 내역 조회 실패:', error)
  }
}

/**
 * 제출 완료/미제출 탭을 정확히 나누려면 항목마다 제출 여부를 알아야 한다.
 * 명세에 제출 여부를 목록으로 주는 API 가 없어 건별로 조회한다(N+1).
 * 한꺼번에 쏘지 않도록 동시 요청 수를 제한한다.
 * 백엔드가 `GET /api/enrollments/my` 응답에 제출 여부를 실어 주면 이 함수는 지운다.
 */
async function loadSubmissionStates(items, concurrency = 5) {
  checkingSubmission.value = true
  const queue = [...items]
  const workers = Array.from({ length: Math.min(concurrency, queue.length) }, async () => {
    while (queue.length) {
      const item = queue.shift()
      if (notImplemented.value) return
      try {
        const found = unwrapObjectResponse(await surveyApi.getByEnrollment(item.id))
        if (found) submitted[item.id] = true
      } catch (error) {
        // 404 는 "이 계약은 아직 미제출" 이라는 정상 응답이다.
        if (error?.response?.status === 404) continue
        if (isEndpointMissing(error)) {
          // 엔드포인트 자체가 없으므로 나머지 조회를 멈춘다.
          notImplemented.value = true
          return
        }
        console.error(`[Survey] 계약 ${item.id} 제출 여부 조회 실패:`, error)
      }
    }
  })
  await Promise.all(workers)
  checkingSubmission.value = false
}

async function handleSubmit() {
  if (!selectedId.value) return
  message.value = ''
  hasError.value = false
  submitting.value = true
  try {
    await surveyApi.submit(selectedId.value, { ...form })
    const wasEditing = editing.value
    submitted[selectedId.value] = true
    editing.value = false
    message.value = wasEditing ? '만족도를 수정했습니다.' : '만족도를 제출했습니다.'
    await loadSummary(selected.value?.courseId)
    await loadMySurvey(selectedId.value)
  } catch (error) {
    hasError.value = true
    if (isNotImplemented(error)) {
      notImplemented.value = true
      message.value = '만족도 API가 아직 백엔드에 없습니다. 입력한 내용은 저장되지 않았습니다.'
    } else {
      message.value = error.response?.data?.message || '제출에 실패했습니다.'
    }
  } finally {
    submitting.value = false
  }
}

// 순차로 부른다. 요약이 404 면 notImplemented 가 서서 개별 조회를 건너뛴다.
watch(selectedId, async id => {
  editing.value = false
  message.value = ''
  const courseId = activeEnrollments.value.find(item => item.id === id)?.courseId
  await loadSummary(courseId)
  await loadMySurvey(id)
})

// 탭을 바꾸면 목록이 달라지므로 선택을 그 목록 안으로 맞춘다.
watch(activeTab, () => {
  if (!tabItems.value.some(item => item.id === selectedId.value)) {
    selectedId.value = tabItems.value[0]?.id ?? null
  }
})

onMounted(async () => {
  try {
    const res = await enrollmentApi.getMyEnrollments()
    enrollments.value = Array.isArray(res.data?.data) ? res.data.data : []
    const queryCourseId = Number(route.query.courseId)
    const preset = activeEnrollments.value.find(item => Number(item.courseId) === queryCourseId)
    selectedId.value = preset?.id ?? activeEnrollments.value[0]?.id ?? null
  } catch (error) {
    console.error('[Survey] 교육 목록 조회 실패:', error)
    enrollments.value = []
  } finally {
    loading.value = false
  }

  if (activeEnrollments.value.length) await loadSubmissionStates(activeEnrollments.value)
})
</script>

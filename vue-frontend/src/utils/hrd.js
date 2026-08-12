// 표시 문구는 planning-docs/03_api_spec.md "화면 표시 권장" 표를 따른다.
// Category enum 8종을 모두 담는다 — 하나라도 빠지면 등록 폼·필터에서 선택할 수 없다.
export const categoryOptions = [
  { value: 'BACKEND', label: '백엔드/서버', short: '백엔드' },
  { value: 'FRONTEND', label: '프론트엔드', short: '프론트' },
  { value: 'DEVOPS', label: 'Cloud/DevOps', short: 'DevOps' },
  { value: 'DATA_SCIENCE', label: 'AI/Data', short: 'AI' },
  { value: 'MOBILE', label: '모바일', short: '모바일' },
  { value: 'SECURITY', label: '보안', short: '보안' },
  { value: 'DATABASE', label: '데이터베이스', short: 'DB' },
  { value: 'OTHER', label: '기타 기업 교육', short: '기타' }
]

export const categoryLabelMap = categoryOptions.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {
  DATA: 'AI/Data',
  AI: 'AI/Data'
})

export const categoryShortMap = categoryOptions.reduce((acc, item) => {
  acc[item.value] = item.short
  return acc
}, {
  DATA: 'AI',
  AI: 'AI'
})

// description 은 명세(03_api_spec.md:142)에서 라벨 줄을 이어 붙인 형식이다.
//   "커리큘럼: 프롬프트 작성, 업무 자동화\n공급자 경력: 기업 AI 교육 20회"
// 라벨별로 나눠 두고, 라벨이 없으면 전체를 소개 문구로 본다.
export function parseCourseDescription(description) {
  const raw = (description || '').trim()
  const result = { curriculum: [], providerCareer: '', intro: '' }
  if (!raw) return result

  const labels = []
  for (const line of raw.split(/\r?\n/)) {
    const matched = line.match(/^\s*([^:：]{2,12})\s*[:：]\s*(.*)$/)
    if (matched) labels.push({ key: matched[1].trim(), value: matched[2].trim() })
    else if (labels.length) labels[labels.length - 1].value += ` ${line.trim()}`
    else result.intro += (result.intro ? ' ' : '') + line.trim()
  }

  for (const { key, value } of labels) {
    if (key === '커리큘럼') {
      // 가운뎃점(·)은 구분자로 쓰지 않는다. 한국어에서 「문서 요약·초안 자동화」처럼
      // 낱말을 잇는 용도가 더 흔해서, 구분자로 삼으면 한 항목이 둘로 쪼개진다.
      result.curriculum = value.split(/[;,]/).map(item => item.trim()).filter(Boolean)
    } else if (key === '공급자 경력') {
      result.providerCareer = value
    } else {
      result.intro += (result.intro ? ' ' : '') + `${key}: ${value}`
    }
  }

  return result
}

// 프로필 사진을 담을 필드가 백엔드에 없다(User: id·email·name·role·createdAt).
// 없는 이미지를 지어내는 대신, 이름에서 색을 정해 사람마다 같은 아바타가 나오게 한다.
// 같은 이름이면 항상 같은 색이라 사진처럼 구분에 쓸 수 있다.
const AVATAR_COLORS = [
  '#315f9f', '#8a4b9e', '#0f7b6c', '#b4560f', '#9f1239',
  '#1f6f8b', '#5b6ab0', '#7a5b1f', '#2f6d3a', '#8d3a5e'
]

export function avatarColor(name) {
  const text = String(name || '')
  if (!text) return AVATAR_COLORS[0]
  let hash = 0
  for (let i = 0; i < text.length; i += 1) {
    hash = (hash * 31 + text.charCodeAt(i)) % 100000
  }
  return AVATAR_COLORS[hash % AVATAR_COLORS.length]
}

export function avatarInitial(name) {
  const text = String(name || '').trim()
  if (!text) return '?'
  // 한글 이름은 성 한 글자, 영문/기관명은 첫 글자를 대문자로.
  return /[가-힣]/.test(text[0]) ? text[0] : text[0].toUpperCase()
}

export function normalizeCategory(category) {
  return categoryLabelMap[category] || category || '기타 기업 교육'
}

export function normalizeCourse(course) {
  if (!course || typeof course !== 'object') return course
  return {
    ...course,
    categoryLabel: normalizeCategory(course.category),
    categoryShort: categoryShortMap[course.category] || categoryShortMap[course.categoryLabel] || 'Edu',
    providerName: course.instructorName || course.teacherName || course.providerName || `Provider #${course.instructorId ?? '-'}`,
    deliveryTypeLabel: deliveryTypeLabel(course.deliveryType),
    difficultyLabel: difficultyLabel(course.difficulty),
    durationLabel: formatDuration(course.durationDays),
    scheduleLabel: formatSchedule(course.startDate, course.endDate)
  }
}

export function unwrapListResponse(response) {
  const payload = response?.data
  if (Array.isArray(payload?.data)) return payload.data
  // 서버 페이징 응답(PageResult·MyEnrollmentsResponse)은 목록이 content 에 들어온다.
  if (Array.isArray(payload?.data?.content)) return payload.data.content
  if (Array.isArray(payload?.recommendedCourses)) return payload.recommendedCourses
  if (Array.isArray(payload)) return payload
  return []
}

export function unwrapObjectResponse(response) {
  const payload = response?.data
  return payload?.data && typeof payload.data === 'object' ? payload.data : payload
}

export function formatPrice(price) {
  const value = Number(price ?? 0)
  if (Number.isNaN(value)) return '-'
  return `${value.toLocaleString()}원`
}

export function formatDuration(days) {
  const value = Number(days)
  if (!Number.isFinite(value) || value <= 0) return '협의'
  return `${value}일`
}

export function formatDate(date) {
  if (!date) return ''
  const value = new Date(date)
  if (Number.isNaN(value.getTime())) return String(date)
  return value.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).replace(/\. /g, '.').replace(/\.$/, '')
}

export function formatSchedule(startDate, endDate) {
  const start = formatDate(startDate)
  const end = formatDate(endDate)
  if (start && end) return `${start} - ${end}`
  if (start) return `${start} 시작`
  if (end) return `${end} 종료`
  return '일정 협의'
}

// 03_api_spec.md deliveryType enum: ONLINE, OFFLINE, HYBRID, TBD
export const deliveryTypeOptions = [
  { value: 'TBD', label: '협의' },
  { value: 'ONLINE', label: '온라인' },
  { value: 'OFFLINE', label: '오프라인' },
  { value: 'HYBRID', label: '온/오프라인 병행' }
]

export function deliveryTypeLabel(value) {
  const labels = {
    ONLINE: '온라인',
    OFFLINE: '오프라인',
    HYBRID: '온/오프라인 병행',
    TBD: '협의'
  }
  return labels[value] || value || '협의'
}

export function difficultyLabel(value) {
  const labels = {
    BASIC: '기초',
    INTERMEDIATE: '중급',
    ADVANCED: '고급',
    AUTO: '자동 선택'
  }
  return labels[value] || value || '자동 선택'
}

// 문구는 02_functional_spec.md F-08 / 03_api_spec.md 상태 매핑표를 따른다.
export function statusLabel(status) {
  const labels = {
    PENDING: '계약/참여 신청 중',
    ACTIVE: '교육 확정',
    CANCELLED: '취소됨',
    COMPLETED: '완료',
    FAILED: '처리 실패'
  }
  return labels[status] || status || '대기'
}

export function isProviderRole(role) {
  return role === 'INSTRUCTOR'
}

export function isHrdRole(role) {
  return !isProviderRole(role)
}

export function roleLabel(role) {
  return isProviderRole(role) ? '교육 공급자' : 'HRD 담당자'
}

export function inferCategoryFromNeeds({ businessPlan = '', targetJob = '', currentSkills = '', desiredSkills = '' }) {
  const text = `${businessPlan} ${targetJob} ${currentSkills} ${desiredSkills}`.toLowerCase()
  const rules = [
    { value: 'DATA_SCIENCE', keywords: ['ai', 'llm', 'gpt', '생성형', '인공지능', '데이터', 'python', '분석'] },
    { value: 'DEVOPS', keywords: ['cloud', 'aws', 'kubernetes', 'docker', 'k8s', '클라우드', '데브옵스'] },
    { value: 'BACKEND', keywords: ['msa', 'spring', 'java', 'backend', 'microservice', '마이크로서비스', '백엔드'] },
    { value: 'SECURITY', keywords: ['security', '보안', '개인정보', '취약점'] },
    { value: 'DATABASE', keywords: ['sql', 'database', 'db', '데이터베이스', 'mariadb'] },
    { value: 'FRONTEND', keywords: ['frontend', 'vue', 'react', 'ui', '프론트'] }
  ]

  return rules.find(rule => rule.keywords.some(keyword => text.includes(keyword)))?.value || 'OTHER'
}

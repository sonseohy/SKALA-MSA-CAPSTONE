export const categoryOptions = [
  { value: 'BACKEND', label: 'Backend / MSA', short: 'MSA' },
  { value: 'FRONTEND', label: 'Frontend Experience', short: 'FE' },
  { value: 'DEVOPS', label: 'Cloud / DevOps', short: 'Ops' },
  { value: 'DATA_SCIENCE', label: 'AI / Data', short: 'AI' },
  { value: 'SECURITY', label: 'Security', short: 'Sec' },
  { value: 'DATABASE', label: 'Database', short: 'DB' },
  { value: 'OTHER', label: 'General Business', short: 'Biz' }
]

export const categoryLabelMap = categoryOptions.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {
  DATA: 'AI / Data',
  AI: 'AI / Data',
  MOBILE: 'Mobile'
})

export const categoryShortMap = categoryOptions.reduce((acc, item) => {
  acc[item.value] = item.short
  return acc
}, {
  DATA: 'AI',
  AI: 'AI',
  MOBILE: 'Mob'
})

export function normalizeCategory(category) {
  return categoryLabelMap[category] || category || 'General Business'
}

export function normalizeCourse(course) {
  if (!course || typeof course !== 'object') return course
  return {
    ...course,
    categoryLabel: normalizeCategory(course.category),
    categoryShort: categoryShortMap[course.category] || categoryShortMap[course.categoryLabel] || 'Edu',
    providerName: course.instructorName || course.teacherName || course.providerName || `Provider #${course.instructorId ?? '-'}`
  }
}

export function unwrapListResponse(response) {
  const payload = response?.data
  if (Array.isArray(payload?.data)) return payload.data
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

export function statusLabel(status) {
  const labels = {
    PENDING: '신청 검토 중',
    ACTIVE: '교육 확정',
    CANCELLED: '취소됨',
    COMPLETED: '완료',
    FAILED: '처리 실패'
  }
  return labels[status] || status || '대기'
}

export function roleLabel(role) {
  return role === 'INSTRUCTOR' ? '교육 공급자' : 'HRD 담당자'
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

export const sampleProviders = [
  {
    id: 1,
    name: 'Fast Lane Korea',
    type: '교육기관',
    specialty: 'Cloud / DevOps',
    region: '서울, 온라인',
    experience: '기업교육 120회',
    satisfaction: '4.8'
  },
  {
    id: 2,
    name: 'AI Works Lab',
    type: '전문 강사 그룹',
    specialty: 'AI / Data',
    region: '수도권, 온라인',
    experience: '생성형 AI 워크숍 42회',
    satisfaction: '4.7'
  },
  {
    id: 3,
    name: 'MSA Enablement Partners',
    type: '컨설팅/교육',
    specialty: 'Backend / MSA',
    region: '전국 협의',
    experience: '전환 프로젝트 교육 35회',
    satisfaction: '4.6'
  }
]

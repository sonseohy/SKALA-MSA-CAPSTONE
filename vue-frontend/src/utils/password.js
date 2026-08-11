// 비밀번호 정책 — 백엔드 UserDto 의 @Pattern(PASSWORD_PATTERN) 규칙과 반드시 동일하게 유지한다.
// 한쪽만 바꾸면 프론트는 통과했는데 서버가 400 을 내거나 그 반대가 되어 사용자에게 혼란을 준다.
export const PASSWORD_RULES = [
  { key: 'length', label: '8자 이상', test: (v) => v.length >= 8 },
  { key: 'upper', label: '영문 대문자 포함', test: (v) => /[A-Z]/.test(v) },
  { key: 'special', label: '특수문자 포함', test: (v) => /[^A-Za-z0-9]/.test(v) }
]

// 입력값이 각 규칙을 충족하는지와 전체 통과 여부를 함께 반환한다.
// 회원가입·비밀번호 재설정 화면에서 실시간 체크리스트와 제출 가드에 함께 쓴다.
export function checkPassword(value = '') {
  const rules = PASSWORD_RULES.map((rule) => ({
    key: rule.key,
    label: rule.label,
    passed: rule.test(value)
  }))
  return { rules, valid: rules.every((rule) => rule.passed) }
}

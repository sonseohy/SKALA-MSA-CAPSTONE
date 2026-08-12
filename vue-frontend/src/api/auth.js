import api from './index.js'

const DEFAULT_REDIRECT_URI =
  typeof window !== 'undefined' ? `${window.location.origin}/callback` : 'http://localhost:3000/callback'

export const authApi = {
  // OAuth2 Authorization Code -> Access Token 교환
  // 토큰 교환은 user-service(BFF)가 대신한다. client_secret 은 서버에만 있고
  // 브라우저 번들에는 들어가지 않는다(예전에는 여기서 Basic 인증으로 직접 불렀다).
  exchangeCode(code) {
    return api.post('/api/users/token', {
      code,
      redirectUri: import.meta.env.VITE_REDIRECT_URI || DEFAULT_REDIRECT_URI
    })
  },

  // 내 정보 조회
  getMe() {
    return api.get('/api/users/me')
  },

  // 회원가입
  register(data) {
    return api.post('/api/users/register', data)
  }
}

// 비밀번호 재설정 — 인증 없이 호출하는 공개 엔드포인트
export const passwordApi = {
  /**
   * 비밀번호 재설정을 요청한다.
   * 항상 200 을 반환하며, 응답 본문(resp.data.data)에 {resetToken, resetUrl} 이
   * 실릴 수도(이메일·이름 일치), 비어 있을 수도(불일치 시 일반 안내 문구) 있다 —
   * 계정 존재 여부가 외부에 노출되지 않도록 백엔드가 의도적으로 감춘다.
   * 데모 환경이라 resetUrl 을 화면에 그대로 보여준다(실제 운영에서는 이메일 발송).
   * @param {string} email 가입 시 등록한 기업 이메일
   * @param {string} name 가입 시 등록한 이름
   * @returns {Promise} axios 응답. resp.data.message 에 사용자 안내 문구.
   */
  resetRequest(email, name) {
    return api.post('/api/users/password/reset-request', { email, name })
  },

  /**
   * 재설정 토큰과 새 비밀번호로 비밀번호 변경을 확정한다.
   * 토큰이 만료·불일치하면 400 이 오며 사유는 resp.data.message 에 담긴다.
   * @param {string} token 재설정 요청 응답으로 받은 토큰
   * @param {string} newPassword 새 비밀번호(8자 이상)
   * @returns {Promise} axios 응답
   */
  resetConfirm(token, newPassword) {
    return api.post('/api/users/password/reset-confirm', { token, newPassword })
  }
}

// 사용자 단건 조회 — 교육 공급자 정보 표시에 사용
// planning-docs/03_api_spec.md "4. User API · 사용자 단건 조회"
export const userApi = {
  getById(id) {
    return api.get(`/api/users/${id}`)
  },

  /**
   * 사용자 목록 조회. id 여러 개를 한 번에 해소하거나(공급자 이름 채우기),
   * 역할로 전체 목록을 받는다(공급자 목록 화면).
   * @param {{ids?: Array<number|string>, role?: string}} params
   * @returns {Promise} axios 응답. data.data 에 UserResponse 배열.
   */
  getUsers({ ids, role } = {}) {
    // 백엔드는 ids=4,6,8 형태를 받는다. 배열을 그대로 넘기면 axios 가 ids[]= 로 직렬화한다.
    return api.get('/api/users', { params: { ids: ids?.length ? ids.join(',') : undefined, role } })
  },

  // 계정 정보 수정 — 명세에 아직 없는 신규 후보 API.
  // User 엔티티에 있는 필드(name·email)만 다룬다. 소개·경력은 Course.description 에 있다.
  // 백엔드가 없으면 404/405/501 이 오므로 호출부에서 미구현 상태로 안내한다.
  update(id, payload) {
    return api.put(`/api/users/${id}`, payload)
  }
}

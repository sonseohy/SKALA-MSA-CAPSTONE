import api from './index.js'
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const DEFAULT_CLIENT_ID = 'web-client'
const DEFAULT_REDIRECT_URI =
  typeof window !== 'undefined' ? `${window.location.origin}/callback` : 'http://localhost:3000/callback'

export const authApi = {
  // OAuth2 Authorization Code -> Access Token 교환
  // CLIENT_SECRET_BASIC: Authorization 헤더에 client_id:client_secret을 Base64로 인코딩
  //
  // 시크릿은 기본값을 두지 않는다. 브라우저 번들에 실제 값이 박히는 것을 막기 위해
  // .env(.env.example 참고)로만 주입하고, 없으면 즉시 실패시켜 원인을 드러낸다.
  exchangeCode(code) {
    const clientId = import.meta.env.VITE_CLIENT_ID || DEFAULT_CLIENT_ID
    const clientSecret = import.meta.env.VITE_CLIENT_SECRET
    const redirectUri = import.meta.env.VITE_REDIRECT_URI || DEFAULT_REDIRECT_URI

    if (!clientSecret) {
      return Promise.reject(
        new Error('VITE_CLIENT_SECRET 이 설정되지 않았습니다. .env.example 을 복사해 .env 를 만들어 주세요.')
      )
    }

    const credentials = btoa(`${clientId}:${clientSecret}`)

    const body = new URLSearchParams({
      grant_type: 'authorization_code',
      code,
      redirect_uri: redirectUri
    })

    return axios.post(
      `${API_BASE_URL}/oauth2/token`,
      body.toString(),
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Authorization': `Basic ${credentials}`
        }
      }
    )
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

// 사용자 단건 조회 — 교육 공급자 정보 표시에 사용
// planning-docs/03_api_spec.md "4. User API · 사용자 단건 조회"
export const userApi = {
  getById(id) {
    return api.get(`/api/users/${id}`)
  },

  // 계정 정보 수정 — 명세에 아직 없는 신규 후보 API.
  // User 엔티티에 있는 필드(name·email)만 다룬다. 소개·경력은 Course.description 에 있다.
  // 백엔드가 없으면 404/405/501 이 오므로 호출부에서 미구현 상태로 안내한다.
  update(id, payload) {
    return api.put(`/api/users/${id}`, payload)
  }
}

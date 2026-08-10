import api from './index.js'
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const DEFAULT_CLIENT_ID = 'web-client'
const DEFAULT_CLIENT_SECRET = 'web-secret'
const DEFAULT_REDIRECT_URI =
  typeof window !== 'undefined' ? `${window.location.origin}/callback` : 'http://localhost:3000/callback'

export const authApi = {
  // OAuth2 Authorization Code -> Access Token 교환
  // CLIENT_SECRET_BASIC: Authorization 헤더에 client_id:client_secret을 Base64로 인코딩
  exchangeCode(code) {
    const clientId = import.meta.env.VITE_CLIENT_ID || DEFAULT_CLIENT_ID
    const clientSecret = import.meta.env.VITE_CLIENT_SECRET || DEFAULT_CLIENT_SECRET
    const redirectUri = import.meta.env.VITE_REDIRECT_URI || DEFAULT_REDIRECT_URI
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

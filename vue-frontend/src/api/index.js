import axios from 'axios'
import { useAuthStore } from '@/store/auth.js'

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    // 401 을 조용히 넘기면 화면은 로그인 상태를 유지한 채 빈 목록을 보여 준다.
    // 사용자가 인증 만료를 "데이터 없음" 으로 오인하므로 세션을 정리하고 로그인으로 보낸다.
    if (err.response?.status === 401) {
      const auth = useAuthStore()
      if (auth.isAuthenticated) {
        auth.logout(false)
        // 순환 참조를 피하려고 라우터는 호출 시점에 가져온다.
        const { default: router } = await import('@/router/index.js')
        if (router.currentRoute.value.name !== 'Login') {
          router.replace({ name: 'Login', query: { expired: '1' } })
        }
      }
    }
    return Promise.reject(err)
  }
)

export default api

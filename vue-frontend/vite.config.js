import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    host: 'localhost',
    port: 3000,
    strictPort: true,
    proxy: {
      // 비밀번호 재설정은 인증 없이 호출하는 공개 엔드포인트다. 강의 제공 API Gateway 이미지가
      // 이 경로를 공개 허용 목록에 넣지 않아 8080 경유 시 401 이 난다(게이트웨이는 소스가 없어 수정 불가).
      // 데모에서는 user-service(8081)로 직접 프록시한다.
      // 운영 전환 시: 게이트웨이 SecurityConfig permitAll 에 /api/users/password/** 를 추가하면 이 규칙은 불필요.
      // 토큰 교환은 로그인 단계라 아직 액세스 토큰이 없다. 게이트웨이가 이 경로에도
      // 인증을 요구하므로(위 비밀번호 재설정과 같은 사정) user-service(8081)로 직접 프록시한다.
      '/api/users/token': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/api/users/password': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      // '/login' 은 프록시하지 않는다.
      // Vue 라우터에도 /login 이 있어, 프록시를 걸면 주소창 직접 입력이나 새로고침 때
      // auth-server 의 영문 로그인 폼이 대신 뜬다. 로그인은 store/auth.js 가
      // 브라우저를 auth-server 로 직접 보내므로 프록시가 필요 없다.
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      },
      '/userinfo': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
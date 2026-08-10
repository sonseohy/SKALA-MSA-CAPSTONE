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
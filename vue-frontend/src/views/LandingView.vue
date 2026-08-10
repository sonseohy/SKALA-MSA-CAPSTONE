<template>
  <div class="public-page">
    <header class="public-header">
      <router-link to="/" class="public-brand">
        <span class="brand-mark">SK</span>
        <span>LearnNexus HRD</span>
      </router-link>
      <nav>
        <router-link to="/login" class="btn btn-ghost">로그인</router-link>
        <router-link to="/login" class="btn btn-primary">시작하기</router-link>
      </nav>
    </header>

    <main class="public-hero">
      <section class="hero-copy">
        <span class="pill danger">AI Enterprise Learning</span>
        <h1>기업 교육 기획부터<br />과정·강사 매칭까지</h1>
        <p>
          사업계획과 현재 역량을 분석해 필요한 교육 프로그램을 추천하고,<br />
          교육 공급자 탐색부터 계약 신청, 만족도 조사까지 하나의 흐름으로 관리합니다.
        </p>
        <div class="hero-actions">
          <router-link to="/login" class="btn btn-primary">HRD 워크스페이스 시작</router-link>
          <router-link to="/courses" class="btn btn-ghost">카탈로그 보기</router-link>
        </div>
      </section>

      <!--
        공개 랜딩은 비로그인 상태라 게이트웨이가 /api/courses 를 401 로 막는다.
        실제 값을 가져올 수 없으므로 숫자 대신 제공 기능을 소개한다.
      -->
      <section class="hero-preview">
        <div v-for="step in steps" :key="step.tag" class="metric-card">
          <span>{{ step.tag }}</span>
          <strong>{{ step.title }}</strong>
          <small>{{ step.body }}</small>
        </div>
      </section>
    </main>

  </div>
</template>

<script setup>
const steps = [
  { tag: '01 · Needs', title: '사업계획 기반 교육 기획', body: '사업 방향과 보유 역량을 입력해 필요한 교육 분야를 도출' },
  { tag: '02 · Match', title: '교육 프로그램 · 공급자 탐색', body: '분야, 일정, 방식, 예상 교육비를 비교' },
  { tag: '03 · Operate', title: '계약 신청부터 만족도까지', body: '신청 상태와 교육 결과를 한 화면에서 관리' }
]

</script>

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
        <h1>기업 교육 기획부터 과정·강사 매칭까지</h1>
        <p>
          사업계획과 현재 역량을 분석해 필요한 교육 프로그램을 추천하고,
          교육 공급자 탐색부터 계약 신청, 만족도 조사까지 하나의 흐름으로 관리합니다.
        </p>
        <div class="hero-actions">
          <router-link to="/login" class="btn btn-primary">HRD 워크스페이스 시작</router-link>
          <router-link to="/courses" class="btn btn-ghost">카탈로그 보기</router-link>
        </div>
      </section>

      <section class="hero-preview">
        <div class="metric-card">
          <span>진행 중인 교육</span>
          <strong>12</strong>
          <small>Q3 Planning Cycle</small>
        </div>
        <div class="metric-card">
          <span>추천 교육</span>
          <strong>8</strong>
          <small>Needs 기반 후보</small>
        </div>
        <div class="metric-card">
          <span>평균 만족도</span>
          <strong>4.8</strong>
          <small>Active courses</small>
        </div>
      </section>
    </main>
  </div>
</template>

<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Provider Detail</p>
        <h1>{{ provider.name }}</h1>
        <p>{{ provider.type }} · {{ provider.specialty }}</p>
      </div>
      <router-link to="/courses" class="btn btn-primary">제공 과정 보기</router-link>
    </section>

    <section class="content-grid two">
      <article class="panel provider-profile">
        <div class="provider-avatar large">{{ provider.name.charAt(0) }}</div>
        <h2>공급자 소개</h2>
        <p>기업 환경에 맞춘 실무형 교육과 워크숍을 운영하는 교육 공급자입니다.</p>
        <div class="info-grid">
          <div><span>전문 분야</span><strong>{{ provider.specialty }}</strong></div>
          <div><span>교육 가능 지역</span><strong>{{ provider.region }}</strong></div>
          <div><span>기업교육 이력</span><strong>{{ provider.experience }}</strong></div>
          <div><span>평균 만족도</span><strong>{{ provider.satisfaction }}/5.0</strong></div>
        </div>
      </article>

      <article class="panel">
        <h2>Portfolio Highlights</h2>
        <ul class="curriculum-list">
          <li>대기업 직무 전환 교육 및 온보딩 프로그램 운영</li>
          <li>온라인/오프라인 혼합 교육 설계 가능</li>
          <li>교육 종료 후 만족도 리포트 제공</li>
        </ul>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { sampleProviders } from '@/utils/hrd.js'

const route = useRoute()
const provider = computed(() => sampleProviders.find(item => String(item.id) === String(route.params.id)) || sampleProviders[0])
</script>

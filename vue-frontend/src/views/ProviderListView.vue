<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Providers</p>
        <h1>교육 공급자</h1>
        <p>전문 분야와 경력, 운영 조건을 나란히 놓고 교육 공급자를 비교합니다.</p>
      </div>
      <router-link to="/courses" class="btn btn-primary">교육 카탈로그</router-link>
    </section>

    <section v-if="loading" class="provider-grid">
      <div v-for="i in 3" :key="i" class="panel skeleton-card"></div>
    </section>

    <section v-else-if="loadError" class="empty-panel">
      <h2>공급자 정보를 불러오지 못했습니다.</h2>
      <p>공급자 명단과 교육 프로그램 목록을 모두 조회하지 못했습니다. 잠시 후 다시 시도해 주세요.</p>
      <router-link to="/courses" class="btn btn-primary">카탈로그로 이동</router-link>
    </section>

    <section v-else-if="!providers.length" class="empty-panel">
      <h2>등록된 교육 공급자가 없습니다.</h2>
      <p>교육 공급자가 프로그램을 등록하면 이곳에 표시됩니다.</p>
      <router-link to="/courses" class="btn btn-primary">카탈로그로 이동</router-link>
    </section>

    <template v-else>
    <form class="list-toolbar" @submit.prevent="applySearch">
      <div class="toolbar-search">
        <label for="providerSearch">검색</label>
        <input id="providerSearch" v-model.trim="keyword" type="search" placeholder="공급자명, 전문 분야, 경력으로 검색" />
      </div>
      <button type="submit" class="btn btn-primary toolbar-search-btn">검색</button>
      <div class="toolbar-field">
        <label for="providerSort">정렬</label>
        <select id="providerSort" v-model="sortBy">
          <option value="PROGRAMS">프로그램 많은 순</option>
          <option value="NAME">이름 순</option>
        </select>
      </div>
    </form>

    <section v-if="!filteredProviders.length" class="empty-panel">
      <h2>조건에 맞는 공급자가 없습니다.</h2>
      <p>검색어를 바꿔 보세요.</p>
    </section>

    <section v-else class="provider-grid">
      <router-link
        v-for="provider in pager.items.value"
        :key="provider.id"
        :to="`/providers/${provider.id}`"
        class="panel provider-card-lg"
      >
        <header class="provider-card-head">
          <div class="provider-avatar" :style="{ background: provider.color }">{{ provider.initial }}</div>
          <div class="provider-card-title">
            <strong>{{ provider.name }}</strong>
            <!-- 명세 03_api_spec.md:142 의 "공급자 경력". 없으면 아래 요약과 겹치므로 비운다. -->
            <small v-if="provider.career">{{ provider.career }}</small>
          </div>
        </header>

        <div class="provider-chips">
          <span v-for="label in provider.categories" :key="label">{{ label }}</span>
        </div>

        <dl class="provider-facts">
          <div><dt>교육 방식</dt><dd>{{ provider.deliveryTypes }}</dd></div>
          <div><dt>교육 지역</dt><dd>{{ provider.regions }}</dd></div>
          <div><dt>난이도</dt><dd>{{ provider.difficulties }}</dd></div>
          <div><dt>교육 대상</dt><dd>{{ provider.audiences }}</dd></div>
        </dl>

        <footer class="provider-card-foot">
          <span>프로그램 {{ provider.programCount }}건</span>
          <strong>{{ provider.priceRange }}</strong>
        </footer>
      </router-link>
    </section>

    <PagerBar
      :page="pager.page.value"
      :total-pages="pager.totalPages.value"
      :total="pager.total.value"
      :range-start="pager.rangeStart.value"
      :range-end="pager.rangeEnd.value"
      :page-numbers="pager.pageNumbers.value"
      unit="곳"
      @go="pager.go"
    />
    </template>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import HrdLayout from '@/components/HrdLayout.vue'
import PagerBar from '@/components/PagerBar.vue'
import { courseApi } from '@/api/course.js'
import { userApi } from '@/api/auth.js'
import { usePagedList } from '@/composables/usePagedList.js'
import {
  categoryLabelMap,
  deliveryTypeLabel,
  difficultyLabel,
  avatarColor,
  avatarInitial,
  formatPrice,
  parseCourseDescription,
  unwrapListResponse
} from '@/utils/hrd.js'

const loading = ref(true)
const loadError = ref(false)
const providers = ref([])
const keyword = ref('')
const appliedKeyword = ref('')
const sortBy = ref('PROGRAMS')

// 검색어는 버튼(또는 Enter)을 눌렀을 때만 반영한다.
function applySearch() {
  appliedKeyword.value = keyword.value
}

const filteredProviders = computed(() => {
  const word = appliedKeyword.value.trim().toLowerCase()
  const matched = word
    ? providers.value.filter(provider => [provider.name, provider.career, ...provider.categories]
        .some(field => (field || '').toLowerCase().includes(word)))
    : providers.value

  const sorted = [...matched]
  if (sortBy.value === 'NAME') sorted.sort((a, b) => a.name.localeCompare(b.name, 'ko'))
  else sorted.sort((a, b) => b.programCount - a.programCount)
  return sorted
})

const pager = usePagedList(filteredProviders, 9)

const joinOr = (values, fallback) => (values.length ? values.join(', ') : fallback)

// 공급자 명단은 GET /api/users?role=INSTRUCTOR 로 받는다(프로그램을 아직 안 올린 공급자도 나온다).
// 비교 항목은 04_development_direction.md:68 이 "공급자 비교 정보"로 지정한
// deliveryType·region·targetAudience·difficulty 이며, 등록 프로그램에서 도출한다.
function summarize(id, courses) {
  const uniq = (fn) => [...new Set(courses.map(fn).filter(Boolean))]
  const prices = courses.map(c => Number(c.price)).filter(Number.isFinite)
  const career = courses.map(c => parseCourseDescription(c.description).providerCareer).find(Boolean)

  return {
    id,
    career,
    programCount: courses.length,
    categories: uniq(c => c.category).map(c => categoryLabelMap[c] || c),
    deliveryTypes: joinOr(uniq(c => c.deliveryType).map(deliveryTypeLabel), '협의'),
    regions: joinOr(uniq(c => c.region), '협의'),
    difficulties: joinOr(uniq(c => c.difficulty).map(difficultyLabel), '협의'),
    audiences: joinOr(uniq(c => c.targetAudience), '협의'),
    priceRange: prices.length
      ? (Math.min(...prices) === Math.max(...prices)
          ? formatPrice(prices[0])
          : `${formatPrice(Math.min(...prices))} ~ ${formatPrice(Math.max(...prices))}`)
      : '협의'
  }
}

onMounted(async () => {
  // 한쪽이 실패해도 나머지로 화면을 만든다. 이름만 없거나, 비교 정보만 없는 상태가 된다.
  const [userRes, courseRes] = await Promise.allSettled([
    userApi.getUsers({ role: 'INSTRUCTOR' }),
    courseApi.getAll()
  ])

  if (userRes.status === 'rejected') console.error('[ProviderList] 공급자 목록 조회 실패:', userRes.reason)
  if (courseRes.status === 'rejected') console.error('[ProviderList] 교육 목록 조회 실패:', courseRes.reason)

  if (userRes.status === 'rejected' && courseRes.status === 'rejected') {
    loadError.value = true
    loading.value = false
    return
  }

  const names = new Map()
  if (userRes.status === 'fulfilled') {
    for (const user of unwrapListResponse(userRes.value)) names.set(Number(user.id), user.name)
  }

  const byProvider = new Map([...names.keys()].map(id => [id, []]))
  if (courseRes.status === 'fulfilled') {
    for (const course of unwrapListResponse(courseRes.value)) {
      const id = Number(course.instructorId)
      if (!Number.isFinite(id)) continue
      if (!byProvider.has(id)) byProvider.set(id, [])
      byProvider.get(id).push(course)
    }
  }

  providers.value = [...byProvider.entries()].map(([id, list]) => {
    const name = names.get(id) || `공급자 #${id}`
    return { ...summarize(id, list), name, initial: avatarInitial(name), color: avatarColor(name) }
  }).sort((a, b) => b.programCount - a.programCount)

  loading.value = false
})
</script>

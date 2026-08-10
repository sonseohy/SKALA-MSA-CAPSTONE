<template>
  <HrdLayout>
    <section class="page-head">
      <div>
        <p class="eyebrow">Provider Detail</p>
        <h1>{{ loading ? '공급자 정보를 불러오는 중' : providerName }}</h1>
        <p>{{ subtitle }}</p>
      </div>
      <router-link to="/courses" class="btn btn-primary">제공 과정 보기</router-link>
    </section>

    <section v-if="notFound" class="empty-panel">
      <h2>공급자 정보를 찾지 못했습니다.</h2>
      <p>교육 프로그램 상세에서 공급자를 선택하면 해당 공급자의 정보를 볼 수 있습니다.</p>
      <router-link to="/courses" class="btn btn-primary">카탈로그로 이동</router-link>
    </section>

    <section v-else class="content-grid two">
      <article class="panel provider-profile">
        <div class="provider-avatar large" :style="{ background: avatarColor(providerName) }">{{ avatarInitial(providerName) }}</div>

        <div class="panel-title">
          <h2>공급자 소개</h2>
          <button v-if="isOwnProfile && !editing" type="button" class="btn btn-ghost" @click="startEditing">
            프로필 수정
          </button>
        </div>

        <template v-if="!editing">
          <p>{{ intro }}</p>
          <div class="info-grid">
            <div><span>계정 역할</span><strong>{{ roleText }}</strong></div>
            <div><span>등록 프로그램</span><strong>{{ programs.length }}건</strong></div>
            <div><span>주력 교육 분야</span><strong>{{ specialty }}</strong></div>
            <div><span>교육 가능 지역</span><strong>{{ regions }}</strong></div>
          </div>
          <p v-if="saveMessage" class="notice-box" :class="{ error: saveFailed }">{{ saveMessage }}</p>
        </template>

        <form v-else class="needs-form" @submit.prevent="handleSave">
          <p class="muted-note">
            계정 정보만 수정합니다. <strong>주력 분야·가능 지역·경력</strong>은 등록한 프로그램의 설명에서 가져오므로,
            프로그램 등록 화면에서 <code>커리큘럼:</code>·<code>공급자 경력:</code> 을 고쳐야 바뀝니다.
          </p>

          <div class="form-row">
            <label for="providerName">공급자명</label>
            <input id="providerName" v-model.trim="editForm.name" required maxlength="60" />
          </div>

          <div class="form-row">
            <label for="providerEmail">이메일</label>
            <input id="providerEmail" v-model.trim="editForm.email" type="email" required />
          </div>

          <div v-if="saveMessage" class="notice-box" :class="{ error: saveFailed }">{{ saveMessage }}</div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '저장 중...' : '저장' }}
            </button>
            <button type="button" class="btn btn-ghost" @click="cancelEditing">취소</button>
          </div>
        </form>
      </article>

      <article class="panel">
        <div class="panel-title">
          <h2>등록한 교육 프로그램</h2>
          <span class="pill">{{ programs.length }}건</span>
        </div>

        <div v-if="loading" class="skeleton-list">
          <div v-for="i in 3" :key="i" class="skeleton-line"></div>
        </div>

        <p v-else-if="!programs.length" class="task-empty">등록한 교육 프로그램이 없습니다.</p>

        <template v-else>
          <!-- 분야는 이 공급자가 실제로 등록한 것만 후보로 만든다 -->
          <div v-if="categories.length > 1" class="chip-row">
            <button
              v-for="cat in categories"
              :key="cat.value"
              type="button"
              class="filter-chip"
              :class="{ active: selectedCategory === cat.value }"
              @click="selectedCategory = cat.value"
            >
              {{ cat.label }}
            </button>
          </div>

          <p v-if="!filteredPrograms.length" class="task-empty">이 분야에 등록된 프로그램이 없습니다.</p>

          <div v-else class="compact-list">
            <router-link
              v-for="course in pager.items.value"
              :key="course.id"
              :to="`/courses/${course.id}`"
              class="compact-row"
            >
              <span class="program-token">{{ course.categoryShort }}</span>
              <div>
                <strong>{{ course.title }}</strong>
                <small>{{ course.categoryLabel }} · {{ course.scheduleLabel }}</small>
              </div>
            </router-link>
          </div>

          <PagerBar
            :page="pager.page.value"
            :total-pages="pager.totalPages.value"
            :total="pager.total.value"
            :range-start="pager.rangeStart.value"
            :range-end="pager.rangeEnd.value"
            :page-numbers="pager.pageNumbers.value"
            unit="건"
            @go="pager.go"
          />
        </template>
      </article>
    </section>
  </HrdLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import HrdLayout from '@/components/HrdLayout.vue'
import { courseApi } from '@/api/course.js'
import { userApi } from '@/api/auth.js'
import { useAuthStore } from '@/store/auth.js'
import PagerBar from '@/components/PagerBar.vue'
import { usePagedList } from '@/composables/usePagedList.js'
import {
  avatarColor,
  avatarInitial,
  isProviderRole,
  normalizeCategory,
  normalizeCourse,
  roleLabel,
  unwrapListResponse,
  unwrapObjectResponse
} from '@/utils/hrd.js'

const route = useRoute()
const auth = useAuthStore()
const loading = ref(true)
const notFound = ref(false)
const provider = ref(null)
const programs = ref([])
const selectedCategory = ref('ALL')

const providerId = computed(() => Number(route.params.id))

// 본인 프로필일 때만 수정 버튼을 보인다.
const isOwnProfile = computed(() => (
  isProviderRole(auth.user?.role) && Number(auth.user?.id) === providerId.value
))

const editing = ref(false)
const saving = ref(false)
const saveMessage = ref('')
const saveFailed = ref(false)
const editForm = reactive({ name: '', email: '' })

function startEditing() {
  editForm.name = provider.value?.name || ''
  editForm.email = provider.value?.email || ''
  saveMessage.value = ''
  saveFailed.value = false
  editing.value = true
}

function cancelEditing() {
  editing.value = false
  saveMessage.value = ''
  saveFailed.value = false
}

async function handleSave() {
  saving.value = true
  saveMessage.value = ''
  saveFailed.value = false
  try {
    const res = await userApi.update(providerId.value, { name: editForm.name, email: editForm.email })
    provider.value = unwrapObjectResponse(res) || { ...provider.value, ...editForm }
    // 상단바 이름도 함께 갱신한다.
    if (auth.user?.id === providerId.value) auth.setUser({ ...auth.user, ...editForm })
    editing.value = false
    saveMessage.value = '프로필을 저장했습니다.'
  } catch (error) {
    saveFailed.value = true
    if ([404, 405, 501].includes(error?.response?.status)) {
      saveMessage.value = '계정 수정 API가 아직 백엔드에 없습니다. 입력한 내용은 저장되지 않았습니다.'
    } else {
      saveMessage.value = error.response?.data?.message || '저장에 실패했습니다.'
      console.error('[ProviderDetail] 프로필 저장 실패:', error)
    }
  } finally {
    saving.value = false
  }
}

const categories = computed(() => {
  const found = [...new Set(programs.value.map(course => course.category).filter(Boolean))]
  return [
    { value: 'ALL', label: '전체' },
    ...found.map(value => ({ value, label: normalizeCategory(value) }))
  ]
})

const filteredPrograms = computed(() => (
  selectedCategory.value === 'ALL'
    ? programs.value
    : programs.value.filter(course => course.category === selectedCategory.value)
))

const pager = usePagedList(filteredPrograms, 8)

const providerName = computed(() => provider.value?.name || `공급자 #${providerId.value || '-'}`)
const roleText = computed(() => (provider.value ? roleLabel(provider.value.role) : '확인 불가'))

const subtitle = computed(() => {
  if (loading.value) return ''
  if (notFound.value) return ''
  return `${roleText.value} · 등록 프로그램 ${programs.value.length}건`
})

const intro = computed(() => (
  programs.value.length
    ? '이 공급자가 등록한 교육 프로그램은 아래 목록에서 확인할 수 있습니다.'
    : '아직 등록된 교육 프로그램이 없습니다.'
))

// 소개·전문분야는 별도 필드가 없으므로 등록 프로그램에서 도출한다.
const specialty = computed(() => {
  const labels = [...new Set(programs.value.map(c => c.categoryLabel).filter(Boolean))]
  return labels.length ? labels.join(', ') : '등록 정보 없음'
})
const regions = computed(() => {
  const list = [...new Set(programs.value.map(c => c.region).filter(Boolean))]
  return list.length ? list.join(', ') : '협의'
})

async function load() {
  loading.value = true
  notFound.value = false
  provider.value = null
  programs.value = []

  const id = providerId.value
  if (!Number.isFinite(id)) {
    notFound.value = true
    loading.value = false
    return
  }

  const [userRes, courseRes] = await Promise.allSettled([
    userApi.getById(id),
    courseApi.getAll()
  ])

  if (userRes.status === 'fulfilled') {
    provider.value = unwrapObjectResponse(userRes.value)
  } else {
    console.error('[ProviderDetail] 공급자 조회 실패:', userRes.reason)
  }

  if (courseRes.status === 'fulfilled') {
    programs.value = unwrapListResponse(courseRes.value)
      .filter(course => Number(course.instructorId) === id)
      .map(normalizeCourse)
  } else {
    console.error('[ProviderDetail] 프로그램 조회 실패:', courseRes.reason)
  }

  if (!provider.value && !programs.value.length) notFound.value = true
  loading.value = false
}

onMounted(load)
watch(() => route.params.id, load)
</script>

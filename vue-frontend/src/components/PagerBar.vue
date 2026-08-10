<template>
  <nav v-if="total" class="pager" aria-label="목록 페이지 이동">
    <div v-if="totalPages > 1" class="pager-buttons">
      <button type="button" :disabled="page === 1" @click="$emit('go', page - 1)">이전</button>

      <template v-for="(number, index) in pageNumbers" :key="index">
        <span v-if="number === null" class="pager-gap">…</span>
        <button
          v-else
          type="button"
          :class="{ active: number === page }"
          :aria-current="number === page ? 'page' : undefined"
          @click="$emit('go', number)"
        >
          {{ number }}
        </button>
      </template>

      <button type="button" :disabled="page === totalPages" @click="$emit('go', page + 1)">다음</button>
    </div>

    <p class="pager-count">
      전체 {{ total.toLocaleString() }}{{ unit }} 중 <strong>{{ rangeStart }}–{{ rangeEnd }}</strong> 표시
    </p>
  </nav>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  total: { type: Number, required: true },
  rangeStart: { type: Number, required: true },
  rangeEnd: { type: Number, required: true },
  pageNumbers: { type: Array, required: true },
  unit: { type: String, default: '건' }
})

defineEmits(['go'])
</script>

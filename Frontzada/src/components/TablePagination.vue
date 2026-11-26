<script setup>
import { computed } from 'vue'
import { ChevronLeft, ChevronRight } from 'lucide-vue-next'

// v-model:pagina → o componente pai controla a página atual
const props = defineProps({
  pagina: { type: Number, required: true },
  total: { type: Number, required: true },
  porPagina: { type: Number, required: true }
})
const emit = defineEmits(['update:pagina'])

const totalPaginas = computed(() => Math.max(1, Math.ceil(props.total / props.porPagina)))
const inicio = computed(() => (props.total ? (props.pagina - 1) * props.porPagina + 1 : 0))
const fim = computed(() => Math.min(props.pagina * props.porPagina, props.total))

function ir(p) {
  if (p >= 1 && p <= totalPaginas.value) emit('update:pagina', p)
}
</script>

<template>
  <div class="pagination">
    <span>Mostrando <strong class="mono">{{ inicio }}–{{ fim }}</strong> de <strong class="mono">{{ total }}</strong></span>
    <div class="pages">
      <button class="icon-btn" :disabled="pagina <= 1" aria-label="Página anterior" @click="ir(pagina - 1)">
        <ChevronLeft :size="16" />
      </button>
      <span class="atual mono">{{ pagina }} / {{ totalPaginas }}</span>
      <button class="icon-btn" :disabled="pagina >= totalPaginas" aria-label="Próxima página" @click="ir(pagina + 1)">
        <ChevronRight :size="16" />
      </button>
    </div>
  </div>
</template>

<style scoped>
.atual { display: inline-flex; align-items: center; padding: 0 10px; color: var(--text); }
.icon-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.icon-btn:disabled:hover { background: transparent; border-color: transparent; }
</style>

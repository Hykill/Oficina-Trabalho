<script setup>
defineProps({
  titulo: { type: String, required: true },
  valor: { type: [Number, String], default: null },
  descricao: { type: String, default: '' },
  icone: { type: [Object, Function], default: null },
  carregando: Boolean
})
</script>

<template>
  <div class="card stat">
    <div class="stat-top">
      <span class="stat-title">{{ titulo }}</span>
      <span v-if="icone" class="stat-icon"><component :is="icone" :size="18" /></span>
    </div>
    <span v-if="carregando" class="skeleton" style="width: 72px; height: 34px" />
    <span v-else class="stat-value mono">{{ valor ?? '—' }}</span>
    <span class="stat-desc">{{ descricao }}</span>
  </div>
</template>

<style scoped>
.stat {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  transition: border-color var(--transition), transform var(--transition);
}
.stat:hover { border-color: var(--border-strong); transform: translateY(-2px); }
.stat-top { display: flex; align-items: center; justify-content: space-between; }
.stat {
  position: relative;
  overflow: hidden;
  border-top: 3px solid var(--border-strong);
}
.stat-title {
  color: var(--text-muted);
  font-family: var(--font-titulo);
  font-weight: 600;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}
.stat-icon {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 6px;
  background: var(--primary-soft);
  border: 1px solid var(--border-strong);
  color: var(--primary-text);
}
/* Número grande estilo painel/display */
.stat-value { font-size: 38px; font-weight: 700; letter-spacing: 0.01em; line-height: 1.05; }
.stat-desc { font-size: 12px; color: var(--text-faint); }
</style>

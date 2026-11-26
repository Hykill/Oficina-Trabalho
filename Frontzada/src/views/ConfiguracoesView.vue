<script setup>
import { Moon, Sun, Check } from 'lucide-vue-next'
import { useSettings } from '@/composables/useSettings'

const { settings } = useSettings()

const temas = [
  { valor: 'escuro', nome: 'Escuro', descricao: 'Tons de preto e cinza', icone: Moon },
  { valor: 'claro', nome: 'Claro', descricao: 'Fundo branco e cinza claro', icone: Sun }
]
</script>

<template>
  <section>
    <div class="page-header">
      <div>
        <h1 class="page-title">Configurações</h1>
        <p class="page-subtitle">Aparência do sistema</p>
      </div>
    </div>

    <div class="card tema-card">
      <div class="card-header">
        <div>
          <h2 class="card-title">Tema</h2>
          <p class="card-subtitle">Escolha entre o modo escuro e o modo claro</p>
        </div>
      </div>
      <div class="card-body">
        <div class="opcoes" role="radiogroup" aria-label="Tema">
          <button
            v-for="t in temas"
            :key="t.valor"
            class="opcao"
            :class="{ ativa: settings.tema === t.valor }"
            role="radio"
            :aria-checked="settings.tema === t.valor"
            @click="settings.tema = t.valor"
          >
            <!-- Miniatura do tema -->
            <span class="preview" :class="t.valor">
              <span class="p-side" />
              <span class="p-main">
                <span class="p-bar" />
                <span class="p-card" />
                <span class="p-card curto" />
              </span>
            </span>
            <span class="opcao-info">
              <component :is="t.icone" :size="18" />
              <span>
                <strong>{{ t.nome }}</strong>
                <small>{{ t.descricao }}</small>
              </span>
              <span v-if="settings.tema === t.valor" class="check"><Check :size="14" /></span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.tema-card { max-width: 640px; }

.opcoes { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
@media (max-width: 520px) { .opcoes { grid-template-columns: 1fr; } }

.opcao {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 14px;
  text-align: left;
  background: var(--bg-elevated);
  border: 2px solid var(--border);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: border-color var(--transition), transform var(--transition);
}
.opcao:hover { border-color: var(--border-strong); transform: translateY(-2px); }
.opcao.ativa { border-color: var(--primary); }
.opcao:focus-visible { outline: none; box-shadow: 0 0 0 3px var(--primary-ring); }

.opcao-info { display: flex; align-items: center; gap: 10px; color: var(--text-muted); }
.opcao-info > span { display: flex; flex-direction: column; }
.opcao-info strong { color: var(--text); font-size: 14px; }
.opcao-info small { font-size: 12px; color: var(--text-faint); }
.check {
  display: grid !important;
  place-items: center;
  margin-left: auto;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--steel);
  color: var(--on-primary);
}

/* Miniaturas: cores fixas de propósito, para mostrar cada tema */
.preview { display: flex; height: 96px; border-radius: 8px; overflow: hidden; border: 1px solid var(--border); }
.p-side { width: 26%; }
.p-main { flex: 1; display: flex; flex-direction: column; gap: 6px; padding: 10px; }
.p-bar { height: 8px; width: 40%; border-radius: 2px; }
.p-card { flex: 1; border-radius: 3px; }
.p-card.curto { flex: 0.6; }
.p-side { border-top: 4px solid #e8a317; }

.preview.escuro { background: #111214; }
.preview.escuro .p-bar { background: linear-gradient(180deg, #e2e4e7, #9a9ea5); }
.preview.escuro .p-side { background: #17181b; border-right: 1px solid #2e3036; }
.preview.escuro .p-card { background: #1c1d21; border: 1px solid #2e3036; }

.preview.claro { background: #e7e8ea; }
.preview.claro .p-bar { background: linear-gradient(180deg, #6a6e77, #3c3f46); }
.preview.claro .p-side { background: #f5f5f6; border-right: 1px solid #d5d7db; }
.preview.claro .p-card { background: #ffffff; border: 1px solid #d5d7db; }
</style>

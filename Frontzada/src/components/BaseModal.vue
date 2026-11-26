<script setup>
import { watch, onBeforeUnmount } from 'vue'
import { X } from 'lucide-vue-next'

const props = defineProps({
  aberto: { type: Boolean, default: false },
  titulo: { type: String, default: '' },
  subtitulo: { type: String, default: '' },
  largura: { type: String, default: '560px' }
})
const emit = defineEmits(['fechar'])

// Fecha com a tecla ESC
function onKey(e) {
  if (e.key === 'Escape') emit('fechar')
}

watch(
  () => props.aberto,
  (aberto) => {
    document.body.style.overflow = aberto ? 'hidden' : ''
    if (aberto) window.addEventListener('keydown', onKey)
    else window.removeEventListener('keydown', onKey)
  }
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKey)
})
</script>

<template>
  <!-- Teleport: renderiza o modal direto no <body>, fora da hierarquia da página -->
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="aberto" class="overlay" @mousedown.self="emit('fechar')">
        <div class="modal" :style="{ maxWidth: largura }" role="dialog" aria-modal="true" :aria-label="titulo">
          <header class="modal-header">
            <div>
              <h2 class="modal-title">{{ titulo }}</h2>
              <p v-if="subtitulo" class="modal-subtitle">{{ subtitulo }}</p>
            </div>
            <button class="icon-btn" aria-label="Fechar" @click="emit('fechar')">
              <X :size="18" />
            </button>
          </header>

          <div class="modal-body">
            <slot />
          </div>

          <footer v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  padding: 20px;
  background: var(--overlay);
  backdrop-filter: blur(4px);
}
.modal {
  width: 100%;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
}
.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--border);
}
.modal-title { font-size: 17px; }
.modal-subtitle { color: var(--text-muted); font-size: 13px; margin-top: 2px; }
.modal-body { padding: 24px; overflow-y: auto; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
  background: var(--bg-elevated);
  border-radius: 0 0 var(--radius-lg) var(--radius-lg);
}

.modal-enter-active, .modal-leave-active { transition: opacity 200ms ease; }
.modal-enter-active .modal, .modal-leave-active .modal { transition: transform 200ms cubic-bezier(0.4, 0, 0.2, 1), opacity 200ms ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }
.modal-enter-from .modal, .modal-leave-to .modal { transform: translateY(12px) scale(0.98); opacity: 0; }
</style>

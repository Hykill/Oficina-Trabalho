<script setup>
import { CircleCheck, CircleX, Info, X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { toasts, remover } = useToast()
const icones = { success: CircleCheck, error: CircleX, info: Info }
</script>

<template>
  <div class="toasts" aria-live="polite">
    <TransitionGroup name="toast">
      <div v-for="t in toasts" :key="t.id" class="toast" :class="t.tipo">
        <component :is="icones[t.tipo]" :size="18" class="toast-icon" />
        <span class="toast-msg">{{ t.mensagem }}</span>
        <button class="toast-close" aria-label="Fechar" @click="remover(t.id)"><X :size="14" /></button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toasts {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 200;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: min(380px, calc(100vw - 40px));
}
.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  background: var(--surface-2);
  border: 1px solid var(--border-strong);
  border-left: 3px solid var(--primary);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}
.toast.success { border-left-color: var(--success); }
.toast.success .toast-icon { color: var(--success); }
.toast.error { border-left-color: var(--danger); }
.toast.error .toast-icon { color: var(--danger); }
.toast.info .toast-icon { color: var(--primary); }
.toast-icon { flex-shrink: 0; margin-top: 1px; }
.toast-msg { flex: 1; font-size: 13px; }
.toast-close { background: none; border: none; color: var(--text-faint); cursor: pointer; padding: 2px; }
.toast-close:hover { color: var(--text); }

.toast-enter-active, .toast-leave-active { transition: all 220ms ease; }
.toast-enter-from { opacity: 0; transform: translateX(24px); }
.toast-leave-to { opacity: 0; transform: translateX(24px); }
</style>

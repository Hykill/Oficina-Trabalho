<script setup>
import { TriangleAlert } from 'lucide-vue-next'
import BaseModal from './BaseModal.vue'

defineProps({
  aberto: Boolean,
  titulo: { type: String, default: 'Confirmar exclusão' },
  mensagem: { type: String, default: '' },
  textoConfirmar: { type: String, default: 'Excluir' },
  carregando: Boolean
})
defineEmits(['confirmar', 'cancelar'])
</script>

<template>
  <BaseModal :aberto="aberto" :titulo="titulo" largura="440px" @fechar="$emit('cancelar')">
    <div class="confirm">
      <span class="confirm-icon"><TriangleAlert :size="22" /></span>
      <p>
        <slot>{{ mensagem }}</slot>
      </p>
    </div>
    <template #footer>
      <button class="btn btn-secondary" :disabled="carregando" @click="$emit('cancelar')">Cancelar</button>
      <button class="btn btn-danger" :disabled="carregando" @click="$emit('confirmar')">
        <span v-if="carregando" class="spinner" />
        {{ textoConfirmar }}
      </button>
    </template>
  </BaseModal>
</template>

<style scoped>
.confirm { display: flex; gap: 16px; align-items: flex-start; }
.confirm p { color: var(--text-muted); padding-top: 8px; }
.confirm-icon {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--danger-soft);
  color: var(--danger);
}
</style>

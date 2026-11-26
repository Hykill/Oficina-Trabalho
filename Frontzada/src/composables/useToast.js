import { reactive } from 'vue'

// Estado GLOBAL: por estar fora da função, todos os componentes
// que chamam useToast() compartilham a mesma lista.
const toasts = reactive([])
let proximoId = 1

export function useToast() {
  function mostrar(mensagem, tipo = 'info', duracao = 4000) {
    const id = proximoId++
    toasts.push({ id, mensagem, tipo })
    setTimeout(() => remover(id), duracao)
  }
  function remover(id) {
    const i = toasts.findIndex((t) => t.id === id)
    if (i !== -1) toasts.splice(i, 1)
  }
  return {
    toasts,
    remover,
    sucesso: (m) => mostrar(m, 'success'),
    erro: (m) => mostrar(m, 'error', 6000),
    info: (m) => mostrar(m, 'info')
  }
}

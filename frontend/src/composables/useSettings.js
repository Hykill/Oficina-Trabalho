import { reactive, watch } from 'vue'

// Preferências salvas no navegador (localStorage). Não vão para o back-end.
const CHAVE = 'oficina:tema'

function temaInicial() {
  const salvo = localStorage.getItem(CHAVE)
  if (salvo === 'claro' || salvo === 'escuro') return salvo
  return 'escuro' // padrão do sistema
}

const settings = reactive({
  tema: temaInicial(),
  itensPorPagina: 10 // fixo: usado pelas tabelas
})

// O CSS troca as cores pelo atributo data-tema="claro" no <html>
function aplicarTema() {
  document.documentElement.dataset.tema = settings.tema
}

watch(
  () => settings.tema,
  (tema) => {
    localStorage.setItem(CHAVE, tema)
    aplicarTema()
  }
)

aplicarTema()

export function useSettings() {
  return { settings }
}

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Search, Pencil, RotateCcw, CircleCheck, WifiOff, ArrowUpDown, Wallet } from 'lucide-vue-next'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import TablePagination from '@/components/TablePagination.vue'
import StatCard from '@/components/StatCard.vue'
import conclusaoService from '@/services/conclusaoService'
import itemServicoService from '@/services/itemServicoService'
import { mensagemDeErro } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useSettings } from '@/composables/useSettings'
import { formatarMoeda, formatarPlaca, formatarDataHora, normalizarTexto, normalizarPlaca } from '@/utils/formatters'

const toast = useToast()
const { settings } = useSettings()

// ---------------- Dados ----------------
const conclusoes = ref([])
const itens = ref([]) // para calcular o total (mão de obra + peças)
const carregando = ref(true)
const erroCarregar = ref('')

async function carregar() {
  carregando.value = true
  erroCarregar.value = ''
  try {
    const [c, i] = await Promise.all([conclusaoService.listar(), itemServicoService.listar()])
    conclusoes.value = c
    itens.value = i
  } catch (e) {
    erroCarregar.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

const totalDe = (c) =>
  (c.maoDeObra || 0) +
  itens.value.filter((i) => i.servicoId === c.servicoId).reduce((s, i) => s + (i.pecaPreco || 0) * (i.quantidade || 0), 0)

const valorTotalProntos = computed(() => conclusoes.value.reduce((s, c) => s + totalDe(c), 0))

// ---------------- Pesquisa, ordenação e paginação ----------------
const busca = ref('')
const ordenacao = reactive({ campo: 'dataConclusao', direcao: 'desc' })
const pagina = ref(1)

const filtradas = computed(() => {
  const termo = normalizarTexto(busca.value.trim())
  const termoPlaca = normalizarPlaca(busca.value)
  let lista = conclusoes.value.map((c) => ({ ...c, total: totalDe(c) }))
  if (termo) {
    lista = lista.filter(
      (c) =>
        String(c.servicoId) === termo.replace('#', '') ||
        (termoPlaca && normalizarPlaca(c.carroPlaca).includes(termoPlaca)) ||
        normalizarTexto(c.carroModelo).includes(termo) ||
        normalizarTexto(c.clienteNome).includes(termo) ||
        normalizarTexto(c.observacao).includes(termo)
    )
  }
  const { campo, direcao } = ordenacao
  const fator = direcao === 'asc' ? 1 : -1
  return lista.sort((a, b) => {
    const va = a[campo] ?? ''
    const vb = b[campo] ?? ''
    return (typeof va === 'number' ? va - vb : String(va).localeCompare(String(vb), 'pt-BR')) * fator
  })
})

const paginadas = computed(() => {
  const ini = (pagina.value - 1) * settings.itensPorPagina
  return filtradas.value.slice(ini, ini + settings.itensPorPagina)
})

watch(busca, () => (pagina.value = 1))
watch(filtradas, (lista) => {
  const max = Math.max(1, Math.ceil(lista.length / settings.itensPorPagina))
  if (pagina.value > max) pagina.value = max
})

function ordenarPor(campo) {
  if (ordenacao.campo === campo) ordenacao.direcao = ordenacao.direcao === 'asc' ? 'desc' : 'asc'
  else Object.assign(ordenacao, { campo, direcao: campo === 'dataConclusao' ? 'desc' : 'asc' })
}

// ---------------- Editar observação ----------------
const editando = ref(null)
const obsEdicao = ref('')
const salvando = ref(false)

function abrirEdicao(c) {
  editando.value = c
  obsEdicao.value = c.observacao ?? ''
}

async function salvarEdicao() {
  salvando.value = true
  try {
    const atualizada = await conclusaoService.atualizar(editando.value.id, {
      servicoId: editando.value.servicoId,
      observacao: obsEdicao.value.trim() || null
    })
    const i = conclusoes.value.findIndex((c) => c.id === atualizada.id)
    if (i !== -1) conclusoes.value[i] = atualizada
    toast.sucesso('Observação atualizada.')
    editando.value = null
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    salvando.value = false
  }
}

// ---------------- Reabrir ----------------
const reabrindo = ref(null)
const processandoReabertura = ref(false)

async function confirmarReabertura() {
  processandoReabertura.value = true
  try {
    await conclusaoService.reabrir(reabrindo.value.id)
    conclusoes.value = conclusoes.value.filter((c) => c.id !== reabrindo.value.id)
    toast.sucesso(`Serviço #${reabrindo.value.servicoId} reaberto.`)
    reabrindo.value = null
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    processandoReabertura.value = false
  }
}
</script>

<template>
  <section>
    <div class="page-header">
      <div>
        <h1 class="page-title">Serviços prontos</h1>
        <p class="page-subtitle">Serviços concluídos e prontos para entrega</p>
      </div>
      <RouterLink to="/servicos" class="btn btn-secondary">Ver todos os serviços</RouterLink>
    </div>

    <div v-if="!erroCarregar" class="stats">
      <StatCard titulo="Serviços prontos" :valor="conclusoes.length" descricao="Total marcado como pronto" :icone="CircleCheck" :carregando="carregando" />
      <StatCard titulo="Valor dos prontos" :valor="formatarMoeda(valorTotalProntos)" descricao="Mão de obra + peças" :icone="Wallet" :carregando="carregando" />
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search">
          <Search :size="16" />
          <input v-model="busca" class="input" type="search" placeholder="Pesquisar por nº, placa, modelo, cliente..." aria-label="Pesquisar serviços prontos" />
        </div>
        <span v-if="!carregando && !erroCarregar" class="badge">
          {{ filtradas.length }} {{ filtradas.length === 1 ? 'serviço' : 'serviços' }}
        </span>
      </div>

      <div v-if="erroCarregar" class="empty error">
        <span class="empty-icon"><WifiOff :size="24" /></span>
        <h3>Erro ao carregar serviços prontos</h3>
        <p>{{ erroCarregar }}</p>
        <button class="btn btn-primary" style="margin-top: 8px" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="carregando" class="card-body skel-list">
        <span v-for="n in 4" :key="n" class="skeleton" style="height: 40px" />
      </div>

      <div v-else-if="!filtradas.length" class="empty">
        <span class="empty-icon"><CircleCheck :size="24" /></span>
        <template v-if="busca">
          <h3>Nenhum resultado</h3>
          <p>Nenhum serviço pronto corresponde a "{{ busca }}".</p>
        </template>
        <template v-else>
          <h3>Nenhum serviço pronto ainda</h3>
          <p>Na página Serviços, use o botão <CircleCheck :size="14" style="vertical-align: -2px" /> para marcar um serviço como pronto.</p>
          <RouterLink to="/servicos" class="btn btn-primary" style="margin-top: 8px">Ir para Serviços</RouterLink>
        </template>
      </div>

      <template v-else>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="th-sort" @click="ordenarPor('servicoId')">Serviço <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('carroPlaca')">Veículo <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('clienteNome')">Cliente <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('dataConclusao')">Pronto em <ArrowUpDown :size="12" /></th>
                <th>Observação</th>
                <th class="th-sort num" @click="ordenarPor('total')">Total <ArrowUpDown :size="12" /></th>
                <th style="text-align: right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in paginadas" :key="c.id">
                <td><span class="status pronto"><CircleCheck :size="13" /> #{{ c.servicoId }}</span></td>
                <td>
                  <div class="veiculo">
                    <span class="plate">{{ formatarPlaca(c.carroPlaca) }}</span>
                    <span class="name">{{ c.carroModelo }}</span>
                  </div>
                </td>
                <td class="muted">{{ c.clienteNome }}</td>
                <td class="mono muted">{{ formatarDataHora(c.dataConclusao) }}</td>
                <td class="muted obs" :title="c.observacao">{{ c.observacao || '—' }}</td>
                <td class="mono num total">{{ formatarMoeda(c.total) }}</td>
                <td>
                  <div class="actions">
                    <button class="icon-btn" title="Editar observação" aria-label="Editar observação" @click="abrirEdicao(c)"><Pencil :size="16" /></button>
                    <button class="icon-btn" title="Reabrir serviço" aria-label="Reabrir serviço" @click="reabrindo = c"><RotateCcw :size="16" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <TablePagination v-model:pagina="pagina" :total="filtradas.length" :por-pagina="settings.itensPorPagina" />
      </template>
    </div>

    <!-- Editar observação -->
    <BaseModal
      :aberto="!!editando"
      titulo="Editar observação da entrega"
      :subtitulo="editando ? `Serviço #${editando.servicoId} · pronto em ${formatarDataHora(editando.dataConclusao)}` : ''"
      largura="480px"
      @fechar="editando = null"
    >
      <div class="field">
        <label for="obs-edicao">Observação</label>
        <textarea id="obs-edicao" v-model="obsEdicao" class="input" rows="3" placeholder="Ex.: Cliente retirou o veículo" />
      </div>
      <template #footer>
        <button class="btn btn-secondary" :disabled="salvando" @click="editando = null">Cancelar</button>
        <button class="btn btn-primary" :disabled="salvando" @click="salvarEdicao">
          <span v-if="salvando" class="spinner" /> Salvar
        </button>
      </template>
    </BaseModal>

    <ConfirmDialog
      :aberto="!!reabrindo"
      :carregando="processandoReabertura"
      titulo="Reabrir serviço"
      texto-confirmar="Reabrir"
      @cancelar="reabrindo = null"
      @confirmar="confirmarReabertura"
    >
      O serviço <strong style="color: var(--text)">#{{ reabrindo?.servicoId }}</strong> ({{ reabrindo?.carroModelo }})
      voltará para "Em andamento" e sairá desta lista.
    </ConfirmDialog>
  </section>
</template>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-bottom: 20px; max-width: 640px; }
@media (max-width: 520px) { .stats { grid-template-columns: 1fr; } }
.skel-list { display: flex; flex-direction: column; gap: 12px; }
.th-sort svg { vertical-align: -1px; margin-left: 2px; opacity: 0.6; }
.num { text-align: right !important; }
.total { font-weight: 600; }
.obs { max-width: 240px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.veiculo { display: flex; align-items: center; gap: 12px; }
.veiculo .name { font-weight: 500; }
</style>

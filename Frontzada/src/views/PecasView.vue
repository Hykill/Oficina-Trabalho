<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Plus, Search, Pencil, Trash2, Package, WifiOff, ArrowUpDown } from 'lucide-vue-next'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import TablePagination from '@/components/TablePagination.vue'
import pecaService from '@/services/pecaService'
import { mensagemDeErro } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useSettings } from '@/composables/useSettings'
import { formatarMoeda, lerMoeda, moedaParaInput, normalizarTexto } from '@/utils/formatters'
import { validarPeca } from '@/utils/validators'

const toast = useToast()
const { settings } = useSettings()

// ---------------- Listagem ----------------
const pecas = ref([])
const carregando = ref(true)
const erroCarregar = ref('')

async function carregar() {
  carregando.value = true
  erroCarregar.value = ''
  try {
    pecas.value = await pecaService.listar()
  } catch (e) {
    erroCarregar.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

// ---------------- Pesquisa, ordenação e paginação ----------------
const busca = ref('')
const ordenacao = reactive({ campo: 'nome', direcao: 'asc' })
const pagina = ref(1)

const filtradas = computed(() => {
  const termo = normalizarTexto(busca.value.trim())
  let lista = pecas.value
  if (termo) {
    lista = lista.filter((p) => normalizarTexto(p.nome).includes(termo) || normalizarTexto(p.fabricante).includes(termo))
  }
  const { campo, direcao } = ordenacao
  const fator = direcao === 'asc' ? 1 : -1
  return [...lista].sort((a, b) => {
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
  else Object.assign(ordenacao, { campo, direcao: 'asc' })
}

// ---------------- Formulário ----------------
const formAberto = ref(false)
const editandoId = ref(null)
const salvando = ref(false)
const form = reactive({ nome: '', fabricante: '', preco: '' })
const erros = ref({})
const tentouEnviar = ref(false)

function abrirNovo() {
  editandoId.value = null
  Object.assign(form, { nome: '', fabricante: '', preco: '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

function abrirEdicao(p) {
  editandoId.value = p.id
  Object.assign(form, { nome: p.nome ?? '', fabricante: p.fabricante ?? '', preco: moedaParaInput(p.preco) })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

watch(form, () => {
  if (tentouEnviar.value) erros.value = validarPeca(form)
})

async function salvar() {
  tentouEnviar.value = true
  erros.value = validarPeca(form)
  if (Object.keys(erros.value).length) return

  const payload = { nome: form.nome.trim(), fabricante: form.fabricante.trim(), preco: lerMoeda(form.preco) }

  salvando.value = true
  try {
    if (editandoId.value) {
      const atualizada = await pecaService.atualizar(editandoId.value, { id: editandoId.value, ...payload })
      const i = pecas.value.findIndex((p) => p.id === editandoId.value)
      if (i !== -1) pecas.value[i] = atualizada
      toast.sucesso('Peça atualizada com sucesso.')
    } else {
      pecas.value.push(await pecaService.cadastrar(payload))
      toast.sucesso('Peça cadastrada com sucesso.')
    }
    formAberto.value = false
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    salvando.value = false
  }
}

// ---------------- Excluir ----------------
const excluindo = ref(null)
const processandoExclusao = ref(false)

async function confirmarExclusao() {
  processandoExclusao.value = true
  try {
    await pecaService.excluir(excluindo.value.id)
    pecas.value = pecas.value.filter((p) => p.id !== excluindo.value.id)
    toast.sucesso('Peça excluída.')
    excluindo.value = null
  } catch (e) {
    // Ex.: 409 "Não é possível excluir: a peça está sendo usada em um serviço"
    toast.erro(mensagemDeErro(e))
  } finally {
    processandoExclusao.value = false
  }
}
</script>

<template>
  <section>
    <div class="page-header">
      <div>
        <h1 class="page-title">Peças</h1>
        <p class="page-subtitle">Catálogo de peças e preços</p>
      </div>
      <button class="btn btn-primary" @click="abrirNovo"><Plus :size="18" /> Nova peça</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search">
          <Search :size="16" />
          <input v-model="busca" class="input" type="search" placeholder="Pesquisar por nome ou fabricante..." aria-label="Pesquisar peças" />
        </div>
        <span v-if="!carregando && !erroCarregar" class="badge">
          {{ filtradas.length }} {{ filtradas.length === 1 ? 'peça' : 'peças' }}
        </span>
      </div>

      <div v-if="erroCarregar" class="empty error">
        <span class="empty-icon"><WifiOff :size="24" /></span>
        <h3>Erro ao carregar peças</h3>
        <p>{{ erroCarregar }}</p>
        <button class="btn btn-primary" style="margin-top: 8px" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="carregando" class="card-body skel-list">
        <span v-for="n in 5" :key="n" class="skeleton" style="height: 40px" />
      </div>

      <div v-else-if="!filtradas.length" class="empty">
        <span class="empty-icon"><Package :size="24" /></span>
        <template v-if="busca">
          <h3>Nenhum resultado</h3>
          <p>Nenhuma peça corresponde a "{{ busca }}".</p>
        </template>
        <template v-else>
          <h3>Nenhuma peça cadastrada</h3>
          <p>Cadastre as peças para usá-las nos serviços.</p>
          <button class="btn btn-primary" style="margin-top: 8px" @click="abrirNovo"><Plus :size="16" /> Nova peça</button>
        </template>
      </div>

      <template v-else>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="th-sort" @click="ordenarPor('id')">ID <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('nome')">Peça <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('fabricante')">Fabricante <ArrowUpDown :size="12" /></th>
                <th class="th-sort" style="text-align: right" @click="ordenarPor('preco')">Preço <ArrowUpDown :size="12" /></th>
                <th style="text-align: right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in paginadas" :key="p.id">
                <td class="faint mono">#{{ p.id }}</td>
                <td>
                  <div class="cell-user">
                    <span class="avatar quadrado"><Package :size="16" /></span>
                    <span class="name">{{ p.nome }}</span>
                  </div>
                </td>
                <td><span class="badge">{{ p.fabricante }}</span></td>
                <td class="mono" style="text-align: right; font-weight: 600">{{ formatarMoeda(p.preco) }}</td>
                <td>
                  <div class="actions">
                    <button class="icon-btn" title="Editar" aria-label="Editar" @click="abrirEdicao(p)"><Pencil :size="16" /></button>
                    <button class="icon-btn danger" title="Excluir" aria-label="Excluir" @click="excluindo = p"><Trash2 :size="16" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <TablePagination v-model:pagina="pagina" :total="filtradas.length" :por-pagina="settings.itensPorPagina" />
      </template>
    </div>

    <BaseModal
      :aberto="formAberto"
      :titulo="editandoId ? 'Editar peça' : 'Nova peça'"
      :subtitulo="editandoId ? `Peça #${editandoId}` : 'Preencha os dados da peça'"
      largura="500px"
      @fechar="formAberto = false"
    >
      <form id="form-peca" class="form-grid" novalidate @submit.prevent="salvar">
        <div class="field full">
          <label for="nome">Nome da peça <span class="req">*</span></label>
          <input id="nome" v-model="form.nome" class="input" :class="{ invalid: erros.nome }" placeholder="Ex.: Pastilha de freio dianteira" />
          <span v-if="erros.nome" class="field-error">{{ erros.nome }}</span>
        </div>
        <div class="field">
          <label for="fabricante">Fabricante <span class="req">*</span></label>
          <input id="fabricante" v-model="form.fabricante" class="input" :class="{ invalid: erros.fabricante }" placeholder="Ex.: Bosch" />
          <span v-if="erros.fabricante" class="field-error">{{ erros.fabricante }}</span>
        </div>
        <div class="field">
          <label for="preco">Preço (R$) <span class="req">*</span></label>
          <div class="input-moeda">
            <span>R$</span>
            <input id="preco" v-model="form.preco" class="input mono" :class="{ invalid: erros.preco }" placeholder="0,00" inputmode="decimal" />
          </div>
          <span v-if="erros.preco" class="field-error">{{ erros.preco }}</span>
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" :disabled="salvando" @click="formAberto = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="form-peca" :disabled="salvando">
          <span v-if="salvando" class="spinner" />
          {{ editandoId ? 'Salvar alterações' : 'Cadastrar' }}
        </button>
      </template>
    </BaseModal>

    <ConfirmDialog
      :aberto="!!excluindo"
      :carregando="processandoExclusao"
      titulo="Excluir peça"
      @cancelar="excluindo = null"
      @confirmar="confirmarExclusao"
    >
      Tem certeza que deseja excluir <strong style="color: var(--text)">{{ excluindo?.nome }}</strong>?
      Peças usadas em algum serviço não podem ser excluídas.
    </ConfirmDialog>
  </section>
</template>

<style scoped>
.skel-list { display: flex; flex-direction: column; gap: 12px; }
.th-sort svg { vertical-align: -1px; margin-left: 2px; opacity: 0.6; }
.avatar.quadrado { border-radius: 9px; }
</style>

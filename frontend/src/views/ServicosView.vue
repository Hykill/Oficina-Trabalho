<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Plus, Search, Pencil, Trash2, ClipboardList, Wrench, WifiOff, ArrowUpDown, Package, User, Car, X, CircleCheck, RotateCcw } from 'lucide-vue-next'
import conclusaoService from '@/services/conclusaoService'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import TablePagination from '@/components/TablePagination.vue'
import servicoService from '@/services/servicoService'
import itemServicoService from '@/services/itemServicoService'
import carroService from '@/services/carroService'
import pecaService from '@/services/pecaService'
import { mensagemDeErro } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useSettings } from '@/composables/useSettings'
import { formatarMoeda, formatarPlaca, formatarDataHora, lerMoeda, moedaParaInput, normalizarTexto, normalizarPlaca } from '@/utils/formatters'
import { validarServico, validarItem } from '@/utils/validators'

const toast = useToast()
const { settings } = useSettings()

// ---------------- Dados ----------------
const servicos = ref([])
const itens = ref([]) // todos os itens de serviço (filtramos por servicoId)
const carros = ref([])
const pecas = ref([])
const conclusoes = ref([]) // serviços marcados como pronto
const carregando = ref(true)
const erroCarregar = ref('')

async function carregar() {
  carregando.value = true
  erroCarregar.value = ''
  try {
    const [s, i, c, p, cs] = await Promise.all([
      servicoService.listar(),
      itemServicoService.listar(),
      carroService.listar(),
      pecaService.listar(),
      conclusaoService.listar()
    ])
    servicos.value = s
    itens.value = i
    conclusoes.value = cs
    carros.value = [...c].sort((a, b) => a.placa.localeCompare(b.placa))
    pecas.value = [...p].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
  } catch (e) {
    erroCarregar.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

// ---------------- Cálculos ----------------
const itensDoServico = (servicoId) => itens.value.filter((i) => i.servicoId === servicoId)
const subtotal = (item) => (item.pecaPreco || 0) * (item.quantidade || 0)
const totalPecas = (servicoId) => itensDoServico(servicoId).reduce((s, i) => s + subtotal(i), 0)
const totalServico = (s) => (s.maoDeObra || 0) + totalPecas(s.id)
const carroPorId = (id) => carros.value.find((c) => c.id === id)
const conclusaoDo = (servicoId) => conclusoes.value.find((c) => c.servicoId === servicoId)

// ---------------- Pesquisa, ordenação e paginação ----------------
const busca = ref('')
const ordenacao = reactive({ campo: 'id', direcao: 'desc' })
const pagina = ref(1)

const filtrados = computed(() => {
  const termo = normalizarTexto(busca.value.trim())
  const termoPlaca = normalizarPlaca(busca.value)
  let lista = servicos.value.map((s) => ({
    ...s,
    total: totalServico(s),
    qtdItens: itensDoServico(s.id).length,
    pronto: conclusaoDo(s.id) ? 1 : 0 // número para poder ordenar pela coluna
  }))
  if (termo) {
    lista = lista.filter(
      (s) =>
        String(s.id) === termo.replace('#', '') ||
        (termoPlaca && normalizarPlaca(s.carroPlaca).includes(termoPlaca)) ||
        normalizarTexto(s.carroModelo).includes(termo) ||
        normalizarTexto(s.observacao).includes(termo) ||
        normalizarTexto(carroPorId(s.carroId)?.clienteNome).includes(termo)
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

const paginados = computed(() => {
  const ini = (pagina.value - 1) * settings.itensPorPagina
  return filtrados.value.slice(ini, ini + settings.itensPorPagina)
})

watch(busca, () => (pagina.value = 1))
watch(filtrados, (lista) => {
  const max = Math.max(1, Math.ceil(lista.length / settings.itensPorPagina))
  if (pagina.value > max) pagina.value = max
})

function ordenarPor(campo) {
  if (ordenacao.campo === campo) ordenacao.direcao = ordenacao.direcao === 'asc' ? 'desc' : 'asc'
  else Object.assign(ordenacao, { campo, direcao: 'asc' })
}

// ---------------- Formulário do serviço ----------------
const formAberto = ref(false)
const editandoId = ref(null)
const salvando = ref(false)
const form = reactive({ carroId: '', maoDeObra: '', observacao: '' })
const erros = ref({})
const tentouEnviar = ref(false)

function abrirNovo() {
  editandoId.value = null
  Object.assign(form, { carroId: '', maoDeObra: '', observacao: '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

function abrirEdicao(s) {
  editandoId.value = s.id
  Object.assign(form, { carroId: s.carroId ?? '', maoDeObra: moedaParaInput(s.maoDeObra), observacao: s.observacao ?? '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

watch(form, () => {
  if (tentouEnviar.value) erros.value = validarServico(form)
})

async function salvar() {
  tentouEnviar.value = true
  erros.value = validarServico(form)
  if (Object.keys(erros.value).length) return

  // Exatamente os campos do ServicoDTO
  const payload = {
    maoDeObra: lerMoeda(form.maoDeObra),
    observacao: form.observacao.trim() || null,
    carroId: Number(form.carroId)
  }

  salvando.value = true
  try {
    if (editandoId.value) {
      const atualizado = await servicoService.atualizar(editandoId.value, payload)
      const i = servicos.value.findIndex((s) => s.id === editandoId.value)
      if (i !== -1) servicos.value[i] = atualizado
      toast.sucesso('Serviço atualizado com sucesso.')
      formAberto.value = false
    } else {
      const criado = await servicoService.cadastrar(payload)
      servicos.value.push(criado)
      toast.sucesso('Serviço aberto! Agora adicione as peças utilizadas.')
      formAberto.value = false
      abrirOrdem(criado) // já abre a OS para lançar os itens
    }
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    salvando.value = false
  }
}

// ---------------- Ordem de serviço (itens) ----------------
const ordemId = ref(null)
const ordem = computed(() => servicos.value.find((s) => s.id === ordemId.value) || null)
const itensDaOrdem = computed(() => (ordem.value ? itensDoServico(ordem.value.id) : []))
const carroDaOrdem = computed(() => (ordem.value ? carroPorId(ordem.value.carroId) : null))

const novoItem = reactive({ pecaId: '', quantidade: 1 })
const errosItem = ref({})
const adicionandoItem = ref(false)
const itemProcessando = ref(null) // id do item sendo alterado/removido

const pecaSelecionada = computed(() => pecas.value.find((p) => p.id === novoItem.pecaId))

function abrirOrdem(s) {
  ordemId.value = s.id
  Object.assign(novoItem, { pecaId: '', quantidade: 1 })
  errosItem.value = {}
}

async function adicionarItem() {
  errosItem.value = validarItem(novoItem)
  if (Object.keys(errosItem.value).length) return

  const qtd = Number(novoItem.quantidade)
  const existente = itensDaOrdem.value.find((i) => i.pecaId === novoItem.pecaId)

  adicionandoItem.value = true
  try {
    if (existente) {
      // A peça já está na OS: soma a quantidade em vez de duplicar a linha
      await atualizarQuantidade(existente, existente.quantidade + qtd, false)
      toast.sucesso(`Quantidade de "${existente.pecaNome}" atualizada.`)
    } else {
      const criado = await itemServicoService.cadastrar({ quantidade: qtd, servicoId: ordem.value.id, pecaId: novoItem.pecaId })
      itens.value.push(criado)
      toast.sucesso('Peça adicionada ao serviço.')
    }
    Object.assign(novoItem, { pecaId: '', quantidade: 1 })
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    adicionandoItem.value = false
  }
}

async function atualizarQuantidade(item, novaQtd, avisar = true) {
  const qtd = Number(novaQtd)
  if (!Number.isInteger(qtd) || qtd <= 0) {
    toast.erro('Quantidade deve ser um inteiro maior que zero.')
    return
  }
  if (qtd === item.quantidade) return
  itemProcessando.value = item.id
  try {
    const atualizado = await itemServicoService.atualizar(item.id, { quantidade: qtd, servicoId: item.servicoId, pecaId: item.pecaId })
    const i = itens.value.findIndex((x) => x.id === item.id)
    if (i !== -1) itens.value[i] = atualizado
    if (avisar) toast.sucesso('Quantidade atualizada.')
  } catch (e) {
    if (!avisar) throw e
    toast.erro(mensagemDeErro(e))
  } finally {
    itemProcessando.value = null
  }
}

function onQuantidade(item, e) {
  const valor = e.target.value
  if (!Number.isInteger(Number(valor)) || Number(valor) <= 0) e.target.value = item.quantidade // desfaz
  atualizarQuantidade(item, valor)
}

async function removerItem(item) {
  itemProcessando.value = item.id
  try {
    await itemServicoService.excluir(item.id)
    itens.value = itens.value.filter((i) => i.id !== item.id)
    toast.sucesso(`"${item.pecaNome}" removida do serviço.`)
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    itemProcessando.value = null
  }
}

// ---------------- Marcar como pronto / reabrir ----------------
const concluindo = ref(null) // serviço que está sendo marcado como pronto
const obsConclusao = ref('')
const processandoConclusao = ref(false)

function abrirConcluir(s) {
  concluindo.value = s
  obsConclusao.value = ''
}

async function confirmarConclusao() {
  processandoConclusao.value = true
  try {
    const criada = await conclusaoService.concluir({
      servicoId: concluindo.value.id,
      observacao: obsConclusao.value.trim() || null
    })
    conclusoes.value.push(criada)
    toast.sucesso(`Serviço #${concluindo.value.id} marcado como pronto.`)
    concluindo.value = null
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    processandoConclusao.value = false
  }
}

const reabrindo = ref(null) // serviço que está sendo reaberto
const processandoReabertura = ref(false)

async function confirmarReabertura() {
  const conclusao = conclusaoDo(reabrindo.value.id)
  processandoReabertura.value = true
  try {
    await conclusaoService.reabrir(conclusao.id)
    conclusoes.value = conclusoes.value.filter((c) => c.id !== conclusao.id)
    toast.sucesso(`Serviço #${reabrindo.value.id} reaberto.`)
    reabrindo.value = null
  } catch (e) {
    toast.erro(mensagemDeErro(e))
  } finally {
    processandoReabertura.value = false
  }
}

// ---------------- Excluir serviço ----------------
const excluindo = ref(null)
const processandoExclusao = ref(false)

async function confirmarExclusao() {
  processandoExclusao.value = true
  try {
    await servicoService.excluir(excluindo.value.id)
    servicos.value = servicos.value.filter((s) => s.id !== excluindo.value.id)
    toast.sucesso('Serviço excluído.')
    excluindo.value = null
  } catch (e) {
    // Ex.: 409 "Não é possível excluir: o serviço possui itens cadastrados"
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
        <h1 class="page-title">Serviços</h1>
        <p class="page-subtitle">Ordens de serviço, peças utilizadas e valores</p>
      </div>
      <button class="btn btn-primary" @click="abrirNovo"><Plus :size="18" /> Novo serviço</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search">
          <Search :size="16" />
          <input v-model="busca" class="input" type="search" placeholder="Pesquisar por nº, placa, modelo, cliente ou observação..." aria-label="Pesquisar serviços" />
        </div>
        <span v-if="!carregando && !erroCarregar" class="badge">
          {{ filtrados.length }} {{ filtrados.length === 1 ? 'serviço' : 'serviços' }}
        </span>
      </div>

      <div v-if="erroCarregar" class="empty error">
        <span class="empty-icon"><WifiOff :size="24" /></span>
        <h3>Erro ao carregar serviços</h3>
        <p>{{ erroCarregar }}</p>
        <button class="btn btn-primary" style="margin-top: 8px" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="carregando" class="card-body skel-list">
        <span v-for="n in 5" :key="n" class="skeleton" style="height: 40px" />
      </div>

      <div v-else-if="!filtrados.length" class="empty">
        <span class="empty-icon"><Wrench :size="24" /></span>
        <template v-if="busca">
          <h3>Nenhum resultado</h3>
          <p>Nenhum serviço corresponde a "{{ busca }}".</p>
        </template>
        <template v-else>
          <h3>Nenhum serviço registrado</h3>
          <p>Abra um serviço para um veículo e lance as peças utilizadas.</p>
          <button class="btn btn-primary" style="margin-top: 8px" @click="abrirNovo"><Plus :size="16" /> Novo serviço</button>
        </template>
      </div>

      <template v-else>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="th-sort" @click="ordenarPor('id')">Nº <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('carroPlaca')">Veículo <ArrowUpDown :size="12" /></th>
                <th>Observação</th>
                <th class="th-sort" @click="ordenarPor('pronto')">Status <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('qtdItens')">Peças <ArrowUpDown :size="12" /></th>
                <th class="th-sort num" @click="ordenarPor('maoDeObra')">Mão de obra <ArrowUpDown :size="12" /></th>
                <th class="th-sort num" @click="ordenarPor('total')">Total <ArrowUpDown :size="12" /></th>
                <th style="text-align: right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in paginados" :key="s.id" class="clicavel" @click="abrirOrdem(s)">
                <td class="faint mono">#{{ s.id }}</td>
                <td>
                  <div class="veiculo">
                    <span class="plate">{{ formatarPlaca(s.carroPlaca) }}</span>
                    <div>
                      <div class="name">{{ s.carroModelo }}</div>
                      <div class="faint sub">{{ carroPorId(s.carroId)?.clienteNome }}</div>
                    </div>
                  </div>
                </td>
                <td class="muted obs" :title="s.observacao">{{ s.observacao || '—' }}</td>
                <td>
                  <span v-if="s.pronto" class="status pronto"><CircleCheck :size="13" /> Pronto</span>
                  <span v-else class="status andamento"><Wrench :size="13" /> Em andamento</span>
                </td>
                <td><span class="badge" :class="{ primary: s.qtdItens }">{{ s.qtdItens }} {{ s.qtdItens === 1 ? 'item' : 'itens' }}</span></td>
                <td class="mono num muted">{{ formatarMoeda(s.maoDeObra) }}</td>
                <td class="mono num total">{{ formatarMoeda(s.total) }}</td>
                <td @click.stop>
                  <div class="actions">
                    <button v-if="!s.pronto" class="icon-btn sucesso" title="Marcar como pronto" aria-label="Marcar como pronto" @click="abrirConcluir(s)"><CircleCheck :size="16" /></button>
                    <button v-else class="icon-btn" title="Reabrir serviço" aria-label="Reabrir serviço" @click="reabrindo = s"><RotateCcw :size="16" /></button>
                    <button class="icon-btn" title="Abrir ordem de serviço" aria-label="Abrir ordem de serviço" @click="abrirOrdem(s)"><ClipboardList :size="16" /></button>
                    <button class="icon-btn" title="Editar" aria-label="Editar" @click="abrirEdicao(s)"><Pencil :size="16" /></button>
                    <button class="icon-btn danger" title="Excluir" aria-label="Excluir" @click="excluindo = s"><Trash2 :size="16" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <TablePagination v-model:pagina="pagina" :total="filtrados.length" :por-pagina="settings.itensPorPagina" />
      </template>
    </div>

    <!-- Modal: abrir / editar serviço -->
    <BaseModal
      :aberto="formAberto"
      :titulo="editandoId ? 'Editar serviço' : 'Novo serviço'"
      :subtitulo="editandoId ? `Serviço #${editandoId}` : 'Depois de salvar, você poderá lançar as peças'"
      @fechar="formAberto = false"
    >
      <div v-if="!carros.length" class="aviso">
        Nenhum veículo cadastrado. É preciso <RouterLink to="/veiculos">cadastrar um veículo</RouterLink> antes de abrir um serviço.
      </div>
      <form id="form-servico" class="form-grid" novalidate @submit.prevent="salvar">
        <div class="field full">
          <label for="carro">Veículo <span class="req">*</span></label>
          <select id="carro" v-model="form.carroId" class="select" :class="{ invalid: erros.carroId }">
            <option value="" disabled>Selecione um veículo</option>
            <option v-for="c in carros" :key="c.id" :value="c.id">
              {{ formatarPlaca(c.placa) }} — {{ c.modelo }} ({{ c.marca }}) · {{ c.clienteNome }}
            </option>
          </select>
          <span v-if="erros.carroId" class="field-error">{{ erros.carroId }}</span>
        </div>
        <div class="field">
          <label for="maoDeObra">Mão de obra <span class="req">*</span></label>
          <div class="input-moeda">
            <span>R$</span>
            <input id="maoDeObra" v-model="form.maoDeObra" class="input mono" :class="{ invalid: erros.maoDeObra }" placeholder="0,00" inputmode="decimal" />
          </div>
          <span v-if="erros.maoDeObra" class="field-error">{{ erros.maoDeObra }}</span>
        </div>
        <div class="field full">
          <label for="obs">Observação</label>
          <textarea id="obs" v-model="form.observacao" class="input" rows="3" placeholder="Ex.: Troca de pastilhas e revisão do sistema de freios" />
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" :disabled="salvando" @click="formAberto = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="form-servico" :disabled="salvando || !carros.length">
          <span v-if="salvando" class="spinner" />
          {{ editandoId ? 'Salvar alterações' : 'Abrir serviço' }}
        </button>
      </template>
    </BaseModal>

    <!-- Modal: ordem de serviço com os itens -->
    <BaseModal :aberto="!!ordem" :titulo="`Ordem de serviço #${ordem?.id ?? ''}`" largura="820px" @fechar="ordemId = null">
      <template v-if="ordem">
        <div v-if="conclusaoDo(ordem.id)" class="faixa-pronto">
          <CircleCheck :size="16" />
          Pronto desde {{ formatarDataHora(conclusaoDo(ordem.id).dataConclusao) }}
        </div>
        <div class="os-info">
          <div class="os-bloco">
            <span class="os-label"><Car :size="13" /> Veículo</span>
            <div class="veiculo">
              <span class="plate">{{ formatarPlaca(ordem.carroPlaca) }}</span>
              <strong>{{ ordem.carroModelo }}</strong>
              <span class="muted">{{ carroDaOrdem?.marca }}</span>
            </div>
          </div>
          <div class="os-bloco">
            <span class="os-label"><User :size="13" /> Cliente</span>
            <strong>{{ carroDaOrdem?.clienteNome || '—' }}</strong>
          </div>
          <div v-if="ordem.observacao" class="os-bloco full">
            <span class="os-label">Observação</span>
            <p class="muted">{{ ordem.observacao }}</p>
          </div>
        </div>

        <h4 class="secao"><Package :size="16" /> Peças utilizadas</h4>

        <!-- Adicionar peça -->
        <form class="add-item" novalidate @submit.prevent="adicionarItem">
          <div class="field grow">
            <select v-model="novoItem.pecaId" class="select" :class="{ invalid: errosItem.pecaId }" aria-label="Peça">
              <option value="" disabled>{{ pecas.length ? 'Selecione uma peça' : 'Nenhuma peça cadastrada' }}</option>
              <option v-for="p in pecas" :key="p.id" :value="p.id">{{ p.nome }} — {{ p.fabricante }} · {{ formatarMoeda(p.preco) }}</option>
            </select>
            <span v-if="errosItem.pecaId" class="field-error">{{ errosItem.pecaId }}</span>
          </div>
          <div class="field qtd">
            <input v-model.number="novoItem.quantidade" type="number" min="1" step="1" class="input mono" :class="{ invalid: errosItem.quantidade }" aria-label="Quantidade" />
          </div>
          <button class="btn btn-primary" type="submit" :disabled="adicionandoItem || !pecas.length">
            <span v-if="adicionandoItem" class="spinner" />
            <Plus v-else :size="16" /> Adicionar
          </button>
        </form>
        <p v-if="errosItem.quantidade" class="field-error" style="margin-top: -8px; margin-bottom: 12px">{{ errosItem.quantidade }}</p>
        <p v-else-if="pecaSelecionada" class="field-hint" style="margin-top: -8px; margin-bottom: 12px">
          Subtotal: {{ formatarMoeda(pecaSelecionada.preco * (Number(novoItem.quantidade) || 0)) }}
        </p>
        <p v-if="!pecas.length" class="field-hint" style="margin-bottom: 12px">
          Cadastre peças na página <RouterLink to="/pecas" class="link">Peças</RouterLink>.
        </p>

        <!-- Lista de itens -->
        <div class="itens card">
          <table class="table">
            <thead>
              <tr>
                <th>Peça</th>
                <th class="num">Preço unit.</th>
                <th style="width: 110px">Qtd.</th>
                <th class="num">Subtotal</th>
                <th style="width: 48px"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!itensDaOrdem.length">
                <td colspan="5" class="faint" style="text-align: center; padding: 28px">Nenhuma peça lançada neste serviço.</td>
              </tr>
              <tr v-for="item in itensDaOrdem" :key="item.id" :class="{ processando: itemProcessando === item.id }">
                <td class="name">{{ item.pecaNome }}</td>
                <td class="mono num muted">{{ formatarMoeda(item.pecaPreco) }}</td>
                <td>
                  <input
                    :value="item.quantidade"
                    type="number"
                    min="1"
                    step="1"
                    class="input mono qtd-input"
                    aria-label="Quantidade"
                    :disabled="itemProcessando === item.id"
                    @change="onQuantidade(item, $event)"
                  />
                </td>
                <td class="mono num">{{ formatarMoeda(subtotal(item)) }}</td>
                <td>
                  <button class="icon-btn danger" title="Remover peça" aria-label="Remover peça" :disabled="itemProcessando === item.id" @click="removerItem(item)">
                    <X :size="16" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Totais -->
        <dl class="totais">
          <div><dt>Peças</dt><dd class="mono">{{ formatarMoeda(totalPecas(ordem.id)) }}</dd></div>
          <div><dt>Mão de obra</dt><dd class="mono">{{ formatarMoeda(ordem.maoDeObra) }}</dd></div>
          <div class="geral"><dt>Total</dt><dd class="mono">{{ formatarMoeda(totalServico(ordem)) }}</dd></div>
        </dl>
        <p class="field-hint">O preço das peças é o preço atual do cadastro (a API não guarda o valor da época do serviço).</p>
      </template>
      <template #footer>
        <button class="btn btn-secondary" @click="abrirEdicao(ordem); ordemId = null"><Pencil :size="16" /> Editar serviço</button>
        <button v-if="ordem && !conclusaoDo(ordem.id)" class="btn btn-secondary" @click="abrirConcluir(ordem)">
          <CircleCheck :size="16" /> Marcar como pronto
        </button>
        <button class="btn btn-primary" @click="ordemId = null">Fechar</button>
      </template>
    </BaseModal>

    <!-- Modal: marcar como pronto -->
    <BaseModal
      :aberto="!!concluindo"
      titulo="Marcar serviço como pronto"
      :subtitulo="concluindo ? `Serviço #${concluindo.id} · ${concluindo.carroModelo} · ${formatarPlaca(concluindo.carroPlaca)}` : ''"
      largura="480px"
      @fechar="concluindo = null"
    >
      <template v-if="concluindo">
        <div class="resumo-total">
          <span class="muted">Valor total do serviço</span>
          <strong class="mono">{{ formatarMoeda(totalServico(concluindo)) }}</strong>
        </div>
        <div class="field">
          <label for="obs-conclusao">Observação da entrega</label>
          <textarea id="obs-conclusao" v-model="obsConclusao" class="input" rows="3" placeholder="Ex.: Cliente avisado por telefone" />
          <span class="field-hint">A data e a hora da conclusão são registradas automaticamente.</span>
        </div>
      </template>
      <template #footer>
        <button class="btn btn-secondary" :disabled="processandoConclusao" @click="concluindo = null">Cancelar</button>
        <button class="btn btn-primary" :disabled="processandoConclusao" @click="confirmarConclusao">
          <span v-if="processandoConclusao" class="spinner" />
          <CircleCheck v-else :size="16" /> Marcar como pronto
        </button>
      </template>
    </BaseModal>

    <!-- Confirmação: reabrir -->
    <ConfirmDialog
      :aberto="!!reabrindo"
      :carregando="processandoReabertura"
      titulo="Reabrir serviço"
      texto-confirmar="Reabrir"
      @cancelar="reabrindo = null"
      @confirmar="confirmarReabertura"
    >
      O serviço <strong style="color: var(--text)">#{{ reabrindo?.id }}</strong> voltará para "Em andamento"
      e sairá da aba Prontos.
    </ConfirmDialog>

    <ConfirmDialog
      :aberto="!!excluindo"
      :carregando="processandoExclusao"
      titulo="Excluir serviço"
      @cancelar="excluindo = null"
      @confirmar="confirmarExclusao"
    >
      Tem certeza que deseja excluir o serviço <strong style="color: var(--text)">#{{ excluindo?.id }}</strong>
      ({{ excluindo?.carroModelo }} · {{ formatarPlaca(excluindo?.carroPlaca) }})?
      <template v-if="excluindo && conclusaoDo(excluindo.id)">
        <br /><br />
        <span style="color: var(--warning-text)">Este serviço está marcado como pronto. Reabra-o antes de excluir.</span>
      </template>
      <template v-if="excluindo && itensDoServico(excluindo.id).length">
        <br /><br />
        <span style="color: var(--warning)">
          Este serviço tem {{ itensDoServico(excluindo.id).length }} peça(s) lançada(s). O back-end só permite excluir depois de
          remover as peças na ordem de serviço.
        </span>
      </template>
    </ConfirmDialog>
  </section>
</template>

<style scoped>
.skel-list { display: flex; flex-direction: column; gap: 12px; }
.th-sort svg { vertical-align: -1px; margin-left: 2px; opacity: 0.6; }
.num { text-align: right !important; }
.total { font-weight: 600; }
.obs { max-width: 260px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.clicavel { cursor: pointer; }

.faixa-pronto {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px 14px;
  border-radius: var(--radius);
  background: var(--success-soft);
  color: var(--success);
  font-weight: 500;
}
.resumo-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: var(--radius);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
}
.resumo-total strong { font-size: 18px; color: var(--primary-text); }

.veiculo { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.veiculo .name { font-weight: 500; }
.veiculo .sub { font-size: 12px; }

.aviso {
  margin-bottom: 20px;
  padding: 12px 14px;
  border-radius: var(--radius);
  background: var(--warning-soft);
  color: var(--warning-text);
  font-size: 13px;
}
.aviso a, .link { text-decoration: underline; }

/* Ordem de serviço */
.os-info { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; }
.os-info .full { grid-column: 1 / -1; }
.os-bloco {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.os-label { display: flex; align-items: center; gap: 6px; font-size: 12px; color: var(--text-faint); text-transform: uppercase; letter-spacing: 0.04em; }
@media (max-width: 600px) { .os-info { grid-template-columns: 1fr; } }

.secao { display: flex; align-items: center; gap: 8px; margin: 24px 0 12px; font-size: 14px; }

.add-item { display: flex; gap: 10px; align-items: flex-start; margin-bottom: 16px; }
.add-item .grow { flex: 1; min-width: 0; }
.add-item .qtd { width: 90px; }
@media (max-width: 600px) { .add-item { flex-wrap: wrap; } .add-item .grow { flex-basis: 100%; } }

.itens { overflow-x: auto; border-radius: var(--radius); }
.itens .table td, .itens .table th { padding-left: 14px; padding-right: 14px; }
.qtd-input { height: 34px; width: 80px; }
tr.processando { opacity: 0.5; }

.totais {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  margin: 16px 0 8px;
}
.totais div { display: flex; gap: 24px; justify-content: space-between; min-width: 260px; }
.totais dt { color: var(--text-muted); }
.totais dd { margin: 0; }
.totais .geral { padding-top: 10px; margin-top: 4px; border-top: 1px solid var(--border); font-size: 18px; font-weight: 700; }
.totais .geral dt { color: var(--text); }
.totais .geral dd { color: var(--primary-text); }
</style>

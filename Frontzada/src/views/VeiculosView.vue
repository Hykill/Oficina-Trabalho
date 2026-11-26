<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Plus, Search, Pencil, Trash2, Eye, Car, WifiOff, ArrowUpDown, User } from 'lucide-vue-next'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import TablePagination from '@/components/TablePagination.vue'
import carroService from '@/services/carroService'
import clienteService from '@/services/clienteService'
import { mensagemDeErro } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useSettings } from '@/composables/useSettings'
import { formatarPlaca, normalizarPlaca, formatarCpf, normalizarTexto } from '@/utils/formatters'
import { validarCarro } from '@/utils/validators'

const toast = useToast()
const { settings } = useSettings()

// ---------------- Listagem ----------------
const carros = ref([])
const clientes = ref([]) // usados no <select> de proprietário
const carregando = ref(true)
const erroCarregar = ref('')

async function carregar() {
  carregando.value = true
  erroCarregar.value = ''
  try {
    const [v, c] = await Promise.all([carroService.listar(), clienteService.listar()])
    carros.value = v
    clientes.value = [...c].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
  } catch (e) {
    erroCarregar.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

// ---------------- Pesquisa, ordenação e paginação ----------------
const busca = ref('')
const ordenacao = reactive({ campo: 'id', direcao: 'desc' })
const pagina = ref(1)

const filtrados = computed(() => {
  const termo = normalizarTexto(busca.value.trim())
  const termoPlaca = normalizarPlaca(busca.value)
  let lista = carros.value
  if (termo) {
    lista = lista.filter(
      (v) =>
        (termoPlaca && normalizarPlaca(v.placa).includes(termoPlaca)) ||
        normalizarTexto(v.modelo).includes(termo) ||
        normalizarTexto(v.marca).includes(termo) ||
        normalizarTexto(v.clienteNome).includes(termo)
    )
  }
  const { campo, direcao } = ordenacao
  const fator = direcao === 'asc' ? 1 : -1
  return [...lista].sort((a, b) => {
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

// ---------------- Formulário ----------------
const formAberto = ref(false)
const editandoId = ref(null)
const salvando = ref(false)
const form = reactive({ placa: '', marca: '', modelo: '', clienteId: '' })
const erros = ref({})
const tentouEnviar = ref(false)

function abrirNovo() {
  editandoId.value = null
  Object.assign(form, { placa: '', marca: '', modelo: '', clienteId: '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

function abrirEdicao(v) {
  editandoId.value = v.id
  Object.assign(form, { placa: normalizarPlaca(v.placa), marca: v.marca ?? '', modelo: v.modelo ?? '', clienteId: v.clienteId ?? '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

watch(form, () => {
  if (tentouEnviar.value) erros.value = validarCarro(form)
})

async function salvar() {
  tentouEnviar.value = true
  erros.value = validarCarro(form)
  if (Object.keys(erros.value).length) return

  // Exatamente os campos do CarroDTO do back-end
  const payload = {
    modelo: form.modelo.trim(),
    marca: form.marca.trim(),
    placa: normalizarPlaca(form.placa),
    clienteId: Number(form.clienteId)
  }

  salvando.value = true
  try {
    if (editandoId.value) {
      const atualizado = await carroService.atualizar(editandoId.value, payload)
      const i = carros.value.findIndex((v) => v.id === editandoId.value)
      if (i !== -1) carros.value[i] = atualizado
      toast.sucesso('Veículo atualizado com sucesso.')
    } else {
      const criado = await carroService.cadastrar(payload)
      carros.value.push(criado)
      toast.sucesso('Veículo cadastrado com sucesso.')
    }
    formAberto.value = false
  } catch (e) {
    const msg = mensagemDeErro(e)
    if (e.response?.status === 409) erros.value = { ...erros.value, placa: msg } // placa duplicada
    toast.erro(msg)
  } finally {
    salvando.value = false
  }
}

const onPlaca = (e) => (form.placa = normalizarPlaca(e.target.value))

// ---------------- Visualizar ----------------
const visualizando = ref(null)
const proprietario = computed(() => clientes.value.find((c) => c.id === visualizando.value?.clienteId))

// ---------------- Excluir ----------------
const excluindo = ref(null)
const processandoExclusao = ref(false)

async function confirmarExclusao() {
  processandoExclusao.value = true
  try {
    await carroService.excluir(excluindo.value.id)
    carros.value = carros.value.filter((v) => v.id !== excluindo.value.id)
    toast.sucesso('Veículo excluído.')
    excluindo.value = null
  } catch (e) {
    // Ex.: 409 "Não é possível excluir: o carro possui serviços cadastrados"
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
        <h1 class="page-title">Veículos</h1>
        <p class="page-subtitle">Gerencie os veículos atendidos pela oficina</p>
      </div>
      <button class="btn btn-primary" @click="abrirNovo"><Plus :size="18" /> Novo veículo</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search">
          <Search :size="16" />
          <input v-model="busca" class="input" type="search" placeholder="Pesquisar por placa, modelo, marca ou proprietário..." aria-label="Pesquisar veículos" />
        </div>
        <span v-if="!carregando && !erroCarregar" class="badge">
          {{ filtrados.length }} {{ filtrados.length === 1 ? 'veículo' : 'veículos' }}
        </span>
      </div>

      <div v-if="erroCarregar" class="empty error">
        <span class="empty-icon"><WifiOff :size="24" /></span>
        <h3>Erro ao carregar veículos</h3>
        <p>{{ erroCarregar }}</p>
        <button class="btn btn-primary" style="margin-top: 8px" @click="carregar">Tentar novamente</button>
      </div>

      <div v-else-if="carregando" class="card-body skel-list">
        <span v-for="n in 5" :key="n" class="skeleton" style="height: 40px" />
      </div>

      <div v-else-if="!filtrados.length" class="empty">
        <span class="empty-icon"><Car :size="24" /></span>
        <template v-if="busca">
          <h3>Nenhum resultado</h3>
          <p>Nenhum veículo corresponde a "{{ busca }}".</p>
        </template>
        <template v-else>
          <h3>Nenhum veículo cadastrado</h3>
          <p>Cadastre o primeiro veículo e vincule-o a um cliente.</p>
          <button class="btn btn-primary" style="margin-top: 8px" @click="abrirNovo"><Plus :size="16" /> Novo veículo</button>
        </template>
      </div>

      <template v-else>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="th-sort" @click="ordenarPor('id')">ID <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('placa')">Placa <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('modelo')">Modelo <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('marca')">Marca <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('clienteNome')">Proprietário <ArrowUpDown :size="12" /></th>
                <th style="text-align: right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="v in paginados" :key="v.id">
                <td class="faint mono">#{{ v.id }}</td>
                <td><span class="plate">{{ formatarPlaca(v.placa) }}</span></td>
                <td><strong>{{ v.modelo }}</strong></td>
                <td><span class="badge">{{ v.marca }}</span></td>
                <td class="muted">{{ v.clienteNome }}</td>
                <td>
                  <div class="actions">
                    <button class="icon-btn" title="Visualizar" aria-label="Visualizar" @click="visualizando = v"><Eye :size="16" /></button>
                    <button class="icon-btn" title="Editar" aria-label="Editar" @click="abrirEdicao(v)"><Pencil :size="16" /></button>
                    <button class="icon-btn danger" title="Excluir" aria-label="Excluir" @click="excluindo = v"><Trash2 :size="16" /></button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <TablePagination v-model:pagina="pagina" :total="filtrados.length" :por-pagina="settings.itensPorPagina" />
      </template>
    </div>

    <!-- Modal: cadastrar / editar -->
    <BaseModal
      :aberto="formAberto"
      :titulo="editandoId ? 'Editar veículo' : 'Novo veículo'"
      :subtitulo="editandoId ? `Veículo #${editandoId}` : 'Preencha os dados do veículo'"
      @fechar="formAberto = false"
    >
      <div v-if="!clientes.length" class="aviso">
        Nenhum cliente cadastrado. É preciso <RouterLink to="/clientes">cadastrar um cliente</RouterLink> antes do veículo.
      </div>
      <form id="form-veiculo" class="form-grid" novalidate @submit.prevent="salvar">
        <div class="field">
          <label for="placa">Placa <span class="req">*</span></label>
          <input id="placa" :value="form.placa" class="input mono placa-input" :class="{ invalid: erros.placa }" placeholder="ABC1D23" maxlength="8" @input="onPlaca" />
          <span v-if="erros.placa" class="field-error">{{ erros.placa }}</span>
          <span v-else class="field-hint">Padrão antigo (ABC1234) ou Mercosul (ABC1D23)</span>
        </div>
        <div class="field">
          <label for="marca">Marca <span class="req">*</span></label>
          <input id="marca" v-model="form.marca" class="input" :class="{ invalid: erros.marca }" placeholder="Ex.: Volkswagen" list="marcas" />
          <datalist id="marcas">
            <option v-for="m in ['Chevrolet', 'Fiat', 'Ford', 'Honda', 'Hyundai', 'Jeep', 'Nissan', 'Renault', 'Toyota', 'Volkswagen']" :key="m" :value="m" />
          </datalist>
          <span v-if="erros.marca" class="field-error">{{ erros.marca }}</span>
        </div>
        <div class="field full">
          <label for="modelo">Modelo <span class="req">*</span></label>
          <input id="modelo" v-model="form.modelo" class="input" :class="{ invalid: erros.modelo }" placeholder="Ex.: Gol 1.0" />
          <span v-if="erros.modelo" class="field-error">{{ erros.modelo }}</span>
        </div>
        <div class="field full">
          <label for="cliente">Proprietário <span class="req">*</span></label>
          <select id="cliente" v-model="form.clienteId" class="select" :class="{ invalid: erros.clienteId }">
            <option value="" disabled>Selecione um cliente</option>
            <option v-for="c in clientes" :key="c.id" :value="c.id">{{ c.nome }} — CPF {{ formatarCpf(c.cpf) }}</option>
          </select>
          <span v-if="erros.clienteId" class="field-error">{{ erros.clienteId }}</span>
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" :disabled="salvando" @click="formAberto = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="form-veiculo" :disabled="salvando || !clientes.length">
          <span v-if="salvando" class="spinner" />
          {{ editandoId ? 'Salvar alterações' : 'Cadastrar' }}
        </button>
      </template>
    </BaseModal>

    <!-- Modal: visualizar -->
    <BaseModal :aberto="!!visualizando" titulo="Detalhes do veículo" @fechar="visualizando = null">
      <template v-if="visualizando">
        <div class="perfil">
          <span class="carro-icone"><Car :size="24" /></span>
          <div>
            <h3>{{ visualizando.modelo }}</h3>
            <span class="muted">{{ visualizando.marca }} · <span class="faint mono">#{{ visualizando.id }}</span></span>
          </div>
          <span class="plate grande">{{ formatarPlaca(visualizando.placa) }}</span>
        </div>
        <h4 class="secao"><User :size="16" /> Proprietário</h4>
        <dl class="details">
          <div><dt>Nome</dt><dd>{{ visualizando.clienteNome }}</dd></div>
          <div><dt>CPF</dt><dd class="mono">{{ proprietario ? formatarCpf(proprietario.cpf) : '—' }}</dd></div>
        </dl>
      </template>
      <template #footer>
        <button class="btn btn-secondary" @click="visualizando = null">Fechar</button>
        <button class="btn btn-primary" @click="abrirEdicao(visualizando); visualizando = null"><Pencil :size="16" /> Editar</button>
      </template>
    </BaseModal>

    <ConfirmDialog
      :aberto="!!excluindo"
      :carregando="processandoExclusao"
      titulo="Excluir veículo"
      @cancelar="excluindo = null"
      @confirmar="confirmarExclusao"
    >
      Tem certeza que deseja excluir o veículo
      <strong style="color: var(--text)">{{ excluindo?.modelo }} ({{ formatarPlaca(excluindo?.placa) }})</strong>?
      Veículos com serviços cadastrados não podem ser excluídos.
    </ConfirmDialog>
  </section>
</template>

<style scoped>
.skel-list { display: flex; flex-direction: column; gap: 12px; }
.th-sort svg { vertical-align: -1px; margin-left: 2px; opacity: 0.6; }
.placa-input { text-transform: uppercase; letter-spacing: 0.08em; }

.aviso {
  margin-bottom: 20px;
  padding: 12px 14px;
  border-radius: var(--radius);
  background: var(--warning-soft);
  color: var(--warning-text);
  font-size: 13px;
}
.aviso a { text-decoration: underline; }

.perfil { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.perfil h3 { font-size: 17px; }
.carro-icone {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: var(--primary-soft);
  color: var(--primary-text);
}
.plate.grande { margin-left: auto; height: 34px; font-size: 16px; padding: 0 14px; }

.secao {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 24px 0 12px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
  font-size: 14px;
}
</style>

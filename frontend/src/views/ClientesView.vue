<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { Plus, Search, Pencil, Trash2, Eye, Users, WifiOff, ArrowUpDown, Mail, Phone, Car } from 'lucide-vue-next'
import BaseModal from '@/components/BaseModal.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import TablePagination from '@/components/TablePagination.vue'
import clienteService from '@/services/clienteService'
import carroService from '@/services/carroService'
import { mensagemDeErro } from '@/services/api'
import { useToast } from '@/composables/useToast'
import { useSettings } from '@/composables/useSettings'
import { formatarCpf, formatarTelefone, formatarPlaca, somenteDigitos, iniciais, normalizarTexto } from '@/utils/formatters'
import { validarCliente } from '@/utils/validators'

const toast = useToast()
const { settings } = useSettings()

// ---------------- Listagem ----------------
const clientes = ref([])
const carregando = ref(true)
const erroCarregar = ref('')

async function carregar() {
  carregando.value = true
  erroCarregar.value = ''
  try {
    clientes.value = await clienteService.listar()
  } catch (e) {
    erroCarregar.value = mensagemDeErro(e)
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)

// ---------------- Pesquisa, ordenação e paginação ----------------
// A API não tem endpoint de busca, então filtramos aqui no front.
const busca = ref('')
const ordenacao = reactive({ campo: 'id', direcao: 'desc' })
const pagina = ref(1)

const filtrados = computed(() => {
  const termo = normalizarTexto(busca.value.trim())
  const termoDigitos = somenteDigitos(busca.value)
  let lista = clientes.value
  if (termo) {
    lista = lista.filter(
      (c) => normalizarTexto(c.nome).includes(termo) || (termoDigitos && somenteDigitos(c.cpf).includes(termoDigitos))
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

// Ao pesquisar, volta para a primeira página
watch(busca, () => (pagina.value = 1))
// Se excluir o último item da última página, recua uma página
watch(filtrados, (lista) => {
  const max = Math.max(1, Math.ceil(lista.length / settings.itensPorPagina))
  if (pagina.value > max) pagina.value = max
})

function ordenarPor(campo) {
  if (ordenacao.campo === campo) ordenacao.direcao = ordenacao.direcao === 'asc' ? 'desc' : 'asc'
  else Object.assign(ordenacao, { campo, direcao: 'asc' })
}

// ---------------- Formulário (cadastrar / editar) ----------------
const formAberto = ref(false)
const editandoId = ref(null)
const salvando = ref(false)
const form = reactive({ nome: '', cpf: '', telefone: '', email: '' })
const erros = ref({})
const tentouEnviar = ref(false)

function abrirNovo() {
  editandoId.value = null
  Object.assign(form, { nome: '', cpf: '', telefone: '', email: '' })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

function abrirEdicao(c) {
  editandoId.value = c.id
  Object.assign(form, {
    nome: c.nome ?? '',
    cpf: formatarCpf(c.cpf),
    telefone: formatarTelefone(c.telefone),
    email: c.email ?? ''
  })
  erros.value = {}
  tentouEnviar.value = false
  formAberto.value = true
}

// Revalida enquanto o usuário digita, mas só depois da 1ª tentativa de salvar
watch(form, () => {
  if (tentouEnviar.value) erros.value = validarCliente(form)
})

async function salvar() {
  tentouEnviar.value = true
  erros.value = validarCliente(form)
  if (Object.keys(erros.value).length) return

  // Para a API enviamos só dígitos (mesmo formato do banco / Postman)
  const payload = {
    nome: form.nome.trim(),
    cpf: somenteDigitos(form.cpf),
    telefone: somenteDigitos(form.telefone) || null,
    email: form.email.trim() || null
  }

  salvando.value = true
  try {
    if (editandoId.value) {
      const atualizado = await clienteService.atualizar(editandoId.value, { id: editandoId.value, ...payload })
      const i = clientes.value.findIndex((c) => c.id === editandoId.value)
      if (i !== -1) clientes.value[i] = atualizado
      toast.sucesso('Cliente atualizado com sucesso.')
    } else {
      const criado = await clienteService.cadastrar(payload)
      clientes.value.push(criado)
      toast.sucesso('Cliente cadastrado com sucesso.')
    }
    formAberto.value = false
  } catch (e) {
    const msg = mensagemDeErro(e)
    // 409 = CPF duplicado → mostra o erro no próprio campo
    if (e.response?.status === 409) erros.value = { ...erros.value, cpf: msg }
    toast.erro(msg)
  } finally {
    salvando.value = false
  }
}

// ---------------- Visualizar ----------------
const visualizando = ref(null)
const veiculosDoCliente = ref([])
const carregandoVeiculos = ref(false)

async function abrirVisualizar(c) {
  visualizando.value = c
  veiculosDoCliente.value = []
  carregandoVeiculos.value = true
  try {
    // Não há endpoint /clientes/{id}/carros; filtramos a lista de /carros
    const todos = await carroService.listar()
    veiculosDoCliente.value = todos.filter((v) => v.clienteId === c.id)
  } catch {
    veiculosDoCliente.value = null // sinaliza erro ao carregar
  } finally {
    carregandoVeiculos.value = false
  }
}

// ---------------- Excluir ----------------
const excluindo = ref(null)
const processandoExclusao = ref(false)

async function confirmarExclusao() {
  processandoExclusao.value = true
  try {
    await clienteService.excluir(excluindo.value.id)
    clientes.value = clientes.value.filter((c) => c.id !== excluindo.value.id)
    toast.sucesso('Cliente excluído.')
    excluindo.value = null
  } catch (e) {
    // Ex.: 409 "Não é possível excluir: o cliente possui carros cadastrados"
    toast.erro(mensagemDeErro(e))
  } finally {
    processandoExclusao.value = false
  }
}

// Máscaras aplicadas enquanto digita
const onCpf = (e) => (form.cpf = formatarCpf(e.target.value))
const onTelefone = (e) => (form.telefone = formatarTelefone(e.target.value))
</script>

<template>
  <section>
    <div class="page-header">
      <div>
        <h1 class="page-title">Clientes</h1>
        <p class="page-subtitle">Gerencie os clientes da oficina</p>
      </div>
      <button class="btn btn-primary" @click="abrirNovo"><Plus :size="18" /> Novo cliente</button>
    </div>

    <div class="card">
      <div class="toolbar">
        <div class="search">
          <Search :size="16" />
          <input v-model="busca" class="input" type="search" placeholder="Pesquisar por nome ou CPF..." aria-label="Pesquisar clientes" />
        </div>
        <span v-if="!carregando && !erroCarregar" class="badge">
          {{ filtrados.length }} {{ filtrados.length === 1 ? 'cliente' : 'clientes' }}
        </span>
      </div>

      <!-- Erro -->
      <div v-if="erroCarregar" class="empty error">
        <span class="empty-icon"><WifiOff :size="24" /></span>
        <h3>Erro ao carregar clientes</h3>
        <p>{{ erroCarregar }}</p>
        <button class="btn btn-primary" style="margin-top: 8px" @click="carregar">Tentar novamente</button>
      </div>

      <!-- Carregando -->
      <div v-else-if="carregando" class="card-body skel-list">
        <span v-for="n in 5" :key="n" class="skeleton" style="height: 40px" />
      </div>

      <!-- Vazio -->
      <div v-else-if="!filtrados.length" class="empty">
        <span class="empty-icon"><Users :size="24" /></span>
        <template v-if="busca">
          <h3>Nenhum resultado</h3>
          <p>Nenhum cliente corresponde a "{{ busca }}".</p>
        </template>
        <template v-else>
          <h3>Nenhum cliente cadastrado</h3>
          <p>Comece cadastrando o primeiro cliente da oficina.</p>
          <button class="btn btn-primary" style="margin-top: 8px" @click="abrirNovo"><Plus :size="16" /> Novo cliente</button>
        </template>
      </div>

      <!-- Tabela -->
      <template v-else>
        <div class="table-wrap">
          <table class="table">
            <thead>
              <tr>
                <th class="th-sort" @click="ordenarPor('id')">ID <ArrowUpDown :size="12" /></th>
                <th class="th-sort" @click="ordenarPor('nome')">Nome <ArrowUpDown :size="12" /></th>
                <th>CPF</th>
                <th>Telefone</th>
                <th>E-mail</th>
                <th style="text-align: right">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in paginados" :key="c.id">
                <td class="faint mono">#{{ c.id }}</td>
                <td>
                  <div class="cell-user">
                    <span class="avatar">{{ iniciais(c.nome) }}</span>
                    <span class="name">{{ c.nome }}</span>
                  </div>
                </td>
                <td class="mono">{{ formatarCpf(c.cpf) }}</td>
                <td class="mono muted">{{ formatarTelefone(c.telefone) || '—' }}</td>
                <td class="muted">{{ c.email || '—' }}</td>
                <td>
                  <div class="actions">
                    <button class="icon-btn" title="Visualizar" aria-label="Visualizar" @click="abrirVisualizar(c)"><Eye :size="16" /></button>
                    <button class="icon-btn" title="Editar" aria-label="Editar" @click="abrirEdicao(c)"><Pencil :size="16" /></button>
                    <button class="icon-btn danger" title="Excluir" aria-label="Excluir" @click="excluindo = c"><Trash2 :size="16" /></button>
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
      :titulo="editandoId ? 'Editar cliente' : 'Novo cliente'"
      :subtitulo="editandoId ? `Cliente #${editandoId}` : 'Preencha os dados do cliente'"
      @fechar="formAberto = false"
    >
      <form id="form-cliente" class="form-grid" novalidate @submit.prevent="salvar">
        <div class="field full">
          <label for="nome">Nome completo <span class="req">*</span></label>
          <input id="nome" v-model="form.nome" class="input" :class="{ invalid: erros.nome }" placeholder="Ex.: João da Silva" autofocus />
          <span v-if="erros.nome" class="field-error">{{ erros.nome }}</span>
        </div>
        <div class="field">
          <label for="cpf">CPF <span class="req">*</span></label>
          <input id="cpf" :value="form.cpf" class="input mono" :class="{ invalid: erros.cpf }" placeholder="000.000.000-00" inputmode="numeric" @input="onCpf" />
          <span v-if="erros.cpf" class="field-error">{{ erros.cpf }}</span>
        </div>
        <div class="field">
          <label for="telefone">Telefone</label>
          <input id="telefone" :value="form.telefone" class="input mono" :class="{ invalid: erros.telefone }" placeholder="(00) 00000-0000" inputmode="tel" @input="onTelefone" />
          <span v-if="erros.telefone" class="field-error">{{ erros.telefone }}</span>
        </div>
        <div class="field full">
          <label for="email">E-mail</label>
          <input id="email" v-model="form.email" type="email" class="input" :class="{ invalid: erros.email }" placeholder="cliente@email.com" />
          <span v-if="erros.email" class="field-error">{{ erros.email }}</span>
        </div>
      </form>
      <template #footer>
        <button class="btn btn-secondary" :disabled="salvando" @click="formAberto = false">Cancelar</button>
        <button class="btn btn-primary" type="submit" form="form-cliente" :disabled="salvando">
          <span v-if="salvando" class="spinner" />
          {{ editandoId ? 'Salvar alterações' : 'Cadastrar' }}
        </button>
      </template>
    </BaseModal>

    <!-- Modal: visualizar -->
    <BaseModal :aberto="!!visualizando" titulo="Detalhes do cliente" @fechar="visualizando = null">
      <template v-if="visualizando">
        <div class="perfil">
          <span class="avatar grande">{{ iniciais(visualizando.nome) }}</span>
          <div>
            <h3>{{ visualizando.nome }}</h3>
            <span class="faint mono">Cliente #{{ visualizando.id }}</span>
          </div>
        </div>
        <dl class="details">
          <div><dt>CPF</dt><dd class="mono">{{ formatarCpf(visualizando.cpf) }}</dd></div>
          <div><dt><Phone :size="12" /> Telefone</dt><dd class="mono">{{ formatarTelefone(visualizando.telefone) || '—' }}</dd></div>
          <div class="full"><dt><Mail :size="12" /> E-mail</dt><dd>{{ visualizando.email || '—' }}</dd></div>
        </dl>

        <h4 class="secao"><Car :size="16" /> Veículos do cliente</h4>
        <div v-if="carregandoVeiculos" class="skeleton" style="height: 40px" />
        <p v-else-if="veiculosDoCliente === null" class="field-error">Não foi possível carregar os veículos.</p>
        <p v-else-if="!veiculosDoCliente.length" class="faint">Nenhum veículo vinculado.</p>
        <ul v-else class="mini-lista">
          <li v-for="v in veiculosDoCliente" :key="v.id">
            <span class="plate">{{ formatarPlaca(v.placa) }}</span>
            <span><strong>{{ v.modelo }}</strong> <span class="muted">· {{ v.marca }}</span></span>
          </li>
        </ul>
      </template>
      <template #footer>
        <button class="btn btn-secondary" @click="visualizando = null">Fechar</button>
        <button class="btn btn-primary" @click="abrirEdicao(visualizando); visualizando = null"><Pencil :size="16" /> Editar</button>
      </template>
    </BaseModal>

    <!-- Confirmação de exclusão -->
    <ConfirmDialog
      :aberto="!!excluindo"
      :carregando="processandoExclusao"
      titulo="Excluir cliente"
      @cancelar="excluindo = null"
      @confirmar="confirmarExclusao"
    >
      Tem certeza que deseja excluir <strong style="color: var(--text)">{{ excluindo?.nome }}</strong>?
      Esta ação não pode ser desfeita. Clientes com veículos cadastrados não podem ser excluídos.
    </ConfirmDialog>
  </section>
</template>

<style scoped>
.skel-list { display: flex; flex-direction: column; gap: 12px; }
.th-sort svg { vertical-align: -1px; margin-left: 2px; opacity: 0.6; }

.perfil { display: flex; align-items: center; gap: 14px; margin-bottom: 24px; }
.perfil h3 { font-size: 17px; }
.avatar.grande { width: 52px; height: 52px; font-size: 17px; }
.details .full { grid-column: 1 / -1; }
.details dt svg { vertical-align: -1px; }

.secao {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 28px 0 12px;
  padding-top: 20px;
  border-top: 1px solid var(--border);
  font-size: 14px;
}
.mini-lista { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 8px; }
.mini-lista li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
</style>

import { AxiosError } from 'axios'

// =====================================================================
// MODO DEMONSTRAÇÃO
// Simula o back-end Spring Boot dentro do navegador, para o site do
// GitHub Pages funcionar sem servidor. Os dados ficam no localStorage.
//
// Cada função abaixo espelha um Service do Java (mesmas validações,
// mesmas mensagens e mesmos códigos HTTP) e cada "paraResposta" espelha
// um Mapper (mesmo formato de JSON que o back-end devolve).
// =====================================================================

const CHAVE = 'oficina:demo'

// ---------------- Dados de exemplo ----------------
function dadosIniciais() {
  const agora = new Date()
  const diasAtras = (d, h) => dataLocal(new Date(agora.getTime() - d * 864e5 - h * 36e5))
  return {
    clientes: [
      { id: 1, nome: 'Ana Souza', cpf: '52998224725', telefone: '11987654321', email: 'ana.souza@email.com' },
      { id: 2, nome: 'Bruno Lima', cpf: '11144477735', telefone: '11912345678', email: 'bruno.lima@email.com' },
      { id: 3, nome: 'Carla Mendes', cpf: '39053344705', telefone: '21998765432', email: null },
      { id: 4, nome: 'Diego Ferreira', cpf: '86288366757', telefone: null, email: 'diego.f@email.com' }
    ],
    carros: [
      { id: 1, modelo: 'Gol', marca: 'Volkswagen', placa: 'ABC1D23', clienteId: 1 },
      { id: 2, modelo: 'Onix', marca: 'Chevrolet', placa: 'BRA2E19', clienteId: 2 },
      { id: 3, modelo: 'HB20', marca: 'Hyundai', placa: 'QWE4R56', clienteId: 3 },
      { id: 4, modelo: 'Civic', marca: 'Honda', placa: 'HND7J89', clienteId: 1 },
      { id: 5, modelo: 'Strada', marca: 'Fiat', placa: 'FIA3T21', clienteId: 4 }
    ],
    pecas: [
      { id: 1, nome: 'Filtro de óleo', fabricante: 'Tecfil', preco: 35.9 },
      { id: 2, nome: 'Óleo 5W30 (1 L)', fabricante: 'Mobil', preco: 49.9 },
      { id: 3, nome: 'Pastilha de freio dianteira', fabricante: 'Bosch', preco: 189.0 },
      { id: 4, nome: 'Vela de ignição', fabricante: 'NGK', preco: 29.5 },
      { id: 5, nome: 'Correia dentada', fabricante: 'Gates', preco: 159.9 },
      { id: 6, nome: 'Filtro de ar', fabricante: 'Mann', preco: 42.0 }
    ],
    servicos: [
      { id: 1, maoDeObra: 80, observacao: 'Troca de óleo e filtro', carroId: 1 },
      { id: 2, maoDeObra: 150, observacao: 'Troca das pastilhas de freio', carroId: 2 },
      { id: 3, maoDeObra: 120, observacao: 'Revisão de 40 mil km', carroId: 3 },
      { id: 4, maoDeObra: 350, observacao: 'Troca da correia dentada', carroId: 4 }
    ],
    itens: [
      { id: 1, quantidade: 1, servicoId: 1, pecaId: 1 },
      { id: 2, quantidade: 4, servicoId: 1, pecaId: 2 },
      { id: 3, quantidade: 1, servicoId: 2, pecaId: 3 },
      { id: 4, quantidade: 4, servicoId: 3, pecaId: 4 },
      { id: 5, quantidade: 1, servicoId: 3, pecaId: 6 },
      { id: 6, quantidade: 1, servicoId: 4, pecaId: 5 }
    ],
    conclusoes: [
      { id: 1, servicoId: 1, dataConclusao: diasAtras(2, 3), observacao: 'Cliente retirou o carro' },
      { id: 2, servicoId: 2, dataConclusao: diasAtras(0, 5), observacao: null }
    ]
  }
}

// ---------------- "Banco de dados" (localStorage) ----------------
function carregarBanco() {
  try {
    const salvo = JSON.parse(localStorage.getItem(CHAVE))
    if (salvo && salvo.clientes) return salvo
  } catch {
    /* dados corrompidos: recomeça com os dados de exemplo */
  }
  const novo = dadosIniciais()
  localStorage.setItem(CHAVE, JSON.stringify(novo))
  return novo
}

let db = carregarBanco()
const salvarBanco = () => localStorage.setItem(CHAVE, JSON.stringify(db))

export function restaurarDadosDeExemplo() {
  db = dadosIniciais()
  salvarBanco()
}

const proximoId = (tabela) => db[tabela].reduce((max, r) => Math.max(max, r.id), 0) + 1
const achar = (tabela, id) => db[tabela].find((r) => r.id === id)
const existe = (tabela, filtro) => db[tabela].some(filtro)
const remover = (tabela, id) => (db[tabela] = db[tabela].filter((r) => r.id !== id))

// Data/hora local sem fuso e sem frações, igual ao LocalDateTime do Java
function dataLocal(d = new Date()) {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// ---------------- Erros (equivalente ao ResponseStatusException) ----------------
class ErroHttp extends Error {
  constructor(status, mensagem) {
    super(mensagem)
    this.status = status
  }
}
const BAD_REQUEST = 400
const NOT_FOUND = 404
const CONFLICT = 409
const falhar = (status, mensagem) => {
  throw new ErroHttp(status, mensagem)
}
const vazio = (texto) => texto == null || String(texto).trim() === ''

// ---------------- Mappers (Entity -> JSON de resposta) ----------------
const clienteResp = (c) => ({ id: c.id, nome: c.nome, cpf: c.cpf, telefone: c.telefone, email: c.email })
const pecaResp = (p) => ({ id: p.id, nome: p.nome, fabricante: p.fabricante, preco: p.preco })
const carroResp = (c) => ({
  id: c.id, modelo: c.modelo, marca: c.marca, placa: c.placa,
  clienteId: c.clienteId, clienteNome: achar('clientes', c.clienteId)?.nome
})
const servicoResp = (s) => {
  const carro = achar('carros', s.carroId)
  return { id: s.id, maoDeObra: s.maoDeObra, observacao: s.observacao, carroId: s.carroId, carroModelo: carro?.modelo, carroPlaca: carro?.placa }
}
const itemResp = (i) => {
  const peca = achar('pecas', i.pecaId)
  return { id: i.id, quantidade: i.quantidade, servicoId: i.servicoId, pecaId: i.pecaId, pecaNome: peca?.nome, pecaPreco: peca?.preco }
}
const conclusaoResp = (c) => {
  const servico = achar('servicos', c.servicoId)
  const carro = achar('carros', servico?.carroId)
  return {
    id: c.id, servicoId: c.servicoId,
    carroModelo: carro?.modelo, carroPlaca: carro?.placa,
    clienteNome: achar('clientes', carro?.clienteId)?.nome,
    maoDeObra: servico?.maoDeObra, observacao: c.observacao, dataConclusao: c.dataConclusao
  }
}

// ---------------- Services (regras de negócio) ----------------
// Cada recurso tem: tabela, mensagem de "não encontrado", mapper,
// validação, checagem de duplicidade e checagem antes de excluir.
const recursos = {
  clientes: {
    tabela: 'clientes',
    naoEncontrado: 'Cliente não encontrado',
    resposta: clienteResp,
    validar(d) {
      if (vazio(d.nome)) falhar(BAD_REQUEST, 'O nome é obrigatório')
      if (vazio(d.cpf)) falhar(BAD_REQUEST, 'O CPF é obrigatório')
    },
    duplicidade(d, id) {
      if (existe('clientes', (c) => c.cpf === d.cpf && c.id !== id))
        falhar(CONFLICT, id ? 'Já existe outro cliente com este CPF' : 'Já existe um cliente com este CPF')
    },
    montar: (d) => ({ nome: d.nome, cpf: d.cpf, telefone: d.telefone ?? null, email: d.email ?? null }),
    antesDeExcluir(id) {
      if (existe('carros', (c) => c.clienteId === id))
        falhar(CONFLICT, 'Não é possível excluir: o cliente possui carros cadastrados')
    }
  },

  carros: {
    tabela: 'carros',
    naoEncontrado: 'Carro não encontrado',
    resposta: carroResp,
    validar(d) {
      if (vazio(d.modelo)) falhar(BAD_REQUEST, 'O modelo é obrigatório')
      if (vazio(d.marca)) falhar(BAD_REQUEST, 'A marca é obrigatória')
      if (vazio(d.placa)) falhar(BAD_REQUEST, 'A placa é obrigatória')
      if (d.clienteId == null) falhar(BAD_REQUEST, 'O ID do cliente é obrigatório')
    },
    duplicidade(d, id) {
      if (existe('carros', (c) => c.placa === d.placa && c.id !== id))
        falhar(CONFLICT, id ? 'Já existe outro carro com esta placa' : 'Já existe um carro com esta placa')
    },
    relacionamentos(d) {
      if (!achar('clientes', d.clienteId)) falhar(NOT_FOUND, 'Cliente não encontrado')
    },
    montar: (d) => ({ modelo: d.modelo, marca: d.marca, placa: d.placa, clienteId: d.clienteId }),
    antesDeExcluir(id) {
      if (existe('servicos', (s) => s.carroId === id))
        falhar(CONFLICT, 'Não é possível excluir: o carro possui serviços cadastrados')
    }
  },

  pecas: {
    tabela: 'pecas',
    naoEncontrado: 'Peça não encontrada',
    resposta: pecaResp,
    validar(d) {
      if (vazio(d.nome)) falhar(BAD_REQUEST, 'O nome é obrigatório')
      if (vazio(d.fabricante)) falhar(BAD_REQUEST, 'O fabricante é obrigatório')
      if (d.preco == null) falhar(BAD_REQUEST, 'O preço é obrigatório')
      if (d.preco < 0) falhar(BAD_REQUEST, 'O preço não pode ser negativo')
    },
    montar: (d) => ({ nome: d.nome, fabricante: d.fabricante, preco: d.preco }),
    antesDeExcluir(id) {
      if (existe('itens', (i) => i.pecaId === id))
        falhar(CONFLICT, 'Não é possível excluir: a peça está sendo usada em um serviço')
    }
  },

  servicos: {
    tabela: 'servicos',
    naoEncontrado: 'Serviço não encontrado',
    resposta: servicoResp,
    validar(d) {
      if (d.maoDeObra == null) falhar(BAD_REQUEST, 'O valor da mão de obra é obrigatório')
      if (d.maoDeObra < 0) falhar(BAD_REQUEST, 'O valor da mão de obra não pode ser negativo')
      if (d.carroId == null) falhar(BAD_REQUEST, 'O ID do carro é obrigatório')
    },
    relacionamentos(d) {
      if (!achar('carros', d.carroId)) falhar(NOT_FOUND, 'Carro não encontrado')
    },
    montar: (d) => ({ maoDeObra: d.maoDeObra, observacao: d.observacao ?? null, carroId: d.carroId }),
    antesDeExcluir(id) {
      if (existe('itens', (i) => i.servicoId === id))
        falhar(CONFLICT, 'Não é possível excluir: o serviço possui itens cadastrados')
      if (existe('conclusoes', (c) => c.servicoId === id))
        falhar(CONFLICT, 'Não é possível excluir: o serviço está marcado como pronto')
    }
  },

  'itens-servico': {
    tabela: 'itens',
    naoEncontrado: 'Item de serviço não encontrado',
    resposta: itemResp,
    validar(d) {
      if (d.quantidade == null) falhar(BAD_REQUEST, 'A quantidade é obrigatória')
      if (d.quantidade <= 0) falhar(BAD_REQUEST, 'A quantidade deve ser maior que zero')
      if (d.servicoId == null) falhar(BAD_REQUEST, 'O ID do serviço é obrigatório')
      if (d.pecaId == null) falhar(BAD_REQUEST, 'O ID da peça é obrigatório')
    },
    relacionamentos(d) {
      if (!achar('servicos', d.servicoId)) falhar(NOT_FOUND, 'Serviço não encontrado')
      if (!achar('pecas', d.pecaId)) falhar(NOT_FOUND, 'Peça não encontrada')
    },
    montar: (d) => ({ quantidade: d.quantidade, servicoId: d.servicoId, pecaId: d.pecaId })
  },

  'conclusoes-servico': {
    tabela: 'conclusoes',
    naoEncontrado: 'Conclusão de serviço não encontrada',
    resposta: conclusaoResp,
    validar(d) {
      if (d.servicoId == null) falhar(BAD_REQUEST, 'O ID do serviço é obrigatório')
    },
    duplicidade(d, id) {
      if (existe('conclusoes', (c) => c.servicoId === d.servicoId && c.id !== id))
        falhar(CONFLICT, 'Este serviço já está marcado como pronto')
    },
    relacionamentos(d) {
      if (!achar('servicos', d.servicoId)) falhar(NOT_FOUND, 'Serviço não encontrado')
    },
    // A data vem do "servidor" no cadastro e é mantida na atualização
    montar: (d, atual) => ({ servicoId: d.servicoId, observacao: d.observacao ?? null, dataConclusao: atual?.dataConclusao ?? dataLocal() })
  }
}

// ---------------- Controller (roteia método + endereço) ----------------
function tratar(metodo, url, corpo) {
  const [, nome, idTexto] = url.split('?')[0].match(/^\/?([^/]+)(?:\/([^/]+))?\/?$/) || []
  const r = recursos[nome]
  if (!r) falhar(NOT_FOUND, 'Endereço não encontrado')
  const id = idTexto != null ? Number(idTexto) : null
  const buscar = () => achar(r.tabela, id) || falhar(NOT_FOUND, r.naoEncontrado)

  if (metodo === 'get' && id == null) return [200, db[r.tabela].map(r.resposta)]
  if (metodo === 'get') return [200, r.resposta(buscar())]

  if (metodo === 'post' && id == null) {
    r.validar(corpo)
    r.duplicidade?.(corpo, null)
    r.relacionamentos?.(corpo)
    const novo = { id: proximoId(r.tabela), ...r.montar(corpo) }
    db[r.tabela].push(novo)
    salvarBanco()
    return [201, r.resposta(novo)]
  }

  if (metodo === 'put' && id != null) {
    const atual = buscar()
    r.validar(corpo)
    r.duplicidade?.(corpo, id)
    r.relacionamentos?.(corpo)
    Object.assign(atual, r.montar(corpo, atual))
    salvarBanco()
    return [200, r.resposta(atual)]
  }

  if (metodo === 'delete' && id != null) {
    buscar()
    r.antesDeExcluir?.(id)
    remover(r.tabela, id)
    salvarBanco()
    return [204, '']
  }

  falhar(405, 'Método não suportado')
}

// ---------------- Adaptador do Axios ----------------
// O Axios chama esta função no lugar de fazer a requisição pela rede.
export function adaptadorDemo(config) {
  return new Promise((resolve, reject) => {
    // Pequeno atraso para parecer uma chamada de rede de verdade
    setTimeout(() => {
      const corpo = typeof config.data === 'string' && config.data ? JSON.parse(config.data) : config.data || {}
      try {
        const [status, data] = tratar(config.method.toLowerCase(), config.url, corpo)
        resolve({ data, status, statusText: String(status), headers: {}, config, request: {} })
      } catch (e) {
        if (!(e instanceof ErroHttp)) return reject(e)
        // Mesmo formato de erro do Spring: { status, error, message, path }
        const response = {
          data: { status: e.status, error: e.message, message: e.message, path: config.url },
          status: e.status, statusText: String(e.status), headers: {}, config, request: {}
        }
        reject(new AxiosError(e.message, AxiosError.ERR_BAD_REQUEST, config, {}, response))
      }
    }, 150)
  })
}

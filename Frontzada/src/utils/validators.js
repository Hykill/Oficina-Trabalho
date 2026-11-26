import { somenteDigitos, normalizarPlaca, lerMoeda } from './formatters'

// Regras alinhadas ao back-end:
//  - ClienteService exige nome e CPF.
//  - CarroService exige modelo, marca, placa e clienteId.
// As demais (tamanho do CPF, formato de e-mail/placa) são validações extras do front.

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
const PLACA_REGEX = /^[A-Z]{3}\d[A-Z0-9]\d{2}$/ // ABC1234 ou ABC1D23

export function validarCliente(c) {
  const erros = {}
  if (!c.nome?.trim()) erros.nome = 'O nome é obrigatório.'
  else if (c.nome.trim().length < 3) erros.nome = 'Informe ao menos 3 caracteres.'

  const cpf = somenteDigitos(c.cpf)
  if (!cpf) erros.cpf = 'O CPF é obrigatório.'
  else if (cpf.length !== 11) erros.cpf = 'O CPF deve ter 11 dígitos.'

  const tel = somenteDigitos(c.telefone)
  if (tel && (tel.length < 10 || tel.length > 11)) erros.telefone = 'Informe DDD + 8 ou 9 dígitos.'

  if (c.email?.trim() && !EMAIL_REGEX.test(c.email.trim())) erros.email = 'E-mail inválido.'
  return erros
}

export function validarCarro(c) {
  const erros = {}
  if (!c.modelo?.trim()) erros.modelo = 'O modelo é obrigatório.'
  if (!c.marca?.trim()) erros.marca = 'A marca é obrigatória.'
  const placa = normalizarPlaca(c.placa)
  if (!placa) erros.placa = 'A placa é obrigatória.'
  else if (!PLACA_REGEX.test(placa)) erros.placa = 'Formato inválido. Use ABC1234 ou ABC1D23.'
  if (!c.clienteId) erros.clienteId = 'Selecione o proprietário.'
  return erros
}

// PecaService: nome, fabricante e preço obrigatórios; preço >= 0
export function validarPeca(p) {
  const erros = {}
  if (!p.nome?.trim()) erros.nome = 'O nome é obrigatório.'
  if (!p.fabricante?.trim()) erros.fabricante = 'O fabricante é obrigatório.'
  const preco = lerMoeda(p.preco)
  if (String(p.preco ?? '').trim() === '') erros.preco = 'O preço é obrigatório.'
  else if (preco === null) erros.preco = 'Valor inválido. Ex.: 89,90'
  else if (preco < 0) erros.preco = 'O preço não pode ser negativo.'
  return erros
}

// ServicoService: mão de obra obrigatória e >= 0; carro obrigatório
export function validarServico(s) {
  const erros = {}
  if (!s.carroId) erros.carroId = 'Selecione o veículo.'
  const valor = lerMoeda(s.maoDeObra)
  if (String(s.maoDeObra ?? '').trim() === '') erros.maoDeObra = 'O valor da mão de obra é obrigatório.'
  else if (valor === null) erros.maoDeObra = 'Valor inválido. Ex.: 150,00'
  else if (valor < 0) erros.maoDeObra = 'O valor não pode ser negativo.'
  return erros
}

// ItemServicoService: quantidade > 0; peça obrigatória (o serviço vem do contexto)
export function validarItem(i) {
  const erros = {}
  if (!i.pecaId) erros.pecaId = 'Selecione a peça.'
  const qtd = Number(i.quantidade)
  if (!Number.isInteger(qtd) || qtd <= 0) erros.quantidade = 'Quantidade deve ser um inteiro maior que zero.'
  return erros
}

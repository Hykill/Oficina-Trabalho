// O banco guarda apenas dígitos (ex.: CPF "12345678900", telefone "11999999999").
// Estas funções só mudam a APRESENTAÇÃO; para a API sempre enviamos somenteDigitos().

export const somenteDigitos = (valor = '') => String(valor ?? '').replace(/\D/g, '')

export function formatarCpf(valor) {
  const d = somenteDigitos(valor).slice(0, 11)
  if (d.length <= 3) return d
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`
}

export function formatarTelefone(valor) {
  const d = somenteDigitos(valor).slice(0, 11)
  if (!d) return ''
  if (d.length <= 2) return `(${d}`
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`
}

// Placa: maiúscula, sem hífen/espaço, máx. 7 caracteres (ABC1234 ou Mercosul ABC1D23)
export const normalizarPlaca = (valor = '') =>
  String(valor ?? '').toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 7)

export function formatarPlaca(valor) {
  const p = normalizarPlaca(valor)
  // Placa antiga (ABC1234) é exibida com hífen; Mercosul (ABC1D23) sem.
  return /^[A-Z]{3}\d{4}$/.test(p) ? `${p.slice(0, 3)}-${p.slice(3)}` : p
}

export function iniciais(nome = '') {
  const partes = String(nome ?? '').trim().split(/\s+/).filter(Boolean)
  if (!partes.length) return '?'
  return (partes[0][0] + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase()
}

// ---------- Dinheiro ----------
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
export const formatarMoeda = (valor) => moeda.format(Number(valor) || 0)

// Converte o que o usuário digitou ("1.250,90", "1250.90", "12,5") em número.
// Retorna null se estiver vazio ou inválido.
export function lerMoeda(texto) {
  let t = String(texto ?? '').trim().replace(/[R$\s]/g, '')
  if (!t) return null
  if (t.includes(',')) t = t.replace(/\./g, '').replace(',', '.') // formato brasileiro
  const n = Number(t)
  return Number.isFinite(n) ? Math.round(n * 100) / 100 : null
}

// Número → texto para preencher o input ao editar (ex.: 1250.9 → "1250,90")
export const moedaParaInput = (valor) =>
  valor === null || valor === undefined ? '' : Number(valor).toFixed(2).replace('.', ',')

// ---------- Datas ----------
// O Spring envia LocalDateTime como texto ISO: "2026-11-26T18:30:00.123"
export function formatarDataHora(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// Remove acentos e deixa minúsculo — usado na pesquisa
export const normalizarTexto = (t = '') =>
  String(t ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()

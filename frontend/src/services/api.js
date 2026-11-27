import axios from 'axios'
import { adaptadorDemo } from './demoApi'

// Instância única do Axios usada por todos os services.
// "/api" é redirecionado pelo proxy do Vite para http://localhost:8080 (ver vite.config.js).
export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api'

// No GitHub Pages não há servidor: o modo demonstração responde as
// requisições dentro do próprio navegador (ver demoApi.js).
export const MODO_DEMO = import.meta.env.VITE_MODO_DEMO === 'true'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
  ...(MODO_DEMO && { adapter: adaptadorDemo })
})

/**
 * Converte qualquer erro do Axios numa mensagem legível.
 * O Spring devolve { status, error, message, path } e, como o
 * application.properties tem "include-message=always", o "message"
 * traz o texto do ResponseStatusException (ex.: "Já existe um cliente com este CPF").
 */
export function mensagemDeErro(error) {
  if (error.response) {
    const { data, status } = error.response
    if (data && typeof data === 'object' && data.message) return data.message
    if (status === 404) return 'Registro não encontrado.'
    if (status >= 500) return 'Erro interno no servidor.'
    return `Erro ${status} ao comunicar com a API.`
  }
  if (error.code === 'ECONNABORTED') return 'A API demorou demais para responder.'
  return 'Não foi possível conectar à API. Verifique se o Spring Boot está rodando na porta 8080.'
}

export default api

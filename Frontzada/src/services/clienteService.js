import api from './api'

// Espelha exatamente o ClienteController (/clientes) do back-end.
// ClienteDTO: { id, nome, cpf, telefone, email }
export default {
  listar: () => api.get('/clientes').then((r) => r.data),
  buscarPorId: (id) => api.get(`/clientes/${id}`).then((r) => r.data),
  cadastrar: (cliente) => api.post('/clientes', cliente).then((r) => r.data),
  atualizar: (id, cliente) => api.put(`/clientes/${id}`, cliente).then((r) => r.data),
  excluir: (id) => api.delete(`/clientes/${id}`)
}

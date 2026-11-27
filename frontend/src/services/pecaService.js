import api from './api'

// Espelha o PecaController (/pecas) do back-end.
// PecaDTO: { id, nome, fabricante, preco }
export default {
  listar: () => api.get('/pecas').then((r) => r.data),
  buscarPorId: (id) => api.get(`/pecas/${id}`).then((r) => r.data),
  cadastrar: (peca) => api.post('/pecas', peca).then((r) => r.data),
  atualizar: (id, peca) => api.put(`/pecas/${id}`, peca).then((r) => r.data),
  excluir: (id) => api.delete(`/pecas/${id}`)
}

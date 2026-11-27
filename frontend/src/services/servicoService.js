import api from './api'

// Espelha o ServicoController (/servicos) do back-end.
// Envio (ServicoDTO):            { maoDeObra, observacao, carroId }
// Resposta (ServicoResponseDTO): { id, maoDeObra, observacao, carroId, carroModelo, carroPlaca }
export default {
  listar: () => api.get('/servicos').then((r) => r.data),
  buscarPorId: (id) => api.get(`/servicos/${id}`).then((r) => r.data),
  cadastrar: (servico) => api.post('/servicos', servico).then((r) => r.data),
  atualizar: (id, servico) => api.put(`/servicos/${id}`, servico).then((r) => r.data),
  excluir: (id) => api.delete(`/servicos/${id}`)
}

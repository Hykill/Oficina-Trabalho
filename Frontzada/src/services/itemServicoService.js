import api from './api'

// Espelha o ItemServicoController (/itens-servico) do back-end.
// Envio (ItemServicoDTO):            { quantidade, servicoId, pecaId }
// Resposta (ItemServicoResponseDTO): { id, quantidade, servicoId, pecaId, pecaNome, pecaPreco }
// Obs.: não existe rota "itens de um serviço"; listamos todos e filtramos por servicoId.
export default {
  listar: () => api.get('/itens-servico').then((r) => r.data),
  buscarPorId: (id) => api.get(`/itens-servico/${id}`).then((r) => r.data),
  cadastrar: (item) => api.post('/itens-servico', item).then((r) => r.data),
  atualizar: (id, item) => api.put(`/itens-servico/${id}`, item).then((r) => r.data),
  excluir: (id) => api.delete(`/itens-servico/${id}`)
}

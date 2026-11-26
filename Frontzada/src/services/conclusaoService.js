import api from './api'

// Espelha o ConclusaoServicoController (/conclusoes-servico) do back-end.
// Envio (ConclusaoServicoDTO): { servicoId, observacao }
// Resposta (ConclusaoServicoResponseDTO):
//   { id, servicoId, carroModelo, carroPlaca, clienteNome, maoDeObra, observacao, dataConclusao }
// Criar = marcar o serviço como pronto. Excluir = reabrir o serviço.
export default {
  listar: () => api.get('/conclusoes-servico').then((r) => r.data),
  buscarPorId: (id) => api.get(`/conclusoes-servico/${id}`).then((r) => r.data),
  concluir: (conclusao) => api.post('/conclusoes-servico', conclusao).then((r) => r.data),
  atualizar: (id, conclusao) => api.put(`/conclusoes-servico/${id}`, conclusao).then((r) => r.data),
  reabrir: (id) => api.delete(`/conclusoes-servico/${id}`)
}

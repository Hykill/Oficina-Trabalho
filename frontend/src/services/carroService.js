import api from './api'

// Espelha o CarroController (/carros) do back-end.
// Envio (CarroDTO):            { modelo, marca, placa, clienteId }
// Resposta (CarroResponseDTO): { id, modelo, marca, placa, clienteId, clienteNome }
export default {
  listar: () => api.get('/carros').then((r) => r.data),
  buscarPorId: (id) => api.get(`/carros/${id}`).then((r) => r.data),
  cadastrar: (carro) => api.post('/carros', carro).then((r) => r.data),
  atualizar: (id, carro) => api.put(`/carros/${id}`, carro).then((r) => r.data),
  excluir: (id) => api.delete(`/carros/${id}`)
}

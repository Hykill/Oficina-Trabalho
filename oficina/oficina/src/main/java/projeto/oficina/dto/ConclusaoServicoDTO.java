package projeto.oficina.dto;

// O que o front envia para marcar um serviço como pronto.
// A data da conclusão NÃO vem do front: o Service usa a data/hora atual.
public record ConclusaoServicoDTO(
        Long servicoId,
        String observacao
) {
}

package projeto.oficina.dto;

public record ItemServicoResponseDTO(
        Long id,
        Integer quantidade,
        Long servicoId,
        Long pecaId,
        String pecaNome,
        Double pecaPreco
) {
}

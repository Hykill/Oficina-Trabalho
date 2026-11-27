package projeto.oficina.dto;

public record ServicoResponseDTO(
        Long id,
        Double maoDeObra,
        String observacao,
        Long carroId,
        String carroModelo,
        String carroPlaca
) {
}

package projeto.oficina.dto;

import java.time.LocalDateTime;

public record ConclusaoServicoResponseDTO(
        Long id,
        Long servicoId,
        String carroModelo,
        String carroPlaca,
        String clienteNome,
        Double maoDeObra,
        String observacao,
        LocalDateTime dataConclusao
) {
}

package projeto.oficina.dto;

public record CarroResponseDTO(
        Long id,
        String modelo,
        String marca,
        String placa,
        Long clienteId,
        String clienteNome
) {
}

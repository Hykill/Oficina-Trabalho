package projeto.oficina.dto;

public record CarroDTO(
        String modelo,
        String marca,
        String placa,
        Long clienteId
) {
}

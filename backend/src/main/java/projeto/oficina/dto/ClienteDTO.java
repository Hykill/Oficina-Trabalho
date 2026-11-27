package projeto.oficina.dto;

public record ClienteDTO(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String email
) {
}

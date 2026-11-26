package projeto.oficina.mapper;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.ClienteDTO;
import projeto.oficina.entity.ClienteEntity;

@Component
public class ClienteMapper {

    // DTO -> Entity (usado no cadastro)
    public ClienteEntity paraEntity(ClienteDTO dto) {
        return ClienteEntity.builder()
                .nome(dto.nome())
                .cpf(dto.cpf())
                .telefone(dto.telefone())
                .email(dto.email())
                .build();
    }

    // Entity -> DTO (usado em todas as respostas)
    public ClienteDTO paraDTO(ClienteEntity entity) {
        return new ClienteDTO(
                entity.getId(),
                entity.getNome(),
                entity.getCpf(),
                entity.getTelefone(),
                entity.getEmail()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    public void atualizarEntity(ClienteEntity entity, ClienteDTO dto) {
        entity.setNome(dto.nome());
        entity.setCpf(dto.cpf());
        entity.setTelefone(dto.telefone());
        entity.setEmail(dto.email());
    }
}

package projeto.oficina.mapper;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.PecaDTO;
import projeto.oficina.entity.PecaEntity;

@Component
public class PecaMapper {

    // DTO -> Entity (usado no cadastro)
    public PecaEntity paraEntity(PecaDTO dto) {
        return PecaEntity.builder()
                .nome(dto.nome())
                .fabricante(dto.fabricante())
                .preco(dto.preco())
                .build();
    }

    // Entity -> DTO (usado em todas as respostas)
    public PecaDTO paraDTO(PecaEntity entity) {
        return new PecaDTO(
                entity.getId(),
                entity.getNome(),
                entity.getFabricante(),
                entity.getPreco()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    public void atualizarEntity(PecaEntity entity, PecaDTO dto) {
        entity.setNome(dto.nome());
        entity.setFabricante(dto.fabricante());
        entity.setPreco(dto.preco());
    }
}

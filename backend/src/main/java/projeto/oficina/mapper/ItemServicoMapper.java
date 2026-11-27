package projeto.oficina.mapper;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.ItemServicoDTO;
import projeto.oficina.dto.ItemServicoResponseDTO;
import projeto.oficina.entity.ItemServicoEntity;
import projeto.oficina.entity.PecaEntity;
import projeto.oficina.entity.ServicoEntity;

@Component
public class ItemServicoMapper {

    // DTO -> Entity (usado no cadastro)
    // Serviço e peça chegam prontos: quem buscou no banco foi o Service
    public ItemServicoEntity paraEntity(ItemServicoDTO dto, ServicoEntity servico, PecaEntity peca) {
        return ItemServicoEntity.builder()
                .quantidade(dto.quantidade())
                .servico(servico)
                .peca(peca)
                .build();
    }

    // Entity -> DTO de resposta (usado em todas as respostas)
    public ItemServicoResponseDTO paraResponseDTO(ItemServicoEntity entity) {
        return new ItemServicoResponseDTO(
                entity.getId(),
                entity.getQuantidade(),
                entity.getServico().getId(),
                entity.getPeca().getId(),
                entity.getPeca().getNome(),
                entity.getPeca().getPreco()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    public void atualizarEntity(ItemServicoEntity entity, ItemServicoDTO dto, ServicoEntity servico, PecaEntity peca) {
        entity.setQuantidade(dto.quantidade());
        entity.setServico(servico);
        entity.setPeca(peca);
    }
}

package projeto.oficina.mapper;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.ServicoDTO;
import projeto.oficina.dto.ServicoResponseDTO;
import projeto.oficina.entity.CarroEntity;
import projeto.oficina.entity.ServicoEntity;

@Component
public class ServicoMapper {

    // DTO -> Entity (usado no cadastro)
    // O carro chega pronto: quem buscou no banco foi o Service
    public ServicoEntity paraEntity(ServicoDTO dto, CarroEntity carro) {
        return ServicoEntity.builder()
                .maoDeObra(dto.maoDeObra())
                .observacao(dto.observacao())
                .carro(carro)
                .build();
    }

    // Entity -> DTO de resposta (usado em todas as respostas)
    public ServicoResponseDTO paraResponseDTO(ServicoEntity entity) {
        return new ServicoResponseDTO(
                entity.getId(),
                entity.getMaoDeObra(),
                entity.getObservacao(),
                entity.getCarro().getId(),
                entity.getCarro().getModelo(),
                entity.getCarro().getPlaca()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    public void atualizarEntity(ServicoEntity entity, ServicoDTO dto, CarroEntity carro) {
        entity.setMaoDeObra(dto.maoDeObra());
        entity.setObservacao(dto.observacao());
        entity.setCarro(carro);
    }
}

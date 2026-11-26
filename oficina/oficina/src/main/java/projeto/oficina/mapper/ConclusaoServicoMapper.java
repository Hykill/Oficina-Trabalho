package projeto.oficina.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.ConclusaoServicoDTO;
import projeto.oficina.dto.ConclusaoServicoResponseDTO;
import projeto.oficina.entity.ConclusaoServicoEntity;
import projeto.oficina.entity.ServicoEntity;

@Component
public class ConclusaoServicoMapper {

    // DTO -> Entity (usado no cadastro)
    // O serviço chega pronto (quem buscou no banco foi o Service) e a data é definida pelo Service
    public ConclusaoServicoEntity paraEntity(ConclusaoServicoDTO dto, ServicoEntity servico, LocalDateTime dataConclusao) {
        return ConclusaoServicoEntity.builder()
                .servico(servico)
                .observacao(dto.observacao())
                .dataConclusao(dataConclusao)
                .build();
    }

    // Entity -> DTO de resposta (usado em todas as respostas)
    public ConclusaoServicoResponseDTO paraResponseDTO(ConclusaoServicoEntity entity) {
        ServicoEntity servico = entity.getServico();
        return new ConclusaoServicoResponseDTO(
                entity.getId(),
                servico.getId(),
                servico.getCarro().getModelo(),
                servico.getCarro().getPlaca(),
                servico.getCarro().getCliente().getNome(),
                servico.getMaoDeObra(),
                entity.getObservacao(),
                entity.getDataConclusao()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    // A data da conclusão original é mantida
    public void atualizarEntity(ConclusaoServicoEntity entity, ConclusaoServicoDTO dto, ServicoEntity servico) {
        entity.setServico(servico);
        entity.setObservacao(dto.observacao());
    }
}

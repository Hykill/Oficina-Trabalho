package projeto.oficina.mapper;

import org.springframework.stereotype.Component;

import projeto.oficina.dto.CarroDTO;
import projeto.oficina.dto.CarroResponseDTO;
import projeto.oficina.entity.CarroEntity;
import projeto.oficina.entity.ClienteEntity;

@Component
public class CarroMapper {

    // DTO -> Entity (usado no cadastro)
    // O cliente chega pronto: quem buscou no banco foi o Service
    public CarroEntity paraEntity(CarroDTO dto, ClienteEntity cliente) {
        return CarroEntity.builder()
                .modelo(dto.modelo())
                .marca(dto.marca())
                .placa(dto.placa())
                .cliente(cliente)
                .build();
    }

    // Entity -> DTO de resposta (usado em todas as respostas)
    public CarroResponseDTO paraResponseDTO(CarroEntity entity) {
        return new CarroResponseDTO(
                entity.getId(),
                entity.getModelo(),
                entity.getMarca(),
                entity.getPlaca(),
                entity.getCliente().getId(),
                entity.getCliente().getNome()
        );
    }

    // Copia os dados do DTO para uma Entity que já existe (usado na atualização)
    public void atualizarEntity(CarroEntity entity, CarroDTO dto, ClienteEntity cliente) {
        entity.setModelo(dto.modelo());
        entity.setMarca(dto.marca());
        entity.setPlaca(dto.placa());
        entity.setCliente(cliente);
    }
}

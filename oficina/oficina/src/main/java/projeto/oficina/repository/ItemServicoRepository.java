package projeto.oficina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projeto.oficina.entity.ItemServicoEntity;

@Repository
public interface ItemServicoRepository extends JpaRepository<ItemServicoEntity, Long> {

    boolean existsByServicoId(Long servicoId);

    boolean existsByPecaId(Long pecaId);
}

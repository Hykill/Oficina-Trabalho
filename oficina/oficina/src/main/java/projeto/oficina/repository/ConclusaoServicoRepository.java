package projeto.oficina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projeto.oficina.entity.ConclusaoServicoEntity;

@Repository
public interface ConclusaoServicoRepository extends JpaRepository<ConclusaoServicoEntity, Long> {

    boolean existsByServicoId(Long servicoId);

    boolean existsByServicoIdAndIdNot(Long servicoId, Long id);
}

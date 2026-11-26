package projeto.oficina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projeto.oficina.entity.ServicoEntity;

@Repository
public interface ServicoRepository extends JpaRepository<ServicoEntity, Long> {

    boolean existsByCarroId(Long carroId);
}

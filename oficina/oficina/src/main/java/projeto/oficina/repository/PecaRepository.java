package projeto.oficina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projeto.oficina.entity.PecaEntity;

@Repository
public interface PecaRepository extends JpaRepository<PecaEntity, Long> {
}

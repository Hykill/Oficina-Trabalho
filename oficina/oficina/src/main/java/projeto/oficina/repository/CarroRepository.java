package projeto.oficina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projeto.oficina.entity.CarroEntity;

@Repository
public interface CarroRepository extends JpaRepository<CarroEntity, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    boolean existsByClienteId(Long clienteId);
}

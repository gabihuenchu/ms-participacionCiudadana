package cl.catastrofescl.citizen.repository;

import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NeedRepository extends JpaRepository<Necesidad, UUID> {

    Page<Necesidad> findByEstadoIn(List<EstadoNecesidad> estados, Pageable pageable);

    List<Necesidad> findByCentroIdAndEstadoIn(UUID centroId, List<EstadoNecesidad> estados);

    boolean existsByCentroIdAndItemIdAndEstadoIn(UUID centroId, UUID itemId, List<EstadoNecesidad> estados);
}

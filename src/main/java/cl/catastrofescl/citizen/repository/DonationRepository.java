package cl.catastrofescl.citizen.repository;

import cl.catastrofescl.citizen.entity.Donacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donacion, UUID> {

    Optional<Donacion> findByCodigoQr(String codigoQr);

    Page<Donacion> findByUsuarioDonanteId(UUID usuarioId, Pageable pageable);

    @Query("SELECT d FROM Donacion d LEFT JOIN FETCH d.items WHERE d.codigoQr = :codigoQr")
    Optional<Donacion> findByCodigoQrWithItems(@Param("codigoQr") String codigoQr);
}

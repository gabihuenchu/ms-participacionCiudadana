package cl.catastrofescl.citizen.repository;

import cl.catastrofescl.citizen.entity.Donacion;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface DonacionRepository extends JpaRepository<Donacion, UUID> {

    Optional<Donacion> findByCodigoQr(String codigoQr);

    Page<Donacion> findByUsuarioDonanteId(UUID usuarioId, Pageable pageable);

    @Query("SELECT d FROM Donacion d LEFT JOIN FETCH d.items WHERE d.codigoQr = :codigoQr")
    Optional<Donacion> findByCodigoQrWithItems(@Param("codigoQr") String codigoQr);

    @Query("""
            SELECT COALESCE(SUM(i.cantidad), 0)
            FROM ItemDonacion i
            JOIN i.donacion d
            WHERE d.centroId = :centroId
              AND i.itemId = :itemId
              AND d.estado IN :estados
            """)
    Long sumCantidadPorCentroItemYEstados(
            @Param("centroId") UUID centroId,
            @Param("itemId") UUID itemId,
            @Param("estados") Collection<EstadoDonacion> estados
    );
}

package cl.catastrofescl.citizen.dto.response;

import cl.catastrofescl.citizen.entity.EstadoDonacion;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DonationResponse(
        UUID id,
        UUID centroId,
        UUID usuarioDonanteId,
        String codigoQr,
        EstadoDonacion estado,
        OffsetDateTime donadoEn,
        OffsetDateTime confirmadoEn,
        UUID confirmadoPorUsuarioId,
        /** Suma de cantidades de todos los ítems de la donación. */
        Integer cantidadTotal,
        List<DonationItemResponse> items
) {
}

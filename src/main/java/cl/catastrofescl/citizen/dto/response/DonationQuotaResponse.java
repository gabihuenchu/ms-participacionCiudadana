package cl.catastrofescl.citizen.dto.response;

import java.util.UUID;

/** Cupo de donación disponible para un ítem en un centro de acopio. */
public record DonationQuotaResponse(
        UUID itemId,
        UUID necesidadId,
        Long cantidadNecesaria,
        Long cantidadComprometida,
        Long cantidadMaximaDonacion
) {
}

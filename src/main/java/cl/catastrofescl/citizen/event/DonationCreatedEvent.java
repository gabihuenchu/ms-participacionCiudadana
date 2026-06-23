package cl.catastrofescl.citizen.event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DonationCreatedEvent(
        UUID eventId,
        UUID donacionId,
        UUID centroId,
        UUID usuarioDonanteId,
        String codigoQr,
        List<DonationItemPayload> items,
        OffsetDateTime donadoEn
) {
    public record DonationItemPayload(UUID itemId, Long cantidad) {
    }
}

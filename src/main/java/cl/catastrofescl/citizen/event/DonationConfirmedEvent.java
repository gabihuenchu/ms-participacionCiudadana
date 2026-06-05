package cl.catastrofescl.citizen.event;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DonationConfirmedEvent(
        UUID eventId,
        UUID donacionId,
        UUID centroId,
        UUID confirmadoPorUsuarioId,
        String codigoQr,
        List<DonationCreatedEvent.DonationItemPayload> items,
        OffsetDateTime confirmadoEn
) {
}

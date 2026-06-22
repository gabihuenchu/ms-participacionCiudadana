package cl.catastrofescl.citizen.event;

import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NeedCreatedEvent(
        UUID eventId,
        UUID necesidadId,
        UUID centroId,
        UUID itemId,
        UUID emergenciaId,
        Long cantidadNecesaria,
        PrioridadNecesidad prioridad,
        OrigenNecesidad origen,
        OffsetDateTime creadaEn
) {
}

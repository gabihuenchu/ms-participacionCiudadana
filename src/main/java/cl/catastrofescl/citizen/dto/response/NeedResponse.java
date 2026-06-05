package cl.catastrofescl.citizen.dto.response;

import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NeedResponse(
        UUID id,
        UUID centroId,
        UUID itemId,
        UUID emergenciaId,
        Integer cantidadNecesaria,
        PrioridadNecesidad prioridad,
        OrigenNecesidad origen,
        EstadoNecesidad estado,
        OffsetDateTime creadaEn,
        OffsetDateTime resueltaEn
) {
}

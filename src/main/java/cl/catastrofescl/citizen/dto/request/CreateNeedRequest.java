package cl.catastrofescl.citizen.dto.request;

import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateNeedRequest(
        @NotNull UUID centroId,
        @NotNull UUID itemId,
        UUID emergenciaId,
        @NotNull @Min(1) Long cantidadNecesaria,
        @NotNull PrioridadNecesidad prioridad
) {
}

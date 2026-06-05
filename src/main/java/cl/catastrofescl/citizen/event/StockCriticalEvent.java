package cl.catastrofescl.citizen.event;

import java.util.UUID;

public record StockCriticalEvent(
        UUID eventId,
        UUID centroId,
        UUID itemId,
        UUID emergenciaId,
        Integer stockActual,
        String nivelCriticidad,
        Integer cantidadSugerida
) {
}

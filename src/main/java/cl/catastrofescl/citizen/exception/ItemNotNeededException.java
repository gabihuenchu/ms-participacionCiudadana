package cl.catastrofescl.citizen.exception;

import java.util.UUID;

public class ItemNotNeededException extends RuntimeException {

    public ItemNotNeededException(UUID centroId, UUID itemId) {
        super(String.format(
                "El centro %s no requiere donaciones del ítem %s (sin necesidad activa o stock suficiente).",
                centroId,
                itemId
        ));
    }
}

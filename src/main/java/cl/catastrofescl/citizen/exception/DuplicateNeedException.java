package cl.catastrofescl.citizen.exception;

import java.util.UUID;

public class DuplicateNeedException extends RuntimeException {

    public DuplicateNeedException(UUID centroId, UUID itemId) {
        super("Ya existe una necesidad activa para el centro " + centroId + " e ítem " + itemId);
    }
}

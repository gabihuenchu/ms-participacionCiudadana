package cl.catastrofescl.citizen.exception;

import java.util.UUID;

public class NeedNotFoundException extends RuntimeException {

    public NeedNotFoundException(UUID id) {
        super("Necesidad con id " + id + " no encontrada");
    }
}

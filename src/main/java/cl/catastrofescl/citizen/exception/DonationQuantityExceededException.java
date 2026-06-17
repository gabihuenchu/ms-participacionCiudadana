package cl.catastrofescl.citizen.exception;

import java.util.UUID;

public class DonationQuantityExceededException extends RuntimeException {

    public DonationQuantityExceededException(UUID itemId, int cantidadSolicitada, int cantidadMaxima) {
        super(String.format(
                "La cantidad solicitada (%d) para el ítem %s supera el cupo disponible (%d). "
                        + "El centro ya tiene suficiente stock comprometido de ese recurso.",
                cantidadSolicitada,
                itemId,
                cantidadMaxima
        ));
    }
}

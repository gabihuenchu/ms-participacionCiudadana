package cl.catastrofescl.citizen.exception;

public class DonationNotFoundException extends RuntimeException {

    public DonationNotFoundException(String codigoQr) {
        super("Donación con código QR " + codigoQr + " no encontrada");
    }
}

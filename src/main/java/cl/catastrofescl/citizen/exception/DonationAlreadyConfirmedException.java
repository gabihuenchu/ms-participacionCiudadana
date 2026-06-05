package cl.catastrofescl.citizen.exception;

public class DonationAlreadyConfirmedException extends RuntimeException {

    public DonationAlreadyConfirmedException(String codigoQr) {
        super("La donación con código QR " + codigoQr + " ya fue confirmada o cancelada");
    }
}

package cl.catastrofescl.citizen.dto.response;

import java.util.UUID;

public record DonationItemResponse(
        UUID id,
        UUID itemId,
        Integer cantidad
) {
}

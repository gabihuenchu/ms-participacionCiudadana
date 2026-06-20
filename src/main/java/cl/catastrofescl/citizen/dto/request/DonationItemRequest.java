package cl.catastrofescl.citizen.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DonationItemRequest(
        @NotNull UUID itemId,
        @NotNull @Min(1) Long cantidad
) {
}

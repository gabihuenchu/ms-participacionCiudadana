package cl.catastrofescl.citizen.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateDonationRequest(
        @NotNull UUID centroId,
        @NotEmpty @Valid List<DonationItemRequest> items
) {
}

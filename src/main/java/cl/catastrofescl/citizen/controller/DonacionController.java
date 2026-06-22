package cl.catastrofescl.citizen.controller;

import cl.catastrofescl.citizen.dto.request.CreateDonationRequest;
import cl.catastrofescl.citizen.dto.response.DonationResponse;
import cl.catastrofescl.citizen.dto.response.PageResponse;
import cl.catastrofescl.citizen.security.UserContext;
import cl.catastrofescl.citizen.service.DonacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/donaciones")
@RequiredArgsConstructor
@Tag(name = "Donaciones", description = "Gestión de donaciones ciudadanas")
public class DonacionController {

    private final DonacionService donacionService;

    @PostMapping
    @PreAuthorize("hasAuthority('DONACION_REALIZAR')")
    @Operation(summary = "Registrar una donación realizada por un usuario")
    public ResponseEntity<DonationResponse> registrar(@Valid @RequestBody CreateDonationRequest request) {
        UUID usuarioId = UserContext.obtenerUsuarioId();
        DonationResponse response = donacionService.registrar(request, usuarioId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/donaciones/" + response.id()))
                .body(response);
    }

    @PostMapping("/{codigoQr}/confirmar")
    @PreAuthorize("hasAuthority('DONACION_CONFIRMAR')")
    @Operation(summary = "Confirmar recepción de donación escaneando QR")
    public DonationResponse confirmar(@PathVariable String codigoQr) {
        UUID operadorId = UserContext.obtenerUsuarioId();
        return donacionService.confirmar(codigoQr, operadorId);
    }

    @GetMapping("/mis-contribuciones")
    @Operation(summary = "Consultar historial de donaciones del usuario autenticado")
    public PageResponse<DonationResponse> misContribuciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID usuarioId = UserContext.obtenerUsuarioId();
        return donacionService.listarMisContribuciones(usuarioId, page, size);
    }
}

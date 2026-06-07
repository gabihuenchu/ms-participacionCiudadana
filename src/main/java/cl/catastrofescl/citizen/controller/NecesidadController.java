package cl.catastrofescl.citizen.controller;

import cl.catastrofescl.citizen.dto.request.CreateNeedRequest;
import cl.catastrofescl.citizen.dto.response.NeedResponse;
import cl.catastrofescl.citizen.dto.response.PageResponse;
import cl.catastrofescl.citizen.service.NecesidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/necesidades")
@RequiredArgsConstructor
@Tag(name = "Necesidades", description = "Gestión de necesidades de recursos")
public class NecesidadController {

    private final NecesidadService necesidadService;

    @GetMapping("/publicas")
    @Operation(summary = "Listar necesidades activas visibles para la ciudadanía")
    public PageResponse<NeedResponse> listarPublicas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return necesidadService.listarPublicas(page, size);
    }

    @GetMapping("/centro/{centroId}")
    @Operation(summary = "Consultar necesidades de un centro específico")
    public List<NeedResponse> listarPorCentro(@PathVariable UUID centroId) {
        return necesidadService.listarPorCentro(centroId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('NECESIDAD_GESTIONAR')")
    @Operation(summary = "Registrar una necesidad de recursos (manual)")
    public ResponseEntity<NeedResponse> crear(@Valid @RequestBody CreateNeedRequest request) {
        NeedResponse response = necesidadService.crearManual(request);
        return ResponseEntity
                .created(URI.create("/necesidades/" + response.id()))
                .body(response);
    }
}

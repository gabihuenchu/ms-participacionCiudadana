package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.request.CreateNeedRequest;
import cl.catastrofescl.citizen.dto.response.NeedResponse;
import cl.catastrofescl.citizen.dto.response.PageResponse;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.event.NeedCreatedEvent;
import cl.catastrofescl.citizen.event.StockCriticalEvent;
import cl.catastrofescl.citizen.exception.DuplicateNeedException;
import cl.catastrofescl.citizen.repository.NecesidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NecesidadService {

    private static final List<EstadoNecesidad> ESTADOS_PUBLICOS = List.of(
            EstadoNecesidad.ACTIVA,
            EstadoNecesidad.PARCIALMENTE_CUBIERTA
    );

    private final NecesidadRepository necesidadRepository;
    private final NecesidadMapper necesidadMapper;
    private final EventPublisher eventPublisher;
    private final DonacionCapacidadService donacionCapacidadService;

    @Transactional(readOnly = true)
    public PageResponse<NeedResponse> listarPublicas(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "prioridad", "creadaEn"));
        Page<Necesidad> result = necesidadRepository.findByEstadoIn(ESTADOS_PUBLICOS, pageable);
        return toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public List<NeedResponse> listarPorCentro(UUID centroId) {
        return necesidadRepository.findByCentroIdAndEstadoIn(centroId, ESTADOS_PUBLICOS).stream()
                .map(n -> necesidadMapper.toResponse(
                        n,
                        donacionCapacidadService.calcularCantidadComprometida(centroId, n.getItemId())
                ))
                .toList();
    }

    @Transactional
    public NeedResponse crearManual(CreateNeedRequest request, UUID usuarioId) {
        return crearNecesidad(
                request.centroId(),
                request.itemId(),
                request.emergenciaId(),
                request.cantidadNecesaria(),
                request.prioridad(),
                OrigenNecesidad.MANUAL,
                usuarioId
        );
    }

    @Transactional
    public NeedResponse crearAutomatica(StockCriticalEvent event) {
        PrioridadNecesidad prioridad = mapearPrioridad(event.nivelCriticidad());
        long cantidad = event.cantidadSugerida() != null && event.cantidadSugerida() > 0
                ? event.cantidadSugerida()
                : 1L;

        if (necesidadRepository.existsByCentroIdAndItemIdAndEstadoIn(
                event.centroId(), event.itemId(), ESTADOS_PUBLICOS)) {
            throw new DuplicateNeedException(event.centroId(), event.itemId());
        }

        return crearNecesidad(
                event.centroId(),
                event.itemId(),
                event.emergenciaId(),
                cantidad,
                prioridad,
                OrigenNecesidad.AUTOMATICO,
                null
        );
    }

    private NeedResponse crearNecesidad(
            UUID centroId,
            UUID itemId,
            UUID emergenciaId,
            long cantidad,
            PrioridadNecesidad prioridad,
            OrigenNecesidad origen,
            UUID usuarioId
    ) {
        OffsetDateTime ahora = OffsetDateTime.now();
        Necesidad necesidad = Necesidad.builder()
                .id(UUID.randomUUID())
                .centroId(centroId)
                .itemId(itemId)
                .emergenciaId(emergenciaId)
                .cantidadNecesaria(cantidad)
                .prioridad(prioridad)
                .origen(origen)
                .estado(EstadoNecesidad.ACTIVA)
                .creadaEn(ahora)
                .build();

        Necesidad guardada = necesidadRepository.save(necesidad);

        eventPublisher.publicar("need.created", new NeedCreatedEvent(
                UUID.randomUUID(),
                guardada.getId(),
                guardada.getCentroId(),
                guardada.getItemId(),
                guardada.getEmergenciaId(),
                guardada.getCantidadNecesaria(),
                guardada.getPrioridad(),
                guardada.getOrigen(),
                usuarioId,
                guardada.getCreadaEn()
        ));

        return necesidadMapper.toResponse(guardada, 0L);
    }

    private PrioridadNecesidad mapearPrioridad(String nivelCriticidad) {
        if (nivelCriticidad == null) {
            return PrioridadNecesidad.ALTO;
        }
        return switch (nivelCriticidad.toUpperCase()) {
            case "AGOTADO", "CRITICO" -> PrioridadNecesidad.CRITICO;
            case "ALTO" -> PrioridadNecesidad.ALTO;
            case "MEDIO" -> PrioridadNecesidad.MEDIO;
            default -> PrioridadNecesidad.BAJO;
        };
    }

    private PageResponse<NeedResponse> toPageResponse(Page<Necesidad> page) {
        return new PageResponse<>(
                page.getContent().stream()
                        .map(n -> necesidadMapper.toResponse(
                                n,
                                donacionCapacidadService.calcularCantidadComprometida(n.getCentroId(), n.getItemId())
                        ))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}

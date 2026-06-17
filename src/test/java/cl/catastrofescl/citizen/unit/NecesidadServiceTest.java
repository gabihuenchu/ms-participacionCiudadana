package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.dto.request.CreateNeedRequest;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.event.StockCriticalEvent;
import cl.catastrofescl.citizen.exception.DuplicateNeedException;
import cl.catastrofescl.citizen.repository.NecesidadRepository;
import cl.catastrofescl.citizen.service.DonacionCapacidadService;
import cl.catastrofescl.citizen.service.EventPublisher;
import cl.catastrofescl.citizen.service.NecesidadMapper;
import cl.catastrofescl.citizen.service.NecesidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NecesidadServiceTest {

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private NecesidadMapper necesidadMapper;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private DonacionCapacidadService donacionCapacidadService;

    @InjectMocks
    private NecesidadService necesidadService;

    private UUID centroId;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        centroId = UUID.randomUUID();
        itemId = UUID.randomUUID();
    }

    @Test
    void crearManual_persisteNecesidadActiva() {
        var request = new CreateNeedRequest(centroId, itemId, null, 10, PrioridadNecesidad.ALTO);

        when(necesidadRepository.save(any(Necesidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(necesidadMapper.toResponse(any(Necesidad.class), eq(0))).thenAnswer(invocation -> {
            Necesidad n = invocation.getArgument(0);
            return new cl.catastrofescl.citizen.dto.response.NeedResponse(
                    n.getId(), n.getCentroId(), n.getItemId(), n.getEmergenciaId(),
                    n.getCantidadNecesaria(), 0, n.getCantidadNecesaria(),
                    n.getPrioridad(), n.getOrigen(), n.getEstado(),
                    n.getCreadaEn(), n.getResueltaEn()
            );
        });

        var response = necesidadService.crearManual(request);

        ArgumentCaptor<Necesidad> captor = ArgumentCaptor.forClass(Necesidad.class);
        verify(necesidadRepository).save(captor.capture());
        Necesidad guardada = captor.getValue();

        assertThat(guardada.getCentroId()).isEqualTo(centroId);
        assertThat(guardada.getOrigen()).isEqualTo(OrigenNecesidad.MANUAL);
        assertThat(guardada.getEstado()).isEqualTo(EstadoNecesidad.ACTIVA);
        assertThat(response.prioridad()).isEqualTo(PrioridadNecesidad.ALTO);
        verify(eventPublisher).publicar(org.mockito.ArgumentMatchers.eq("need.created"), any());
    }

    @Test
    void crearAutomatica_cuandoDuplicada_lanzaDuplicateNeedException() {
        var event = new StockCriticalEvent(
                UUID.randomUUID(), centroId, itemId, null, 0, "CRITICO", 5
        );
        when(necesidadRepository.existsByCentroIdAndItemIdAndEstadoIn(eq(centroId), eq(itemId), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> necesidadService.crearAutomatica(event))
                .isInstanceOf(DuplicateNeedException.class);

        verify(necesidadRepository, never()).save(any());
    }

    @Test
    void crearAutomatica_mapeaPrioridadCriticaDesdeAgotado() {
        var event = new StockCriticalEvent(
                UUID.randomUUID(), centroId, itemId, null, 0, "AGOTADO", 3
        );
        when(necesidadRepository.existsByCentroIdAndItemIdAndEstadoIn(any(), any(), any())).thenReturn(false);
        when(necesidadRepository.save(any(Necesidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(necesidadMapper.toResponse(any(Necesidad.class), eq(0))).thenAnswer(invocation -> {
            Necesidad n = invocation.getArgument(0);
            return new cl.catastrofescl.citizen.dto.response.NeedResponse(
                    n.getId(), n.getCentroId(), n.getItemId(), n.getEmergenciaId(),
                    n.getCantidadNecesaria(), 0, n.getCantidadNecesaria(),
                    n.getPrioridad(), n.getOrigen(), n.getEstado(),
                    n.getCreadaEn(), n.getResueltaEn()
            );
        });

        var response = necesidadService.crearAutomatica(event);

        assertThat(response.prioridad()).isEqualTo(PrioridadNecesidad.CRITICO);
        assertThat(response.origen()).isEqualTo(OrigenNecesidad.AUTOMATICO);
    }

    @Test
    void listarPublicas_retornaPaginaDelRepositorio() {
        Necesidad necesidad = Necesidad.builder()
                .id(UUID.randomUUID())
                .centroId(centroId)
                .itemId(itemId)
                .cantidadNecesaria(5)
                .prioridad(PrioridadNecesidad.MEDIO)
                .origen(OrigenNecesidad.MANUAL)
                .estado(EstadoNecesidad.ACTIVA)
                .creadaEn(java.time.OffsetDateTime.now())
                .build();

        when(necesidadRepository.findByEstadoIn(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(necesidad)));
        when(donacionCapacidadService.calcularCantidadComprometida(centroId, itemId)).thenReturn(2);
        when(necesidadMapper.toResponse(necesidad, 2)).thenReturn(
                new cl.catastrofescl.citizen.dto.response.NeedResponse(
                        necesidad.getId(), centroId, itemId, null, 5, 2, 3,
                        PrioridadNecesidad.MEDIO, OrigenNecesidad.MANUAL, EstadoNecesidad.ACTIVA,
                        necesidad.getCreadaEn(), null
                )
        );

        var page = necesidadService.listarPublicas(0, 20);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
    }
}

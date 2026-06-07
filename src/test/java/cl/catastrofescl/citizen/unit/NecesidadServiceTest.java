package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.dto.request.CreateNeedRequest;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.repository.NecesidadRepository;
import cl.catastrofescl.citizen.service.EventPublisher;
import cl.catastrofescl.citizen.service.NecesidadMapper;
import cl.catastrofescl.citizen.service.NecesidadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private NecesidadService necesidadService;

    @Test
    void crearManual_persisteNecesidadActiva() {
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        var request = new CreateNeedRequest(centroId, itemId, null, 10, PrioridadNecesidad.ALTO);

        when(necesidadRepository.save(any(Necesidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(necesidadMapper.toResponse(any(Necesidad.class))).thenAnswer(invocation -> {
            Necesidad n = invocation.getArgument(0);
            return new cl.catastrofescl.citizen.dto.response.NeedResponse(
                    n.getId(), n.getCentroId(), n.getItemId(), n.getEmergenciaId(),
                    n.getCantidadNecesaria(), n.getPrioridad(), n.getOrigen(), n.getEstado(),
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
}

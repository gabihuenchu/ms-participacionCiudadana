package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.request.CreateNeedRequest;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.repository.NeedRepository;
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
class NeedServiceTest {

    @Mock
    private NeedRepository needRepository;

    @Mock
    private NeedMapper needMapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private NeedService needService;

    @Test
    void crearManual_persisteNecesidadActiva() {
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        var request = new CreateNeedRequest(centroId, itemId, null, 10, PrioridadNecesidad.ALTO);

        when(needRepository.save(any(Necesidad.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(needMapper.toResponse(any(Necesidad.class))).thenAnswer(invocation -> {
            Necesidad n = invocation.getArgument(0);
            return new cl.catastrofescl.citizen.dto.response.NeedResponse(
                    n.getId(), n.getCentroId(), n.getItemId(), n.getEmergenciaId(),
                    n.getCantidadNecesaria(), n.getPrioridad(), n.getOrigen(), n.getEstado(),
                    n.getCreadaEn(), n.getResueltaEn()
            );
        });

        var response = needService.crearManual(request);

        ArgumentCaptor<Necesidad> captor = ArgumentCaptor.forClass(Necesidad.class);
        verify(needRepository).save(captor.capture());
        Necesidad guardada = captor.getValue();

        assertThat(guardada.getCentroId()).isEqualTo(centroId);
        assertThat(guardada.getOrigen()).isEqualTo(OrigenNecesidad.MANUAL);
        assertThat(guardada.getEstado()).isEqualTo(EstadoNecesidad.ACTIVA);
        assertThat(response.prioridad()).isEqualTo(PrioridadNecesidad.ALTO);
        verify(eventPublisher).publicar(org.mockito.ArgumentMatchers.eq("need.created"), any());
    }
}

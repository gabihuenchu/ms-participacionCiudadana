package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.dto.request.CreateDonationRequest;
import cl.catastrofescl.citizen.dto.request.DonationItemRequest;
import cl.catastrofescl.citizen.dto.response.DonationResponse;
import cl.catastrofescl.citizen.entity.Donacion;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.entity.ItemDonacion;
import cl.catastrofescl.citizen.exception.DonationAlreadyConfirmedException;
import cl.catastrofescl.citizen.exception.DonationNotFoundException;
import cl.catastrofescl.citizen.repository.DonacionRepository;
import cl.catastrofescl.citizen.service.DonacionCapacidadService;
import cl.catastrofescl.citizen.service.DonacionMapper;
import cl.catastrofescl.citizen.service.DonacionService;
import cl.catastrofescl.citizen.service.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock
    private DonacionRepository donacionRepository;

    @Spy
    private DonacionMapper donacionMapper = new DonacionMapper();

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private DonacionCapacidadService donacionCapacidadService;

    @InjectMocks
    private DonacionService donacionService;

    private UUID centroId;
    private UUID itemId;
    private UUID donanteId;
    private UUID operadorId;

    @BeforeEach
    void setUp() {
        centroId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        donanteId = UUID.randomUUID();
        operadorId = UUID.randomUUID();
    }

    @Test
    void registrar_persisteDonacionPendienteYPublicaEvento() {
        var request = new CreateDonationRequest(
                centroId,
                List.of(new DonationItemRequest(itemId, 3L))
        );

        when(donacionRepository.save(any(Donacion.class))).thenAnswer(inv -> inv.getArgument(0));

        DonationResponse response = donacionService.registrar(request, donanteId);

        ArgumentCaptor<Donacion> captor = ArgumentCaptor.forClass(Donacion.class);
        verify(donacionRepository).save(captor.capture());

        Donacion guardada = captor.getValue();
        assertThat(guardada.getCentroId()).isEqualTo(centroId);
        assertThat(guardada.getUsuarioDonanteId()).isEqualTo(donanteId);
        assertThat(guardada.getEstado()).isEqualTo(EstadoDonacion.PENDIENTE);
        assertThat(guardada.getCodigoQr()).isNotBlank();
        assertThat(guardada.getItems()).hasSize(1);
        assertThat(response.estado()).isEqualTo(EstadoDonacion.PENDIENTE);
        verify(eventPublisher).publicar(eq("donation.created"), any());
    }

    @Test
    void confirmar_actualizaEstadoYPublicaEvento() {
        Donacion donacion = donacionPendiente("qr-123");
        when(donacionRepository.findByCodigoQrWithItems("qr-123")).thenReturn(Optional.of(donacion));
        when(donacionRepository.save(any(Donacion.class))).thenAnswer(inv -> inv.getArgument(0));

        DonationResponse response = donacionService.confirmar("qr-123", operadorId);

        assertThat(response.estado()).isEqualTo(EstadoDonacion.CONFIRMADA);
        assertThat(response.confirmadoPorUsuarioId()).isEqualTo(operadorId);
        verify(donacionCapacidadService).actualizarNecesidadTrasConfirmacion(centroId, itemId);
        verify(eventPublisher).publicar(eq("donation.confirmed"), any());
    }

    @Test
    void confirmar_cuandoNoExiste_lanzaDonationNotFoundException() {
        when(donacionRepository.findByCodigoQrWithItems("inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donacionService.confirmar("inexistente", operadorId))
                .isInstanceOf(DonationNotFoundException.class);

        verify(donacionRepository, never()).save(any());
        verify(eventPublisher, never()).publicar(any(), any());
    }

    @Test
    void confirmar_cuandoYaConfirmada_lanzaDonationAlreadyConfirmedException() {
        Donacion donacion = donacionPendiente("qr-confirmada");
        donacion.setEstado(EstadoDonacion.CONFIRMADA);
        when(donacionRepository.findByCodigoQrWithItems("qr-confirmada")).thenReturn(Optional.of(donacion));

        assertThatThrownBy(() -> donacionService.confirmar("qr-confirmada", operadorId))
                .isInstanceOf(DonationAlreadyConfirmedException.class);

        verify(donacionRepository, never()).save(any());
    }

    @Test
    void listarMisContribuciones_retornaPaginaMapeada() {
        Donacion donacion = donacionPendiente("qr-list");
        when(donacionRepository.findByUsuarioDonanteId(eq(donanteId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(donacion)));

        var page = donacionService.listarMisContribuciones(donanteId, 0, 20);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().codigoQr()).isEqualTo("qr-list");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    private Donacion donacionPendiente(String codigoQr) {
        Donacion donacion = Donacion.builder()
                .id(UUID.randomUUID())
                .centroId(centroId)
                .usuarioDonanteId(donanteId)
                .codigoQr(codigoQr)
                .estado(EstadoDonacion.PENDIENTE)
                .donadoEn(OffsetDateTime.now())
                .build();
        donacion.getItems().add(ItemDonacion.builder()
                .id(UUID.randomUUID())
                .donacion(donacion)
                .itemId(itemId)
                .cantidad(2L)
                .build());
        return donacion;
    }
}

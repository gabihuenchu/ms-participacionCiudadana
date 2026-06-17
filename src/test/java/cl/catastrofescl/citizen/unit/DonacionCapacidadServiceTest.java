package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.exception.DonationQuantityExceededException;
import cl.catastrofescl.citizen.exception.ItemNotNeededException;
import cl.catastrofescl.citizen.repository.DonacionRepository;
import cl.catastrofescl.citizen.repository.NecesidadRepository;
import cl.catastrofescl.citizen.service.DonacionCapacidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonacionCapacidadServiceTest {

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private DonacionRepository donacionRepository;

    @InjectMocks
    private DonacionCapacidadService donacionCapacidadService;

    private UUID centroId;
    private UUID itemId;
    private Necesidad necesidad;

    @BeforeEach
    void setUp() {
        centroId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        necesidad = Necesidad.builder()
                .id(UUID.randomUUID())
                .centroId(centroId)
                .itemId(itemId)
                .cantidadNecesaria(10)
                .prioridad(PrioridadNecesidad.MEDIO)
                .origen(OrigenNecesidad.MANUAL)
                .estado(EstadoNecesidad.ACTIVA)
                .creadaEn(OffsetDateTime.now())
                .build();
    }

    @Test
    void validarCantidadDonacion_sinNecesidadActiva_lanzaItemNotNeededException() {
        when(necesidadRepository.findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                eq(centroId), eq(itemId), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donacionCapacidadService.validarCantidadDonacion(centroId, itemId, 1))
                .isInstanceOf(ItemNotNeededException.class);
    }

    @Test
    void validarCantidadDonacion_cuandoCupoAgotado_lanzaItemNotNeededException() {
        when(necesidadRepository.findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                eq(centroId), eq(itemId), any())).thenReturn(Optional.of(necesidad));
        when(donacionRepository.sumCantidadPorCentroItemYEstados(eq(centroId), eq(itemId), any()))
                .thenReturn(10);

        assertThatThrownBy(() -> donacionCapacidadService.validarCantidadDonacion(centroId, itemId, 1))
                .isInstanceOf(ItemNotNeededException.class);
    }

    @Test
    void validarCantidadDonacion_cuandoExcedeCupo_lanzaDonationQuantityExceededException() {
        when(necesidadRepository.findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                eq(centroId), eq(itemId), any())).thenReturn(Optional.of(necesidad));
        when(donacionRepository.sumCantidadPorCentroItemYEstados(eq(centroId), eq(itemId), any()))
                .thenReturn(7);

        assertThatThrownBy(() -> donacionCapacidadService.validarCantidadDonacion(centroId, itemId, 4))
                .isInstanceOf(DonationQuantityExceededException.class);
    }

    @Test
    void calcularCantidadMaximaDonacion_restaComprometidaDeNecesaria() {
        when(necesidadRepository.findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                eq(centroId), eq(itemId), any())).thenReturn(Optional.of(necesidad));
        when(donacionRepository.sumCantidadPorCentroItemYEstados(eq(centroId), eq(itemId), any()))
                .thenReturn(3);

        assertThat(donacionCapacidadService.calcularCantidadMaximaDonacion(centroId, itemId)).isEqualTo(7);
    }

    @Test
    void actualizarNecesidadTrasConfirmacion_marcaResueltaCuandoSeAlcanzaMeta() {
        when(necesidadRepository.findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                eq(centroId), eq(itemId), any())).thenReturn(Optional.of(necesidad));
        when(donacionRepository.sumCantidadPorCentroItemYEstados(
                eq(centroId), eq(itemId), eq(List.of(EstadoDonacion.CONFIRMADA))
        )).thenReturn(10);

        donacionCapacidadService.actualizarNecesidadTrasConfirmacion(centroId, itemId);

        assertThat(necesidad.getEstado()).isEqualTo(EstadoNecesidad.RESUELTA);
        assertThat(necesidad.getResueltaEn()).isNotNull();
        verify(necesidadRepository).save(necesidad);
    }
}

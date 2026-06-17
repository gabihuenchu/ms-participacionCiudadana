package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.response.DonationQuotaResponse;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.Necesidad;
import cl.catastrofescl.citizen.exception.DonationQuantityExceededException;
import cl.catastrofescl.citizen.exception.ItemNotNeededException;
import cl.catastrofescl.citizen.repository.DonacionRepository;
import cl.catastrofescl.citizen.repository.NecesidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DonacionCapacidadService {

    private static final List<EstadoDonacion> ESTADOS_COMPROMETIDOS = List.of(
            EstadoDonacion.PENDIENTE,
            EstadoDonacion.CONFIRMADA
    );

    private static final List<EstadoNecesidad> ESTADOS_NECESIDAD_ABIERTA = List.of(
            EstadoNecesidad.ACTIVA,
            EstadoNecesidad.PARCIALMENTE_CUBIERTA
    );

    private final NecesidadRepository necesidadRepository;
    private final DonacionRepository donacionRepository;

    @Transactional(readOnly = true)
    public List<DonationQuotaResponse> listarCuposPorCentro(UUID centroId) {
        return necesidadRepository.findByCentroIdAndEstadoIn(centroId, ESTADOS_NECESIDAD_ABIERTA).stream()
                .map(n -> toQuota(centroId, n))
                .filter(q -> q.cantidadMaximaDonacion() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public int calcularCantidadComprometida(UUID centroId, UUID itemId) {
        Integer total = donacionRepository.sumCantidadPorCentroItemYEstados(
                centroId, itemId, ESTADOS_COMPROMETIDOS
        );
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public int calcularCantidadMaximaDonacion(UUID centroId, UUID itemId) {
        return necesidadRepository
                .findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                        centroId, itemId, ESTADOS_NECESIDAD_ABIERTA
                )
                .map(n -> Math.max(0, n.getCantidadNecesaria() - calcularCantidadComprometida(centroId, itemId)))
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public void validarCantidadDonacion(UUID centroId, UUID itemId, int cantidadSolicitada) {
        Necesidad necesidad = necesidadRepository
                .findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                        centroId, itemId, ESTADOS_NECESIDAD_ABIERTA
                )
                .orElseThrow(() -> new ItemNotNeededException(centroId, itemId));

        int maxima = Math.max(
                0,
                necesidad.getCantidadNecesaria() - calcularCantidadComprometida(centroId, itemId)
        );

        if (maxima <= 0) {
            throw new ItemNotNeededException(centroId, itemId);
        }

        if (cantidadSolicitada > maxima) {
            throw new DonationQuantityExceededException(itemId, cantidadSolicitada, maxima);
        }
    }

    @Transactional
    public void actualizarNecesidadTrasConfirmacion(UUID centroId, UUID itemId) {
        necesidadRepository
                .findFirstByCentroIdAndItemIdAndEstadoInOrderByPrioridadDescCreadaEnDesc(
                        centroId, itemId, ESTADOS_NECESIDAD_ABIERTA
                )
                .ifPresent(necesidad -> {
                    int confirmada = valorSeguro(donacionRepository.sumCantidadPorCentroItemYEstados(
                            centroId, itemId, List.of(EstadoDonacion.CONFIRMADA)
                    ));

                    if (confirmada >= necesidad.getCantidadNecesaria()) {
                        necesidad.setEstado(EstadoNecesidad.RESUELTA);
                        necesidad.setResueltaEn(OffsetDateTime.now());
                    } else if (confirmada > 0) {
                        necesidad.setEstado(EstadoNecesidad.PARCIALMENTE_CUBIERTA);
                    }
                    necesidadRepository.save(necesidad);
                });
    }

    private static int valorSeguro(Integer value) {
        return value != null ? value : 0;
    }

    DonationQuotaResponse toQuota(UUID centroId, Necesidad necesidad) {
        int comprometida = calcularCantidadComprometida(centroId, necesidad.getItemId());
        int maxima = Math.max(0, necesidad.getCantidadNecesaria() - comprometida);
        return new DonationQuotaResponse(
                necesidad.getItemId(),
                necesidad.getId(),
                necesidad.getCantidadNecesaria(),
                comprometida,
                maxima
        );
    }
}

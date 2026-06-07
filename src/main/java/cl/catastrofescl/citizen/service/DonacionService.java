package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.request.CreateDonationRequest;
import cl.catastrofescl.citizen.dto.request.DonationItemRequest;
import cl.catastrofescl.citizen.dto.response.DonationResponse;
import cl.catastrofescl.citizen.dto.response.PageResponse;
import cl.catastrofescl.citizen.entity.Donacion;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.entity.ItemDonacion;
import cl.catastrofescl.citizen.event.DonationConfirmedEvent;
import cl.catastrofescl.citizen.event.DonationCreatedEvent;
import cl.catastrofescl.citizen.exception.DonationAlreadyConfirmedException;
import cl.catastrofescl.citizen.exception.DonationNotFoundException;
import cl.catastrofescl.citizen.repository.DonacionRepository;
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
public class DonacionService {

    private final DonacionRepository donacionRepository;
    private final DonacionMapper donacionMapper;
    private final EventPublisher eventPublisher;

    @Transactional
    public DonationResponse registrar(CreateDonationRequest request, UUID usuarioDonanteId) {
        OffsetDateTime ahora = OffsetDateTime.now();
        String codigoQr = UUID.randomUUID().toString();

        Donacion donacion = Donacion.builder()
                .id(UUID.randomUUID())
                .centroId(request.centroId())
                .usuarioDonanteId(usuarioDonanteId)
                .codigoQr(codigoQr)
                .estado(EstadoDonacion.PENDIENTE)
                .donadoEn(ahora)
                .build();

        for (DonationItemRequest itemRequest : request.items()) {
            ItemDonacion item = ItemDonacion.builder()
                    .id(UUID.randomUUID())
                    .donacion(donacion)
                    .itemId(itemRequest.itemId())
                    .cantidad(itemRequest.cantidad())
                    .build();
            donacion.getItems().add(item);
        }

        Donacion guardada = donacionRepository.save(donacion);

        List<DonationCreatedEvent.DonationItemPayload> itemsPayload = guardada.getItems().stream()
                .map(i -> new DonationCreatedEvent.DonationItemPayload(i.getItemId(), i.getCantidad()))
                .toList();

        eventPublisher.publicar("donation.created", new DonationCreatedEvent(
                UUID.randomUUID(),
                guardada.getId(),
                guardada.getCentroId(),
                guardada.getUsuarioDonanteId(),
                guardada.getCodigoQr(),
                itemsPayload,
                guardada.getDonadoEn()
        ));

        return donacionMapper.toResponse(guardada);
    }

    @Transactional
    public DonationResponse confirmar(String codigoQr, UUID operadorId) {
        Donacion donacion = donacionRepository.findByCodigoQrWithItems(codigoQr)
                .orElseThrow(() -> new DonationNotFoundException(codigoQr));

        if (donacion.getEstado() == EstadoDonacion.CONFIRMADA) {
            throw new DonationAlreadyConfirmedException(codigoQr);
        }

        if (donacion.getEstado() == EstadoDonacion.CANCELADA) {
            throw new DonationAlreadyConfirmedException(codigoQr);
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        donacion.setEstado(EstadoDonacion.CONFIRMADA);
        donacion.setConfirmadoEn(ahora);
        donacion.setConfirmadoPorUsuarioId(operadorId);

        Donacion guardada = donacionRepository.save(donacion);

        List<DonationCreatedEvent.DonationItemPayload> itemsPayload = guardada.getItems().stream()
                .map(i -> new DonationCreatedEvent.DonationItemPayload(i.getItemId(), i.getCantidad()))
                .toList();

        eventPublisher.publicar("donation.confirmed", new DonationConfirmedEvent(
                UUID.randomUUID(),
                guardada.getId(),
                guardada.getCentroId(),
                operadorId,
                guardada.getCodigoQr(),
                itemsPayload,
                guardada.getConfirmadoEn()
        ));

        return donacionMapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public PageResponse<DonationResponse> listarMisContribuciones(UUID usuarioId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "donadoEn"));
        Page<Donacion> result = donacionRepository.findByUsuarioDonanteId(usuarioId, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(donacionMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}

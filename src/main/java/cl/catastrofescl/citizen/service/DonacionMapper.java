package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.response.DonationItemResponse;
import cl.catastrofescl.citizen.dto.response.DonationResponse;
import cl.catastrofescl.citizen.entity.Donacion;
import cl.catastrofescl.citizen.entity.ItemDonacion;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DonacionMapper {

    public DonationResponse toResponse(Donacion donacion) {
        List<DonationItemResponse> items = donacion.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new DonationResponse(
                donacion.getId(),
                donacion.getCentroId(),
                donacion.getUsuarioDonanteId(),
                donacion.getCodigoQr(),
                donacion.getEstado(),
                donacion.getDonadoEn(),
                donacion.getConfirmadoEn(),
                donacion.getConfirmadoPorUsuarioId(),
                items
        );
    }

    private DonationItemResponse toItemResponse(ItemDonacion item) {
        return new DonationItemResponse(item.getId(), item.getItemId(), item.getCantidad());
    }
}

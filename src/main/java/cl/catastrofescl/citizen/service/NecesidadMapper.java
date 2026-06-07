package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.response.NeedResponse;
import cl.catastrofescl.citizen.entity.Necesidad;
import org.springframework.stereotype.Component;

@Component
public class NecesidadMapper {

    public NeedResponse toResponse(Necesidad necesidad) {
        return new NeedResponse(
                necesidad.getId(),
                necesidad.getCentroId(),
                necesidad.getItemId(),
                necesidad.getEmergenciaId(),
                necesidad.getCantidadNecesaria(),
                necesidad.getPrioridad(),
                necesidad.getOrigen(),
                necesidad.getEstado(),
                necesidad.getCreadaEn(),
                necesidad.getResueltaEn()
        );
    }
}

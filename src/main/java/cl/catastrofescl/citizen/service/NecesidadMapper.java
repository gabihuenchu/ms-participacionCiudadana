package cl.catastrofescl.citizen.service;

import cl.catastrofescl.citizen.dto.response.NeedResponse;
import cl.catastrofescl.citizen.entity.Necesidad;
import org.springframework.stereotype.Component;

@Component
public class NecesidadMapper {

    public NeedResponse toResponse(Necesidad necesidad, long cantidadComprometida) {
        long restante = Math.max(0L, necesidad.getCantidadNecesaria() - cantidadComprometida);
        return new NeedResponse(
                necesidad.getId(),
                necesidad.getCentroId(),
                necesidad.getItemId(),
                necesidad.getEmergenciaId(),
                necesidad.getCantidadNecesaria(),
                cantidadComprometida,
                restante,
                necesidad.getPrioridad(),
                necesidad.getOrigen(),
                necesidad.getEstado(),
                necesidad.getCreadaEn(),
                necesidad.getResueltaEn()
        );
    }
}

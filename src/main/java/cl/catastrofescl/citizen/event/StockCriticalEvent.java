package cl.catastrofescl.citizen.event;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * Contrato del evento stock.critical consumido desde ms-resources (fuente de verdad del inventario).
 *
 * <p>El productor (ms-resources, StockCriticoEvento) publica los campos con nomenclatura en español
 * (eventoId, itemCatalogoId, estadoCriticidad) y campos adicionales del sobre del evento (ocurridoEn,
 * correlacionId, versionEvento, fuente, inventarioId, categoria, umbralMinimo, umbralOptimo). Se usan
 * alias Jackson para mapear los nombres y se ignoran las propiedades no declaradas.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StockCriticalEvent(
        @JsonAlias({"eventoId"}) UUID eventId,
        UUID centroId,
        @JsonAlias({"itemCatalogoId"}) UUID itemId,
        UUID emergenciaId,
        Long stockActual,
        @JsonAlias({"estadoCriticidad"}) String nivelCriticidad,
        Long cantidadSugerida
) {
}

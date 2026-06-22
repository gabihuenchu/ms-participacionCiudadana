package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.event.DonationConfirmedEvent;
import cl.catastrofescl.citizen.event.DonationCreatedEvent;
import cl.catastrofescl.citizen.event.NeedCreatedEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrato JSON compatible con {@code ProcesadorEventosNotificacion} del repo
 * Kamilo14/MS-Notificacion (cola {@code notifications.queue}, routing keys del topic exchange).
 */
class EventosNotificacionContractTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();
    }

    @Test
    void donationCreated_incluyeCamposRequeridosPorMsNotificacion() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID usuarioDonanteId = UUID.randomUUID();
        OffsetDateTime donadoEn = OffsetDateTime.parse("2026-06-21T10:00:00Z");

        var event = new DonationCreatedEvent(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                usuarioDonanteId,
                "QR-123",
                List.of(new DonationCreatedEvent.DonationItemPayload(UUID.randomUUID(), 2L)),
                donadoEn
        );

        JsonNode nodo = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertThat(nodo.get("eventId").asText()).isEqualTo(eventId.toString());
        assertThat(nodo.get("usuarioDonanteId").asText()).isEqualTo(usuarioDonanteId.toString());
        assertThat(nodo.get("codigoQr").asText()).isEqualTo("QR-123");
        assertThat(nodo.get("donadoEn").asText()).isEqualTo(donadoEn.toString());
    }

    @Test
    void donationConfirmed_incluyeUsuarioDonanteIdParaNotificacionInApp() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID usuarioDonanteId = UUID.randomUUID();
        UUID confirmadoPorUsuarioId = UUID.randomUUID();

        var event = new DonationConfirmedEvent(
                eventId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                usuarioDonanteId,
                confirmadoPorUsuarioId,
                "QR-456",
                List.of(new DonationCreatedEvent.DonationItemPayload(UUID.randomUUID(), 1L)),
                OffsetDateTime.parse("2026-06-21T11:00:00Z")
        );

        JsonNode nodo = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertThat(nodo.get("eventId").asText()).isEqualTo(eventId.toString());
        assertThat(nodo.get("usuarioDonanteId").asText()).isEqualTo(usuarioDonanteId.toString());
        assertThat(nodo.get("confirmadoPorUsuarioId").asText()).isEqualTo(confirmadoPorUsuarioId.toString());
    }

    @Test
    void needCreated_manual_incluyeUsuarioId() throws Exception {
        UUID usuarioId = UUID.randomUUID();

        var event = new NeedCreatedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                5L,
                PrioridadNecesidad.ALTO,
                OrigenNecesidad.MANUAL,
                usuarioId,
                OffsetDateTime.parse("2026-06-21T12:00:00Z")
        );

        JsonNode nodo = objectMapper.readTree(objectMapper.writeValueAsString(event));

        assertThat(nodo.get("usuarioId").asText()).isEqualTo(usuarioId.toString());
    }
}

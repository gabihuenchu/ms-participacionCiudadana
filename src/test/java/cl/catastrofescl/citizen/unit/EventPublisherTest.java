package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    @Test
    void publicar_enviaEventoAlExchangeCorrecto() {
        var payload = Map.of("donacionId", "abc-123");

        eventPublisher.publicar("donation.created", payload);

        verify(rabbitTemplate).convertAndSend(
                eq(EventPublisher.EXCHANGE),
                eq("donation.created"),
                eq(payload)
        );
    }

    @Test
    void publicar_needCreated_usaRoutingKeyCorrecto() {
        var payload = Map.of("necesidadId", "nec-456");

        eventPublisher.publicar("need.created", payload);

        verify(rabbitTemplate).convertAndSend(
                eq("catastrofescl.events"),
                eq("need.created"),
                eq(payload)
        );
    }
}

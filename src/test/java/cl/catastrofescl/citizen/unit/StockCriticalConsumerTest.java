package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.event.StockCriticalConsumer;
import cl.catastrofescl.citizen.event.StockCriticalEvent;
import cl.catastrofescl.citizen.exception.DuplicateNeedException;
import cl.catastrofescl.citizen.service.NecesidadService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockCriticalConsumerTest {

    @Mock
    private NecesidadService necesidadService;

    @Mock
    private Channel channel;

    @InjectMocks
    private StockCriticalConsumer consumer;

    @Test
    void consumirStockCritical_procesaEventoYConfirmaAck() throws Exception {
        var event = new StockCriticalEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, 0, "ALTO", 2
        );

        consumer.consumirStockCritical(event, channel, 42L);

        verify(necesidadService).crearAutomatica(event);
        verify(channel).basicAck(42L, false);
    }

    @Test
    void consumirStockCritical_duplicada_confirmaAckSinReencolar() throws Exception {
        var event = new StockCriticalEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, 0, "ALTO", 2
        );
        doThrow(new DuplicateNeedException(event.centroId(), event.itemId()))
                .when(necesidadService).crearAutomatica(event);

        consumer.consumirStockCritical(event, channel, 7L);

        verify(channel).basicAck(7L, false);
    }

    @Test
    void consumirStockCritical_errorInesperado_enviaNack() throws Exception {
        var event = new StockCriticalEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, 0, "ALTO", 2
        );
        doThrow(new RuntimeException("fallo BD")).when(necesidadService).crearAutomatica(event);

        consumer.consumirStockCritical(event, channel, 9L);

        verify(channel).basicNack(9L, false, false);
    }
}

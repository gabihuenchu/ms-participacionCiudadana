package cl.catastrofescl.citizen.event;

import cl.catastrofescl.citizen.config.RabbitMQConfig;
import cl.catastrofescl.citizen.exception.DuplicateNeedException;
import cl.catastrofescl.citizen.service.IdempotenciaEventos;
import cl.catastrofescl.citizen.service.NecesidadService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockCriticalConsumer {

    private static final String TIPO_EVENTO = "stock.critical";

    private final NecesidadService necesidadService;
    private final IdempotenciaEventos idempotenciaEventos;

    @RabbitListener(queues = RabbitMQConfig.STOCK_CRITICAL_QUEUE)
    public void consumirStockCritical(
            @Payload StockCriticalEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        String eventId = event.eventId() != null ? event.eventId().toString() : null;
        try {
            if (idempotenciaEventos.yaProcesado(TIPO_EVENTO, eventId)) {
                log.debug("Evento stock.critical {} ya procesado (idempotencia), se confirma sin reprocesar", eventId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            necesidadService.crearAutomatica(event);
            idempotenciaEventos.marcarProcesado(TIPO_EVENTO, eventId);
            channel.basicAck(deliveryTag, false);
            log.info("Necesidad automática creada desde stock.critical para centro {} ítem {}",
                    event.centroId(), event.itemId());
        } catch (DuplicateNeedException ex) {
            log.info("Necesidad ya existente para centro {} ítem {}: {}",
                    event.centroId(), event.itemId(), ex.getMessage());
            idempotenciaEventos.marcarProcesado(TIPO_EVENTO, eventId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Error procesando stock.critical (a DLQ): {}", ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}

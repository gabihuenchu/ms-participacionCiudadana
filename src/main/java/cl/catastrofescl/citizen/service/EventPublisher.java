package cl.catastrofescl.citizen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    public static final String EXCHANGE = "catastrofescl.events";

    private final RabbitTemplate rabbitTemplate;

    public void publicar(String routingKey, Object event) {
        log.info("Publicando evento {} en exchange {}", routingKey, EXCHANGE);
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
    }
}

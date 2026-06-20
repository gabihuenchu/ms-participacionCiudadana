package cl.catastrofescl.citizen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Idempotencia de consumidores de eventos basada en Redis ({@code processed:{tipo}:{eventId}}, TTL 24h).
 * Tolera que Redis no este disponible (degradacion: deja pasar el evento) para no bloquear el consumo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotenciaEventos {

    private static final Duration TTL = Duration.ofHours(24);

    private final ObjectProvider<StringRedisTemplate> redisPlantilla;

    public boolean yaProcesado(String tipoEvento, String eventId) {
        if (eventId == null) {
            return false;
        }
        StringRedisTemplate redis = redisPlantilla.getIfAvailable();
        if (redis == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redis.hasKey(clave(tipoEvento, eventId)));
        } catch (Exception ex) {
            log.warn("Redis no disponible para verificar idempotencia ({}): {}", tipoEvento, ex.getMessage());
            return false;
        }
    }

    public void marcarProcesado(String tipoEvento, String eventId) {
        if (eventId == null) {
            return;
        }
        StringRedisTemplate redis = redisPlantilla.getIfAvailable();
        if (redis == null) {
            return;
        }
        try {
            redis.opsForValue().set(clave(tipoEvento, eventId), "1", TTL);
        } catch (Exception ex) {
            log.warn("Redis no disponible para marcar idempotencia ({}): {}", tipoEvento, ex.getMessage());
        }
    }

    private String clave(String tipoEvento, String eventId) {
        return "processed:" + tipoEvento + ":" + eventId;
    }
}

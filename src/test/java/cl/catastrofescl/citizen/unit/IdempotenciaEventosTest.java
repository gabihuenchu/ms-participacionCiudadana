package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.service.IdempotenciaEventos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotenciaEventosTest {

    @Mock
    private ObjectProvider<StringRedisTemplate> redisPlantilla;

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private IdempotenciaEventos idempotencia;

    @BeforeEach
    void setUp() {
        idempotencia = new IdempotenciaEventos(redisPlantilla);
    }

    @Test
    void yaProcesado_eventIdNull_retornaFalse() {
        assertThat(idempotencia.yaProcesado("stock.critical", null)).isFalse();
    }

    @Test
    void yaProcesado_redisNoDisponible_retornaFalse() {
        when(redisPlantilla.getIfAvailable()).thenReturn(null);

        assertThat(idempotencia.yaProcesado("stock.critical", "evt-1")).isFalse();
    }

    @Test
    void yaProcesado_claveExiste_retornaTrue() {
        when(redisPlantilla.getIfAvailable()).thenReturn(redis);
        when(redis.hasKey("processed:stock.critical:evt-1")).thenReturn(true);

        assertThat(idempotencia.yaProcesado("stock.critical", "evt-1")).isTrue();
    }

    @Test
    void yaProcesado_claveNoExiste_retornaFalse() {
        when(redisPlantilla.getIfAvailable()).thenReturn(redis);
        when(redis.hasKey("processed:stock.critical:evt-2")).thenReturn(false);

        assertThat(idempotencia.yaProcesado("stock.critical", "evt-2")).isFalse();
    }

    @Test
    void yaProcesado_redisLanzaExcepcion_degradaRetornandoFalse() {
        when(redisPlantilla.getIfAvailable()).thenReturn(redis);
        when(redis.hasKey(any())).thenThrow(new RuntimeException("Redis caído"));

        assertThat(idempotencia.yaProcesado("stock.critical", "evt-3")).isFalse();
    }

    @Test
    void marcarProcesado_eventIdNull_noLlamaRedis() {
        idempotencia.marcarProcesado("stock.critical", null);

        verify(redisPlantilla, never()).getIfAvailable();
    }

    @Test
    void marcarProcesado_redisDisponible_guardaClaveConTtl() {
        when(redisPlantilla.getIfAvailable()).thenReturn(redis);
        when(redis.opsForValue()).thenReturn(valueOps);

        idempotencia.marcarProcesado("stock.critical", "evt-4");

        verify(valueOps).set(
                eq("processed:stock.critical:evt-4"),
                eq("1"),
                eq(Duration.ofHours(24))
        );
    }

    @Test
    void marcarProcesado_redisLanzaExcepcion_noPropaga() {
        when(redisPlantilla.getIfAvailable()).thenReturn(redis);
        when(redis.opsForValue()).thenThrow(new RuntimeException("Redis caído"));

        idempotencia.marcarProcesado("stock.critical", "evt-5");
    }
}

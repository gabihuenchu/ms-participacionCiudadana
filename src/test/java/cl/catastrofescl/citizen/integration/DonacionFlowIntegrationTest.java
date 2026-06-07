package cl.catastrofescl.citizen.integration;

import cl.catastrofescl.citizen.dto.request.CreateDonationRequest;
import cl.catastrofescl.citizen.dto.request.DonationItemRequest;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.service.DonacionService;
import cl.catastrofescl.citizen.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DonacionFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("catastrofescl_citizen")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
    }

    @MockBean
    private EventPublisher eventPublisher;

    @Autowired
    private DonacionService donacionService;

    @Test
    void registrarYConfirmarDonacion_generaCodigoQrYConfirma() {
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID donanteId = UUID.randomUUID();
        UUID operadorId = UUID.randomUUID();

        var request = new CreateDonationRequest(
                centroId,
                List.of(new DonationItemRequest(itemId, 5))
        );

        var creada = donacionService.registrar(request, donanteId);
        assertThat(creada.codigoQr()).isNotBlank();
        assertThat(creada.estado()).isEqualTo(EstadoDonacion.PENDIENTE);
        assertThat(creada.items()).hasSize(1);

        var confirmada = donacionService.confirmar(creada.codigoQr(), operadorId);
        assertThat(confirmada.estado()).isEqualTo(EstadoDonacion.CONFIRMADA);
        assertThat(confirmada.confirmadoPorUsuarioId()).isEqualTo(operadorId);
    }
}

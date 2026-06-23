package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.exception.DonationNotFoundException;
import cl.catastrofescl.citizen.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/donaciones/qr-inexistente/confirmar");
    }

    @Test
    void handleDonationNotFound_devuelveRfc7807() {
        ProblemDetail problem = handler.handleDonationNotFound(
                new DonationNotFoundException("qr-inexistente"),
                request
        );

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Donación no encontrada");
        assertThat(problem.getType().toString()).isEqualTo("https://catastrofescl.cl/errors/donation-not-found");
        assertThat(problem.getProperties().get("errorCode")).isEqualTo("DONATION_NOT_FOUND");
        assertThat(problem.getProperties().get("instance")).isEqualTo("/donaciones/qr-inexistente/confirmar");
        assertThat(problem.getProperties().get("timestamp")).isNotNull();
    }
}

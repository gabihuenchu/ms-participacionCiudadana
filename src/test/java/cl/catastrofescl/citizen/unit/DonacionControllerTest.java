package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.config.SecurityConfig;
import cl.catastrofescl.citizen.controller.DonacionController;
import cl.catastrofescl.citizen.dto.response.DonationResponse;
import cl.catastrofescl.citizen.entity.EstadoDonacion;
import cl.catastrofescl.citizen.exception.GlobalExceptionHandler;
import cl.catastrofescl.citizen.service.DonacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DonacionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class DonacionControllerTest {

    private static final UUID USUARIO_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonacionService donacionService;

    @Test
    void registrar_sinAuth_retorna403() throws Exception {
        mockMvc.perform(post("/donaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyDonacion()))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrar_conPermiso_retorna201() throws Exception {
        UUID donacionId = UUID.randomUUID();
        UUID centroId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

        when(donacionService.registrar(any(), eq(USUARIO_ID))).thenReturn(new DonationResponse(
                donacionId, centroId, USUARIO_ID, "qr-test", EstadoDonacion.PENDIENTE,
                OffsetDateTime.now(), null, null, 2L, List.of()
        ));

        mockMvc.perform(post("/donaciones")
                        .header("X-Dev-User-Id", USUARIO_ID.toString())
                        .header("X-Dev-Permissions", "DONACION_REALIZAR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyDonacion()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/donaciones/" + donacionId))
                .andExpect(jsonPath("$.codigoQr").value("qr-test"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void registrar_sinPermisoCorrecto_retorna403() throws Exception {
        mockMvc.perform(post("/donaciones")
                        .header("X-Dev-User-Id", USUARIO_ID.toString())
                        .header("X-Dev-Permissions", "OTRO_PERMISO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyDonacion()))
                .andExpect(status().isForbidden());
    }

    private static String bodyDonacion() {
        return """
                {
                  "centroId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                  "items": [{"itemId": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "cantidad": 2}]
                }
                """;
    }
}

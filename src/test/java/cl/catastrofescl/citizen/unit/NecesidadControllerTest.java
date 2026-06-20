package cl.catastrofescl.citizen.unit;

import cl.catastrofescl.citizen.config.SecurityConfig;
import cl.catastrofescl.citizen.controller.NecesidadController;
import cl.catastrofescl.citizen.dto.response.NeedResponse;
import cl.catastrofescl.citizen.dto.response.PageResponse;
import cl.catastrofescl.citizen.entity.EstadoNecesidad;
import cl.catastrofescl.citizen.entity.OrigenNecesidad;
import cl.catastrofescl.citizen.entity.PrioridadNecesidad;
import cl.catastrofescl.citizen.service.DonacionCapacidadService;
import cl.catastrofescl.citizen.service.NecesidadService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NecesidadController.class)
@Import(SecurityConfig.class)
class NecesidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NecesidadService necesidadService;

    @MockBean
    private DonacionCapacidadService donacionCapacidadService;

    @Test
    void listarPublicas_sinAuth_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID centroId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(necesidadService.listarPublicas(0, 20)).thenReturn(new PageResponse<>(
                List.of(new NeedResponse(
                        id, centroId, itemId, null, 10L, 0L, 10L,
                        PrioridadNecesidad.ALTO, OrigenNecesidad.MANUAL,
                        EstadoNecesidad.ACTIVA, OffsetDateTime.now(), null
                )),
                0, 20, 1, 1
        ));

        mockMvc.perform(get("/necesidades/publicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].prioridad").value("ALTO"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listarPorCentro_sinAuth_retorna200() throws Exception {
        UUID centroId = UUID.randomUUID();
        when(necesidadService.listarPorCentro(eq(centroId))).thenReturn(List.of());

        mockMvc.perform(get("/necesidades/centro/{centroId}", centroId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

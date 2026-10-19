package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.exception.MicroserviceErrorHandler;
import com.andesstay.ms_andesstay_bff.security.JwtAuthConverter;
import com.andesstay.ms_andesstay_bff.security.SecurityConfig;
import com.andesstay.ms_andesstay_bff.service.AuditService;
import com.andesstay.ms_andesstay_bff.service.CatalogService;
import com.andesstay.ms_andesstay_bff.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorizacion por rol de las rutas de catalog, audit y report (misma matriz que cada microservicio).
 */
@WebMvcTest({CatalogBffController.class, AuditBffController.class, ReportBffController.class})
@Import({SecurityConfig.class, JwtAuthConverter.class, MicroserviceErrorHandler.class})
class CatalogAuditReportBffSecurityTest {

    private static final String UNIT_BODY = "{\"nombre\":\"Cabana Lago\",\"tipo\":\"CABANA\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogService catalogService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        Map<String, Object> unidad = Map.of("id", 1);
        when(catalogService.listarUnidades(any(), any(), any())).thenReturn(List.of(unidad));
        when(catalogService.obtenerUnidad(anyLong())).thenReturn(unidad);
        when(catalogService.crearUnidad(any())).thenReturn(unidad);
        when(catalogService.actualizarUnidad(anyLong(), any())).thenReturn(unidad);
        when(catalogService.moverCupo(anyLong(), anyString())).thenReturn(unidad);
        when(auditService.buscarEventos(any())).thenReturn(Map.of("content", List.of()));
        when(auditService.timeline(anyLong())).thenReturn(List.of());
        when(reportService.kpis(any())).thenReturn(Map.of("range", "last7d"));
        when(reportService.topUnits(any(), any())).thenReturn(Map.of("unidades", List.of()));
    }

    // ---------------------------------------------------------------- catalog

    @ParameterizedTest(name = "GET /api/catalog/units como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,200", "Auditor,200", "Sin_Rol,403"})
    void listarUnidades(String rol, int esperado) throws Exception {
        assertStatus(get("/api/catalog/units"), rol, esperado);
    }

    @ParameterizedTest(name = "GET /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,200", "Auditor,200", "Sin_Rol,403"})
    void obtenerUnidad(String rol, int esperado) throws Exception {
        assertStatus(get("/api/catalog/units/1"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units como {0} -> {1}")
    @CsvSource({"Admin,201", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void crearUnidad(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units").contentType(MediaType.APPLICATION_JSON).content(UNIT_BODY), rol, esperado);
    }

    @ParameterizedTest(name = "PUT /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void actualizarUnidad(String rol, int esperado) throws Exception {
        assertStatus(put("/api/catalog/units/1").contentType(MediaType.APPLICATION_JSON).content(UNIT_BODY), rol, esperado);
    }

    @ParameterizedTest(name = "DELETE /api/catalog/units/1 como {0} -> {1}")
    @CsvSource({"Admin,204", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void desactivarUnidad(String rol, int esperado) throws Exception {
        assertStatus(delete("/api/catalog/units/1"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units/1/reserve como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void reservarCupo(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units/1/reserve"), rol, esperado);
    }

    @ParameterizedTest(name = "POST /api/catalog/units/1/release como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,200", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void liberarCupo(String rol, int esperado) throws Exception {
        assertStatus(post("/api/catalog/units/1/release"), rol, esperado);
    }

    @Test
    void sinCuposDelCatalogSePropagaComo409() throws Exception {
        when(catalogService.moverCupo(1L, "reserve")).thenThrow(HttpClientErrorException.create(HttpStatus.CONFLICT,
                "Conflict", HttpHeaders.EMPTY, "{\"status\":409,\"message\":\"sin cupos\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/catalog/units/1/reserve").with(usuario("Operador_Dominio")))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"status\":409,\"message\":\"sin cupos\"}"));
    }

    @Test
    void filtrosDelCatalogLleganAlService() throws Exception {
        mockMvc.perform(get("/api/catalog/units").param("tipo", "DOMO").param("soloDisponibles", "true")
                        .with(usuario("Cliente_Dominio")))
                .andExpect(status().isOk());

        verify(catalogService).listarUnidades("DOMO", null, true);
    }

    // ---------------------------------------------------------------- audit

    @ParameterizedTest(name = "GET /api/audit/events como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,200", "Sin_Rol,403"})
    void buscarEventos(String rol, int esperado) throws Exception {
        assertStatus(get("/api/audit/events"), rol, esperado);
    }

    @ParameterizedTest(name = "GET /api/audit/reservations/1/timeline como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,200", "Sin_Rol,403"})
    void timeline(String rol, int esperado) throws Exception {
        assertStatus(get("/api/audit/reservations/1/timeline"), rol, esperado);
    }

    @Test
    void postDeEventosNoSeExponeEnElBff() throws Exception {
        mockMvc.perform(post("/api/audit/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"RESERVA_CREADA\",\"reservationId\":1,\"actor\":\"x\"}")
                        .with(usuario("Admin")))
                .andExpect(status().isMethodNotAllowed());
    }

    // ---------------------------------------------------------------- report

    @ParameterizedTest(name = "GET /api/report/kpis como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void kpis(String rol, int esperado) throws Exception {
        assertStatus(get("/api/report/kpis").param("range", "last24h"), rol, esperado);
    }

    @ParameterizedTest(name = "GET /api/report/top-units como {0} -> {1}")
    @CsvSource({"Admin,200", "Operador_Dominio,403", "Cliente_Dominio,403", "Auditor,403", "Sin_Rol,403"})
    void topUnits(String rol, int esperado) throws Exception {
        assertStatus(get("/api/report/top-units"), rol, esperado);
    }

    @Test
    void reporteNoDisponibleSePropagaComo503() throws Exception {
        when(reportService.kpis(any())).thenThrow(HttpServerErrorException.create(HttpStatus.SERVICE_UNAVAILABLE,
                "Service Unavailable", HttpHeaders.EMPTY,
                "{\"status\":503,\"message\":\"ms-andesstay-reservations no responde\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/report/kpis").with(usuario("Admin")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("ms-andesstay-reservations no responde"));
    }

    @Test
    void microservicioCaidoResponde503() throws Exception {
        when(auditService.timeline(1L)).thenThrow(new ResourceAccessException("Connection refused"));

        mockMvc.perform(get("/api/audit/reservations/1/timeline").with(usuario("Auditor")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/catalog/units")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/report/kpis")).andExpect(status().isUnauthorized());
    }

    private void assertStatus(MockHttpServletRequestBuilder request, String rol, int esperado) throws Exception {
        mockMvc.perform(request.with(usuario(rol))).andExpect(status().is(esperado));
    }

    private static RequestPostProcessor usuario(String rol) {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}

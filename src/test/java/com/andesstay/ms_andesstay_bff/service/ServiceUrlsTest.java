package com.andesstay.ms_andesstay_bff.service;

import com.andesstay.ms_andesstay_bff.config.ServicesProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Cada service usa la URL de andesstay.services.*.url (ej. nombres de contenedor en Docker)
 * y reenvia solo los query params informados.
 */
class ServiceUrlsTest {

    private static final ServicesProperties URLS = new ServicesProperties(
            new ServicesProperties.Service("http://ms-andesstay-reservations:8081"),
            new ServicesProperties.Service("http://ms-andesstay-catalog:8085"),
            new ServicesProperties.Service("http://ms-andesstay-audit:8086"),
            new ServicesProperties.Service("http://ms-andesstay-report:8087"));

    private MockRestServiceServer server;
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("admin@andesstay.cl").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        restClient = builder.build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reservationsUsaLaUrlConfigurada() {
        server.expect(requestTo("http://ms-andesstay-reservations:8081/api/reservations?status=CONFIRMADA"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        new ReservationService(restClient, URLS).obtenerReservasPorEstado("CONFIRMADA");
        server.verify();
    }

    @Test
    void catalogReenviaSoloLosFiltrosInformados() {
        server.expect(requestTo("http://ms-andesstay-catalog:8085/api/catalog/units?tipo=DOMO&soloDisponibles=true"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        new CatalogService(restClient, URLS).listarUnidades("DOMO", " ", true);
        server.verify();
    }

    @Test
    void auditReenviaFiltrosYPaginacion() {
        server.expect(requestTo("http://ms-andesstay-audit:8086/api/audit/events?actor=op@andesstay.cl&page=1"))
                .andRespond(withSuccess("{\"content\":[]}", MediaType.APPLICATION_JSON));

        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("actor", "op@andesstay.cl");
        filtros.put("tipo", null);
        filtros.put("page", "1");
        new AuditService(restClient, URLS).buscarEventos(filtros);
        server.verify();
    }

    @Test
    void reportUsaLaUrlConfigurada() {
        server.expect(requestTo("http://ms-andesstay-report:8087/api/report/top-units?range=last30d&limit=3"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        new ReportService(restClient, URLS).topUnits("last30d", 3);
        server.verify();
    }
}

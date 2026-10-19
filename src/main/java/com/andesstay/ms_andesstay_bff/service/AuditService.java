package com.andesstay.ms_andesstay_bff.service;

import com.andesstay.ms_andesstay_bff.config.ServicesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * Consultas a ms-andesstay-audit (:8086). El POST /api/audit/events es interno (lo llama
 * reservations) y NO se expone en el BFF.
 */
@Service
public class AuditService {

    private final RestClient restClient;
    private final String baseUrl;

    public AuditService(@Qualifier("internalRestClient") RestClient restClient, ServicesProperties services) {
        this.restClient = restClient;
        this.baseUrl = services.audit().url();
    }

    /**
     * GET /api/audit/events con filtros y paginacion (solo los parametros informados)
     */
    public Map<String, Object> buscarEventos(Map<String, String> filtros) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/audit/events");
        filtros.forEach((nombre, valor) -> QueryParams.agregar(uri, nombre, valor));
        return restClient.get()
                .uri(uri.encode().build().toUri())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * GET /api/audit/reservations/{id}/timeline
     */
    public List<Map<String, Object>> timeline(Long reservationId) {
        return restClient.get()
                .uri(baseUrl + "/api/audit/reservations/{id}/timeline", reservationId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }
}

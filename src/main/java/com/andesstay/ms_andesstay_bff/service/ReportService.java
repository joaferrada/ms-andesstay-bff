package com.andesstay.ms_andesstay_bff.service;

import com.andesstay.ms_andesstay_bff.config.ServicesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Consultas a ms-andesstay-report (:8087).
 */
@Service
public class ReportService {

    private final RestClient restClient;
    private final String baseUrl;

    public ReportService(@Qualifier("internalRestClient") RestClient restClient, ServicesProperties services) {
        this.restClient = restClient;
        this.baseUrl = services.report().url();
    }

    /**
     * GET /api/report/kpis?range=
     */
    public Map<String, Object> kpis(String range) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/report/kpis");
        QueryParams.agregar(uri, "range", range);
        return get(uri);
    }

    /**
     * GET /api/report/top-units?range=&limit=
     */
    public Map<String, Object> topUnits(String range, Integer limit) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/report/top-units");
        QueryParams.agregar(uri, "range", range);
        QueryParams.agregar(uri, "limit", limit);
        return get(uri);
    }

    private Map<String, Object> get(UriComponentsBuilder uri) {
        return restClient.get()
                .uri(uri.encode().build().toUri())
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}

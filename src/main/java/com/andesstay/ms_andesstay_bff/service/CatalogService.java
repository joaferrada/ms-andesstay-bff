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
 * Llamadas a ms-andesstay-catalog (:8085). El JWT lo agrega el interceptor de internalRestClient.
 * Los errores del microservicio se propagan como excepciones (ver MicroserviceErrorHandler).
 */
@Service
public class CatalogService {

    private final RestClient restClient;
    private final String baseUrl;

    public CatalogService(@Qualifier("internalRestClient") RestClient restClient, ServicesProperties services) {
        this.restClient = restClient;
        this.baseUrl = services.catalog().url();
    }

    /**
     * GET /api/catalog/units?tipo=&hostal=&soloDisponibles= (solo los filtros informados)
     */
    public List<Map<String, Object>> listarUnidades(String tipo, String hostal, Boolean soloDisponibles) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl).path("/api/catalog/units");
        QueryParams.agregar(uri, "tipo", tipo);
        QueryParams.agregar(uri, "hostal", hostal);
        QueryParams.agregar(uri, "soloDisponibles", soloDisponibles);
        return restClient.get()
                .uri(uri.encode().build().toUri())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * GET /api/catalog/units/{id}
     */
    public Map<String, Object> obtenerUnidad(Long id) {
        return restClient.get()
                .uri(baseUrl + "/api/catalog/units/{id}", id)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * POST /api/catalog/units
     */
    public Map<String, Object> crearUnidad(Map<String, Object> unidadDto) {
        return restClient.post()
                .uri(baseUrl + "/api/catalog/units")
                .body(unidadDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * PUT /api/catalog/units/{id}
     */
    public Map<String, Object> actualizarUnidad(Long id, Map<String, Object> unidadDto) {
        return restClient.put()
                .uri(baseUrl + "/api/catalog/units/{id}", id)
                .body(unidadDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * DELETE /api/catalog/units/{id} (baja logica)
     */
    public void desactivarUnidad(Long id) {
        restClient.delete()
                .uri(baseUrl + "/api/catalog/units/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * POST /api/catalog/units/{id}/reserve o /release (accion = "reserve" | "release")
     */
    public Map<String, Object> moverCupo(Long id, String accion) {
        return restClient.post()
                .uri(baseUrl + "/api/catalog/units/{id}/{accion}", id, accion)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}

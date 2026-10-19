package com.andesstay.ms_andesstay_bff.service;

import com.andesstay.ms_andesstay_bff.config.ServicesProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final RestClient restClient;
    private final String baseUrl;

    public ReservationService(
            @Qualifier("internalRestClient") RestClient restClient,
            ServicesProperties services) {
        this.restClient = restClient;
        this.baseUrl = services.reservations().url();
    }

    /**
     * Obtiene el JWT del usuario autenticado en el BFF.
     */
    private String obtenerToken() {
        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication.getToken().getTokenValue();
    }

    /**
     * GET /api/reservations
     */
    public List<Map<String, Object>> obtenerReservasDesdeMicroservicio() {

        return restClient.get()
                .uri(baseUrl + "/api/reservations")
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * GET /api/reservations?status=...
     */
    public List<Map<String, Object>> obtenerReservasPorEstado(String status) {

        return restClient.get()
                .uri(baseUrl + "/api/reservations?status={status}", status)
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * PUT /api/reservations/{id}/status
     * Los errores 4xx del microservicio (404, 409, 400) se propagan como HttpClientErrorException.
     */
    public Map<String, Object> cambiarEstadoEnMicroservicio(
            Long id,
            Map<String, Object> statusDto) {

        return restClient.put()
                .uri(baseUrl + "/api/reservations/" + id + "/status")
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .body(statusDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * GET /api/reservations/{id}
     */
    public Map<String, Object> obtenerReservaPorId(Long id) {

        return restClient.get()
                .uri(baseUrl + "/api/reservations/" + id)
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * POST /api/reservations
     */
    public Map<String, Object> crearReservaEnMicroservicio(
            Map<String, Object> reservaDto) {

        return restClient.post()
                .uri(baseUrl + "/api/reservations")
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .body(reservaDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * PUT /api/reservations/{id}
     */
    public Map<String, Object> actualizarReservaEnMicroservicio(
            Long id,
            Map<String, Object> reservaDto) {

        return restClient.put()
                .uri(baseUrl + "/api/reservations/" + id)
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .body(reservaDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * DELETE /api/reservations/{id}
     */
    public void eliminarReservaEnMicroservicio(Long id) {

        restClient.delete()
                .uri(baseUrl + "/api/reservations/" + id)
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .toBodilessEntity();
    }
}
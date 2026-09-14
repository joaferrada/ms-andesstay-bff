package com.andesstay.ms_andesstay_bff.service;

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

    public ReservationService(
            @Qualifier("internalRestClient") RestClient restClient) {
        this.restClient = restClient;
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
                .uri("http://localhost:8081/api/reservations")
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    /**
     * GET /api/reservations/{id}
     */
    public Map<String, Object> obtenerReservaPorId(Long id) {

        return restClient.get()
                .uri("http://localhost:8081/api/reservations/" + id)
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

        System.out.println("🔥 ENVIANDO POST AL MICROSERVICIO 8081");
        System.out.println("🔥 TOKEN OBTENIDO: " + (obtenerToken() != null));

        return restClient.post()
                .uri("http://localhost:8081/api/reservations")
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
                .uri("http://localhost:8081/api/reservations/" + id)
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
                .uri("http://localhost:8081/api/reservations/" + id)
                .headers(headers ->
                        headers.setBearerAuth(obtenerToken()))
                .retrieve()
                .toBodilessEntity();
    }
}
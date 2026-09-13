package com.andesstay.ms_andesstay_bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ReservationService {

    private final RestClient restClient;

    public ReservationService(@Qualifier("internalRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<Map<String, Object>> obtenerReservasDesdeMicroservicio() {
        return restClient.get()
                .uri("http://localhost:8081/api/reservations")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
    }

    public Map<String, Object> obtenerReservaPorId(Long id) {
        return restClient.get()
                .uri("http://localhost:8081/api/reservations/" + id)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> crearReservaEnMicroservicio(Map<String, Object> reservaDto) {
        return restClient.post()
                .uri("http://localhost:8081/api/reservations")
                .body(reservaDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> actualizarReservaEnMicroservicio(Long id, Map<String, Object> reservaDto) {
        return restClient.put()
                .uri("http://localhost:8081/api/reservations/" + id)
                .body(reservaDto)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public void eliminarReservaEnMicroservicio(Long id) {
        restClient.delete()
                .uri("http://localhost:8081/api/reservations/" + id)
                .retrieve()
                .toBodilessEntity();
    }
}
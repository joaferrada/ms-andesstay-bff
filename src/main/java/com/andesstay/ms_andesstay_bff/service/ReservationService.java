package com.andesstay.ms_andesstay_bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Service
public class ReservationService {

    private final RestClient restClient;

    // Inyectamos el RestClient configurado con el interceptor de token
    public ReservationService(@Qualifier("internalRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public Map<String, Object> obtenerReservasDesdeMicroservicio() {
        // Apuntamos al microservicio ms-andesstay-reservations corriendo en el puerto 8081
        return restClient.get()
                .uri("http://localhost:8081/api/reservations")
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
}
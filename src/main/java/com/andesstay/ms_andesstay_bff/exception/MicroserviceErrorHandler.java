package com.andesstay.ms_andesstay_bff.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Propaga los errores del microservicio (400, 403, 404, 409, 503...) con su mismo codigo y cuerpo
 * JSON, en vez de convertirlos en 500. Si el microservicio no responde: 503.
 */
@RestControllerAdvice
public class MicroserviceErrorHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> errorDelMicroservicio(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    /** Ej. 503 de report cuando reservations no responde. */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> errorInternoDelMicroservicio(HttpServerErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    /** Conexion rechazada o timeout. */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> microservicioNoDisponible(ResourceAccessException ex) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", "El microservicio no responde. Intente mas tarde");
        return ResponseEntity.status(status).body(body);
    }
}

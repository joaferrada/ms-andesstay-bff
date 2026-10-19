package com.andesstay.ms_andesstay_bff.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Propaga los 4xx del microservicio (400, 403, 404, 409...) con su mismo codigo y cuerpo JSON,
 * en vez de convertirlos en 500.
 */
@RestControllerAdvice
public class MicroserviceErrorHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> errorDelMicroservicio(HttpClientErrorException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }
}

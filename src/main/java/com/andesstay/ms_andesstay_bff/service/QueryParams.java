package com.andesstay.ms_andesstay_bff.service;

import org.springframework.web.util.UriComponentsBuilder;

/**
 * Reenvia al microservicio solo los query params que vienen informados.
 */
final class QueryParams {

    private QueryParams() {
    }

    static void agregar(UriComponentsBuilder uri, String nombre, Object valor) {
        if (valor != null && !valor.toString().isBlank()) {
            uri.queryParam(nombre, valor);
        }
    }
}

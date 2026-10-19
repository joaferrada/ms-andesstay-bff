package com.andesstay.ms_andesstay_bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * URLs internas de los microservicios ({@code andesstay.services.*} en application.yaml,
 * sobreescribibles con RESERVATIONS_URL, CATALOG_URL, AUDIT_URL y REPORT_URL).
 */
@ConfigurationProperties(prefix = "andesstay.services")
public record ServicesProperties(
        Service reservations,
        Service catalog,
        Service audit,
        Service report) {

    public record Service(String url) {
    }
}

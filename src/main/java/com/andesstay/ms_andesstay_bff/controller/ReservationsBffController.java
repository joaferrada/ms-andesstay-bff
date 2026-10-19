package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.ReservationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ReservationsBffController {

    private final ReservationService reservationService;

    public ReservationsBffController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<Map<String, Object>> listarReservas(@RequestParam(required = false) String status,
                                                    Authentication authentication) {

        return status == null || status.isBlank()
                ? reservationService.obtenerReservasDesdeMicroservicio()
                : reservationService.obtenerReservasPorEstado(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerReservaPorId(@PathVariable Long id) {
        // El microservicio responde 404 si no existe o si es de otro cliente
        try {
            Map<String, Object> reserva = reservationService.obtenerReservaPorId(id);
            return reserva != null ? ResponseEntity.ok(reserva) : ResponseEntity.notFound().build();
        } catch (HttpClientErrorException.NotFound ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('Cliente_Dominio') or hasRole('Admin')")
    public Map<String, Object> crearReserva(
            @RequestBody Map<String, Object> reservaDto,
            Authentication authentication) {

        return reservationService.crearReservaEnMicroservicio(reservaDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Cliente_Dominio') or hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> actualizarReserva(@PathVariable Long id, @RequestBody Map<String, Object> reservaDto, Authentication authentication) {
        try {
            Map<String, Object> actualizada = reservationService.actualizarReservaEnMicroservicio(id, reservaDto);
            return actualizada != null ? ResponseEntity.ok(actualizada) : ResponseEntity.notFound().build();
        } catch (HttpClientErrorException.NotFound ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or hasRole('Operador_Dominio')")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservationService.eliminarReservaEnMicroservicio(id);
        return ResponseEntity.noContent().build();
    }

    // EP3: cambio de estado. Propaga 400/404/409 del microservicio con su mensaje JSON.
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('Admin') or hasRole('Operador_Dominio')")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Object> statusDto) {
        try {
            return ResponseEntity.ok(reservationService.cambiarEstadoEnMicroservicio(id, statusDto));
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        }
    }
}
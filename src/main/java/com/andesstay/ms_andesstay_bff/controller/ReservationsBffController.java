package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public List<Map<String, Object>> listarReservas(Authentication authentication) {

        return reservationService.obtenerReservasDesdeMicroservicio();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerReservaPorId(@PathVariable Long id) {
        Map<String, Object> reserva = reservationService.obtenerReservaPorId(id);
        return reserva != null ? ResponseEntity.ok(reserva) : ResponseEntity.notFound().build();
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
        Map<String, Object> actualizada = reservationService.actualizarReservaEnMicroservicio(id, reservaDto);
        return actualizada != null ? ResponseEntity.ok(actualizada) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or hasRole('Operador_Dominio')")
    public ResponseEntity<Void> eliminarReserva(@PathVariable Long id) {
        reservationService.eliminarReservaEnMicroservicio(id);
        return ResponseEntity.noContent().build();
    }
}
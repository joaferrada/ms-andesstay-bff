package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.ReservationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationsBffController {

    private final ReservationService reservationService;

    public ReservationsBffController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public Map<String, Object> listarReservas(Authentication authentication) {
        // Llama al microservicio interno propagando el token
        return reservationService.obtenerReservasDesdeMicroservicio();
    }

    @PostMapping
    @PreAuthorize("hasRole('Cliente_Dominio') or hasRole('Admin')")
    public Map<String, Object> crearReserva(@RequestBody Map<String, Object> reservaDto, Authentication authentication) {
        // Llama al microservicio interno enviando el body y propagando el token
        return reservationService.crearReservaEnMicroservicio(reservaDto);
    }
}
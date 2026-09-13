package com.andesstay.ms_andesstay_bff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationsBffController {

    // Listar reservas (accesible para clientes, operadores y admins según corresponda)
    @GetMapping
    public Map<String, Object> listarReservas(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Lista de reservas en AndesStay");
        response.put("solicitadoPor", authentication.getName());
        return response;
    }

    // Crear una nueva reserva (típicamente realizado por clientes)
    @PostMapping
    @PreAuthorize("hasRole('Cliente_Dominio') or hasRole('Admin')")
    public Map<String, Object> crearReserva(@RequestBody Map<String, Object> reservaDto, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Reserva creada exitosamente");
        response.put("cliente", authentication.getName());
        response.put("datosReserva", reservaDto);
        return response;
    }

    // Cancelar o eliminar una reserva
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or hasRole('Operador_Dominio')")
    public String eliminarReserva(@PathVariable Long id) {
        return "Reserva con ID " + id + " cancelada/eliminada correctamente.";
    }
}
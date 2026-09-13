package com.andesstay.ms_andesstay_bff;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alojamientos")
public class AlojamientoBffController {

    // Endpoint abierto para clientes y público general que consultan la plataforma
    @GetMapping
    public Map<String, Object> listarAlojamientos() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Lista de alojamientos disponibles en AndesStay");
        // Aquí el BFF luego llamará al microservicio correspondiente de alojamientos
        return response;
    }

    // Endpoint exclusivo para operadores de dominio que registran propiedades
    @PostMapping
    @PreAuthorize("hasRole('Operador_Dominio') or hasRole('Admin')")
    public Map<String, Object> crearAlojamiento(@RequestBody Map<String, Object> alojamientoDto, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Alojamiento registrado exitosamente");
        response.put("registradoPor", authentication.getName());
        response.put("datos", alojamientoDto);
        return response;
    }

    // Endpoint exclusivo para administradores de la plataforma
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public String eliminarAlojamiento(@PathVariable Long id) {
        return "Alojamiento con ID " + id + " eliminado por el Administrador.";
    }
}
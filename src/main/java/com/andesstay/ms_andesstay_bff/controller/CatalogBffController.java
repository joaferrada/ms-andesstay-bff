package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * /api/catalog/** -> ms-andesstay-catalog (:8085). Mismos roles que el microservicio.
 */
@RestController
@RequestMapping("/api/catalog/units")
public class CatalogBffController {

    private final CatalogService catalogService;

    public CatalogBffController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio','Cliente_Dominio','Auditor')")
    public List<Map<String, Object>> listarUnidades(@RequestParam(required = false) String tipo,
                                                    @RequestParam(required = false) String hostal,
                                                    @RequestParam(required = false) Boolean soloDisponibles) {
        return catalogService.listarUnidades(tipo, hostal, soloDisponibles);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio','Cliente_Dominio','Auditor')")
    public Map<String, Object> obtenerUnidad(@PathVariable Long id) {
        return catalogService.obtenerUnidad(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> crearUnidad(@RequestBody Map<String, Object> unidadDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.crearUnidad(unidadDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> actualizarUnidad(@PathVariable Long id, @RequestBody Map<String, Object> unidadDto) {
        return catalogService.actualizarUnidad(id, unidadDto);
    }

    // Baja logica (activo=false)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> desactivarUnidad(@PathVariable Long id) {
        catalogService.desactivarUnidad(id);
        return ResponseEntity.noContent().build();
    }

    // Ajuste manual de cupos (reservations los usa al confirmar/cancelar). 409 del microservicio si no hay cupos
    @PostMapping("/{id}/reserve")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio')")
    public Map<String, Object> reservarCupo(@PathVariable Long id) {
        return catalogService.moverCupo(id, "reserve");
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('Admin','Operador_Dominio')")
    public Map<String, Object> liberarCupo(@PathVariable Long id) {
        return catalogService.moverCupo(id, "release");
    }
}

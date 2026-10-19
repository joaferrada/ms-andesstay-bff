package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * /api/audit/** -> ms-andesstay-audit (:8086), solo lectura. El POST /api/audit/events es interno
 * (lo llama reservations) y no se expone aqui.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditBffController {

    private final AuditService auditService;

    public AuditBffController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/events")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public Map<String, Object> buscarEventos(@RequestParam(required = false) String actor,
                                             @RequestParam(required = false) String tipo,
                                             @RequestParam(required = false) Long reservationId,
                                             @RequestParam(required = false) String desde,
                                             @RequestParam(required = false) String hasta,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer size) {
        Map<String, String> filtros = new LinkedHashMap<>();
        filtros.put("actor", actor);
        filtros.put("tipo", tipo);
        filtros.put("reservationId", reservationId != null ? reservationId.toString() : null);
        filtros.put("desde", desde);
        filtros.put("hasta", hasta);
        filtros.put("page", page != null ? page.toString() : null);
        filtros.put("size", size != null ? size.toString() : null);
        return auditService.buscarEventos(filtros);
    }

    @GetMapping("/reservations/{id}/timeline")
    @PreAuthorize("hasAnyRole('Admin','Auditor')")
    public List<Map<String, Object>> timeline(@PathVariable Long id) {
        return auditService.timeline(id);
    }
}

package com.andesstay.ms_andesstay_bff.controller;

import com.andesstay.ms_andesstay_bff.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * /api/report/** -> ms-andesstay-report (:8087). Solo Admin. range/limit invalidos (400) y
 * reservations caido (503) los responde el microservicio y se propagan tal cual.
 */
@RestController
@RequestMapping("/api/report")
public class ReportBffController {

    private final ReportService reportService;

    public ReportBffController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/kpis")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> kpis(@RequestParam(required = false) String range) {
        return reportService.kpis(range);
    }

    @GetMapping("/top-units")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> topUnits(@RequestParam(required = false) String range,
                                        @RequestParam(required = false) Integer limit) {
        return reportService.topUnits(range, limit);
    }
}

package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.model.HistorialClienteProjection;
import com.sandistay.backend.modules.finance.model.OcupacionProjection;
import com.sandistay.backend.modules.finance.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	/**
	 * Endpoint protegido para obtener el reporte de ocupación del hotel.
	 */
	@GetMapping("/occupancy")
	public ResponseEntity<List<OcupacionProjection>> getOccupancyReport(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

		List<OcupacionProjection> report = reportService.getOccupancyReport(start, end);
		return ResponseEntity.ok(report);
	}
	
	/**
     * Endpoint protegido para consultar el historial de un cliente (CRM).
     */
    @GetMapping("/clients/{id}/history")
    public ResponseEntity<HistorialClienteProjection> getClientHistory(@PathVariable Integer id) {
        HistorialClienteProjection history = reportService.getClientHistory(id);
        return ResponseEntity.ok(history);
    }
}
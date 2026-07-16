package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.model.HistorialClienteProjection;
import com.sandistay.backend.modules.finance.model.OcupacionProjection;
import com.sandistay.backend.modules.finance.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador de Reportes y CRM.
 * Sigue el principio de "Thin Controller", delegando la lógica al ReportService.
 */
@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;

    // Constructor manual explícito (Sin Lombok)
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/occupancy")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<OcupacionProjection>> getOccupancyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.getOccupancyReport(start, end));
    }

    @GetMapping(value = "/occupancy/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> downloadOccupancyPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        byte[] pdfBytes = reportService.generarReporteOcupacionPdf(start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "reporte-ocupacion.pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/clients/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<HistorialClienteProjection> getClientHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getClientHistory(id));
    }

    @GetMapping("/clients/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, Object>> searchClientByDocument(@RequestParam String documento) {
        // Delegamos la lógica de búsqueda y mapeo al servicio
        return ResponseEntity.ok(reportService.searchClientByDocument(documento));
    }

    @PatchMapping("/clients/{id}/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateClientCategory(@PathVariable Integer id, @RequestParam Integer idTipoCliente) {
        reportService.updateClientCategory(id, idTipoCliente);
        return ResponseEntity.ok(Map.of("message", "Categoría actualizada exitosamente."));
    }
}

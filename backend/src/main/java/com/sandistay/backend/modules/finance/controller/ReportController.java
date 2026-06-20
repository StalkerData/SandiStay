package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
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

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

    private final ReportService reportService;
    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;

    public ReportController(ReportService reportService, PersonaRepository personaRepository, ClienteRepository clienteRepository) {
        this.reportService = reportService;
        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/occupancy")
    public ResponseEntity<List<OcupacionProjection>> getOccupancyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reportService.getOccupancyReport(start, end));
    }

    @GetMapping(value = "/occupancy/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
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
    public ResponseEntity<HistorialClienteProjection> getClientHistory(@PathVariable Integer id) {
        return ResponseEntity.ok(reportService.getClientHistory(id));
    }

    @GetMapping("/clients/search")
    public ResponseEntity<?> searchClientByDocument(@RequestParam String documento) {
        Persona persona = personaRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna persona con ese documento."));

        Cliente cliente = clienteRepository.findByIdPersona(persona.getIdPersona())
                .orElseThrow(() -> new RuntimeException("La persona existe pero no está registrada como cliente."));

        return ResponseEntity.ok(Map.of(
                "idCliente", cliente.getIdCliente(),
                "codigoCliente", cliente.getCodigoCliente(),
                "idTipoCliente", cliente.getIdTipoCliente(), // NUEVO: Devolvemos la categoría actual
                "nombreCompleto", persona.getNombres() + " " + persona.getApellidos()
        ));
    }

    // NUEVO ENDPOINT: Para que el Admin cambie la categoría manualmente
    @PatchMapping("/clients/{id}/category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateClientCategory(@PathVariable Integer id, @RequestParam Integer idTipoCliente) {
        reportService.updateClientCategory(id, idTipoCliente);
        return ResponseEntity.ok("Categoría actualizada exitosamente.");
    }
}
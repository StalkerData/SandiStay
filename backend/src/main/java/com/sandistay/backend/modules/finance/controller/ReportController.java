package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.model.HistorialClienteProjection;
import com.sandistay.backend.modules.finance.model.OcupacionProjection;
import com.sandistay.backend.modules.finance.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import java.util.Map;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {

	private final ReportService reportService;
	private final PersonaRepository personaRepository;
	private final ClienteRepository clienteRepository;

	public ReportController(ReportService reportService, 
			PersonaRepository personaRepository,
			ClienteRepository clienteRepository) {
		this.reportService = reportService;
		this.personaRepository = personaRepository;
		this.clienteRepository = clienteRepository;
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
    
    /**
     * Endpoint para buscar el ID de un cliente usando su número de documento.
     * Vital para el módulo CRM del Frontend.
     */
    @GetMapping("/clients/search")
    public ResponseEntity<?> searchClientByDocument(@RequestParam String documento) {
        // 1. Buscamos la persona
        Persona persona = personaRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna persona con ese documento."));

        // 2. Buscamos el cliente asociado
        Cliente cliente = clienteRepository.findByIdPersona(persona.getIdPersona())
                .orElseThrow(() -> new RuntimeException("La persona existe pero no está registrada como cliente."));

        // 3. Devolvemos el ID y el nombre para el CRM
        return ResponseEntity.ok(Map.of(
                "idCliente", cliente.getIdCliente(),
                "nombreCompleto", persona.getNombres() + " " + persona.getApellidos()
        ));
    }
}
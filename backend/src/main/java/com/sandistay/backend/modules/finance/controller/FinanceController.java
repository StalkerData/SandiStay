package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.dto.GenerateInvoiceRequest;
import com.sandistay.backend.modules.finance.service.ReportService;
import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import com.sandistay.backend.modules.finance.service.FinanceService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finance")
@SecurityRequirement(name = "Bearer Authentication") 
public class FinanceController {

	private final ReportService reportService;
    private final FinanceService financeService; // <--- 1. AGREGAMOS LA VARIABLE

    // 2. ACTUALIZAMOS EL CONSTRUCTOR PARA INYECTAR AMBOS SERVICIOS
    public FinanceController(ReportService reportService, FinanceService financeService) {
        this.reportService = reportService;
        this.financeService = financeService;
    }


    /**
     * Endpoint protegido para generar una factura en BD y descargar su PDF.
     * 
     * @param request DTO con el ID de la reserva.
     * @param authentication Inyectado por Spring Security (Usuario Staff).
     * @return Archivo binario (PDF) con código 201 Created.
     */
    @PostMapping(value = "/invoices/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateInvoicePdf(
            @Valid @RequestBody GenerateInvoiceRequest request,
            Authentication authentication) {

        // 1. Obtener el ID del usuario (Staff) que genera la factura
        Integer idUsuarioActual = 1;
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            // TODO: Buscar el ID real en la BD usando authentication.getName()
            idUsuarioActual = 1;
        }

        // 2. Llamar al servicio para ejecutar el SP en Postgres y compilar el PDF
        byte[] pdfBytes = reportService.generarFacturaPdf(request.idReserva(), idUsuarioActual);

        // 3. Construir las cabeceras HTTP correctas
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "factura-" + request.idReserva() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.CREATED);
    }
    /**
     * Endpoint protegido para consultar el estado de cuenta (balance) de una reserva.
     */
    @GetMapping("/balance/{idReserva}")
    public ResponseEntity<EstadoCuentaView> getBalance(@PathVariable Integer idReserva) {
        EstadoCuentaView balance = financeService.getBalance(idReserva);
        return ResponseEntity.ok(balance);
    }
        
}
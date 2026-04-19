package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.dto.GenerateInvoiceRequest;
import com.sandistay.backend.modules.finance.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finance")
public class FinanceController {

    private final ReportService reportService;

    public FinanceController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Endpoint protegido para generar una factura en BD y descargar su PDF.
     * 
     * @param request DTO con el ID de la reserva.
     * @param authentication Inyectado por Spring Security (Usuario Staff).
     * @return Archivo binario (PDF) con código 201 Created.
     */
    @PostMapping("/invoices/generate")
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
        
}
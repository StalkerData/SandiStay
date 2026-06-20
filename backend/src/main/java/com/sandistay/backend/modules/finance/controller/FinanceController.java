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

        Integer idUsuarioActual = 1;
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            idUsuarioActual = 1;
        }

        // CORRECCIÓN: Ahora usamos request.codigoReserva()
        byte[] pdfBytes = reportService.generarFacturaPdf(request.codigoReserva(), idUsuarioActual);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // CORRECCIÓN: Usamos el código en el nombre del archivo
        headers.setContentDispositionFormData("inline", "factura-" + request.codigoReserva() + ".pdf");
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
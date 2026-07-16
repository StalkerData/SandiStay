package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador Financiero General.
 * Maneja la generación de facturas y la consulta de balances.
 */
@RestController
@RequestMapping("/finance")
@SecurityRequirement(name = "Bearer Authentication")
public class FinanceController {

    private final ReportService reportService;
    private final FinanceService financeService;
    private final UsuarioRepository usuarioRepository;

    // Constructor manual explícito (Sin Lombok)
    public FinanceController(ReportService reportService, FinanceService financeService, UsuarioRepository usuarioRepository) {
        this.reportService = reportService;
        this.financeService = financeService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Endpoint protegido para generar una factura en BD y descargar su PDF.
     */
    @PostMapping(value = "/invoices/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<byte[]> generateInvoicePdf(
            @Valid @RequestBody GenerateInvoiceRequest request,
            Authentication authentication) {

        Integer idUsuarioActual = obtenerIdUsuario(authentication);

        byte[] pdfBytes = reportService.generarFacturaPdf(request.codigoReserva(), idUsuarioActual);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "factura-" + request.codigoReserva() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.CREATED);
    }

    /**
     * Endpoint protegido para consultar el estado de cuenta (balance) de una reserva.
     */
    @GetMapping("/balance/{idReserva}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<EstadoCuentaView> getBalance(@PathVariable Integer idReserva) {
        EstadoCuentaView balance = financeService.getBalance(idReserva);
        return ResponseEntity.ok(balance);
    }

    /**
     * Método de utilidad para resolver el ID real del usuario que ejecuta la acción.
     */
    private Integer obtenerIdUsuario(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            return usuarioRepository.findByUsername(authentication.getName())
                    .map(Usuario::getIdUsuario)
                    .orElse(1);
        }
        return 1;
    }
}

package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.finance.dto.RegisterPaymentRequest;
import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import com.sandistay.backend.modules.finance.service.TreasuryService;
import com.sandistay.backend.modules.payment.model.Pago;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/finance/treasury")
@PreAuthorize("hasRole('ADMIN')") // SEGURIDAD ESTRICTA: Toda la clase es solo para ADMIN
@SecurityRequirement(name = "Bearer Authentication")
public class TreasuryController {

    private final TreasuryService treasuryService;

    public TreasuryController(TreasuryService treasuryService) {
        this.treasuryService = treasuryService;
    }

    @GetMapping("/receivables")
    public ResponseEntity<List<EstadoCuentaView>> getReceivables() {
        return ResponseEntity.ok(treasuryService.getReceivables());
    }

    @GetMapping("/refunds")
    public ResponseEntity<List<Pago>> getPendingRefunds() {
        return ResponseEntity.ok(treasuryService.getPendingRefunds());
    }

    @PostMapping("/payments")
    public ResponseEntity<Pago> registerPayment(
            @Valid @RequestBody RegisterPaymentRequest request,
            Authentication authentication) {
        
        // TODO: Extraer ID real del Admin. Usamos 1 por defecto para el MVP.
        Integer idAdmin = 1; 
        Pago nuevoPago = treasuryService.registerPayment(request, idAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPago);
    }

    @PatchMapping("/refunds/{idPago}/process")
    public ResponseEntity<String> processRefund(
            @PathVariable Integer idPago,
            Authentication authentication) {
        
        Integer idAdmin = 1;
        treasuryService.processRefund(idPago, idAdmin);
        return ResponseEntity.ok("Reembolso procesado y marcado como DEVUELTO exitosamente.");
    }
}
package com.sandistay.backend.modules.finance.controller;

import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
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
import java.util.Map;

/**
 * Controlador de Tesorería.
 * Maneja el flujo de caja manual (Abonos y Reembolsos).
 */
@RestController
@RequestMapping("/finance/treasury")
@PreAuthorize("hasRole('ADMIN')") // SEGURIDAD ESTRICTA: Toda la clase es solo para ADMIN
@SecurityRequirement(name = "Bearer Authentication")
public class TreasuryController {

    private final TreasuryService treasuryService;
    private final UsuarioRepository usuarioRepository;

    // Constructor manual explícito (Sin Lombok)
    public TreasuryController(TreasuryService treasuryService, UsuarioRepository usuarioRepository) {
        this.treasuryService = treasuryService;
        this.usuarioRepository = usuarioRepository;
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

        Integer idAdmin = obtenerIdUsuario(authentication);
        Pago nuevoPago = treasuryService.registerPayment(request, idAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPago);
    }

    @PatchMapping("/refunds/{idPago}/process")
    public ResponseEntity<Map<String, String>> processRefund(
            @PathVariable Integer idPago,
            Authentication authentication) {

        Integer idAdmin = obtenerIdUsuario(authentication);
        treasuryService.processRefund(idPago, idAdmin);
        return ResponseEntity.ok(Map.of("message", "Reembolso procesado y marcado como DEVUELTO exitosamente."));
    }

    /**
     * Método de utilidad para resolver el ID real del administrador que ejecuta la acción.
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

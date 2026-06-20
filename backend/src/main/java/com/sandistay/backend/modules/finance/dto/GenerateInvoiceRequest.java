package com.sandistay.backend.modules.finance.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO inmutable para solicitar la generación de una factura.
 */
public record GenerateInvoiceRequest(
        @NotBlank(message = "El código de la reserva es obligatorio")
        String codigoReserva // Antes era Integer idReserva
) {}
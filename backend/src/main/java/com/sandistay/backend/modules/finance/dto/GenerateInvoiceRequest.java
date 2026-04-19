package com.sandistay.backend.modules.finance.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO inmutable para solicitar la generación de una factura.
 */
public record GenerateInvoiceRequest(
        @NotNull(message = "El ID de la reserva es obligatorio")
        Integer idReserva
) {}
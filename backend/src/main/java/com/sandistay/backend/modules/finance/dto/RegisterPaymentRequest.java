package com.sandistay.backend.modules.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record RegisterPaymentRequest(
        @NotNull(message = "El ID de la reserva es obligatorio")
        Integer idReserva,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
        BigDecimal monto,

        @NotNull(message = "El método de pago es obligatorio")
        Integer idMetodoPago,

        String referencia,
        String observaciones
) {}
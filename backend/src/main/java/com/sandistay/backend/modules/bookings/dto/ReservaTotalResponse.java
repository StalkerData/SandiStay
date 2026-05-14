package com.sandistay.backend.modules.bookings.dto;

import java.math.BigDecimal;

public record ReservaTotalResponse(
        BigDecimal subtotalHabitaciones,
        BigDecimal subtotalAreas,
        BigDecimal descuentoCliente,
        BigDecimal totalFinal
) {}
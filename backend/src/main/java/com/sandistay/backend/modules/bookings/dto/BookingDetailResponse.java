package com.sandistay.backend.modules.bookings.dto;

import com.sandistay.backend.modules.bookings.model.ReservaHabitacionView;
import com.sandistay.backend.modules.bookings.model.ReservaView;
import com.sandistay.backend.modules.payment.model.Pago;

import java.util.List;

/**
 * DTO inmutable que agrupa toda la información de una reserva (Cabecera, Detalles y Pagos).
 * Cumple con el contrato de API para la vista de detalle del Dashboard Administrativo.
 */
public record BookingDetailResponse(
        ReservaView reserva,
        List<ReservaHabitacionView> items,
        List<Pago> pagos
) {}
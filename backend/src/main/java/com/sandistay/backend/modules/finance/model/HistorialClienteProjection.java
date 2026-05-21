package com.sandistay.backend.modules.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Proyección JPA para mapear el resultado de la función SQL 'fn_historial_cliente'.
 */
public interface HistorialClienteProjection {
    Long getTotalReservas();
    Long getReservasCompletadas();
    Long getReservasCanceladas();
    BigDecimal getTotalGastado();
    LocalDate getUltimaReserva();
    String getHabitacionFavorita();
}
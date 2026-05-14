package com.sandistay.backend.modules.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Proyección JPA para mapear el resultado de la función SQL 'fn_reporte_ocupacion'.
 */
public interface OcupacionProjection {
    LocalDate getFecha();
    Long getHabitacionesOcupadas();
    Long getHabitacionesDisponibles();
    BigDecimal getPorcentajeOcupacion();
    BigDecimal getIngresoEstimado();
}
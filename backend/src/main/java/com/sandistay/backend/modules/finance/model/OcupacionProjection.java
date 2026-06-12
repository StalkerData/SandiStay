package com.sandistay.backend.modules.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OcupacionProjection {
    LocalDate getOut_fecha();
    String getOut_tipo();
    Long getOut_ocupadas();
    BigDecimal getOut_ingreso();
}
package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import org.springframework.data.repository.Repository;
import java.math.BigDecimal;
import java.util.List;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface EstadoCuentaViewRepository extends Repository<EstadoCuentaView, Integer> {
    
    /**
     * Busca el estado de cuenta de una reserva específica usando la vista SQL.
     */
    Optional<EstadoCuentaView> findById(Integer idReserva);
    
    /**
     * Busca todas las reservas que tienen un saldo pendiente mayor a cero (Cuentas por Cobrar).
     */
    List<EstadoCuentaView> findBySaldoPendienteGreaterThan(BigDecimal monto);
}
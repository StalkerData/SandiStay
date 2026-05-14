package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import org.springframework.data.repository.Repository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface EstadoCuentaViewRepository extends Repository<EstadoCuentaView, Integer> {
    
    /**
     * Busca el estado de cuenta de una reserva específica usando la vista SQL.
     */
    Optional<EstadoCuentaView> findById(Integer idReserva);
}
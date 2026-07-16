package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import com.sandistay.backend.modules.finance.repository.EstadoCuentaViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio Financiero General.
 * Maneja consultas rápidas sobre el estado de cuenta de las reservas.
 */
@Service
public class FinanceService {

    private final EstadoCuentaViewRepository estadoCuentaViewRepository;

    // Constructor manual explícito
    public FinanceService(EstadoCuentaViewRepository estadoCuentaViewRepository) {
        this.estadoCuentaViewRepository = estadoCuentaViewRepository;
    }

    /**
     * Obtiene el balance financiero de una reserva.
     */
    @Transactional(readOnly = true)
    public EstadoCuentaView getBalance(Integer idReserva) {
        return estadoCuentaViewRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el estado de cuenta para la reserva especificada."));
    }
}

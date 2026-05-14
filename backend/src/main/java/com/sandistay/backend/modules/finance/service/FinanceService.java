package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import com.sandistay.backend.modules.finance.repository.EstadoCuentaViewRepository;
import org.springframework.stereotype.Service;

@Service
public class FinanceService {

    private final EstadoCuentaViewRepository estadoCuentaViewRepository;

    public FinanceService(EstadoCuentaViewRepository estadoCuentaViewRepository) {
        this.estadoCuentaViewRepository = estadoCuentaViewRepository;
    }

    /**
     * Obtiene el balance financiero de una reserva.
     */
    public EstadoCuentaView getBalance(Integer idReserva) {
        return estadoCuentaViewRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("No se encontró el estado de cuenta para la reserva especificada."));
    }
}
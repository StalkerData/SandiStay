package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.ReservaHabitacionView;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface ReservaHabitacionViewRepository extends Repository<ReservaHabitacionView, Integer> {
    
    /**
     * Busca los detalles de las habitaciones asociadas a una reserva específica.
     * Retorna una lista porque una reserva podría tener múltiples habitaciones.
     */
    List<ReservaHabitacionView> findByIdReserva(Integer idReserva);
}
package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.ReservaHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaHabitacionRepository extends JpaRepository<ReservaHabitacion, Integer> {

    /**
     * Busca todos los detalles de habitación asociados a una reserva específica.
     */
    List<ReservaHabitacion> findByIdReserva(Integer idReserva);
}
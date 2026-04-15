package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.ReservaArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaAreaRepository extends JpaRepository<ReservaArea, Integer> {

    /**
     * Busca todos los detalles de área asociados a una reserva específica.
     */
    List<ReservaArea> findByIdReserva(Integer idReserva);
}
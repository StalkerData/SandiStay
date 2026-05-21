package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.ReservaView;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface ReservaViewRepository extends Repository<ReservaView, Integer> {
    
    List<ReservaView> findAllByOrderByFechaReservaDesc();

    Optional<ReservaView> findById(Integer idReserva);
}
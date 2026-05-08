package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.ReservaView;
import org.springframework.data.repository.Repository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface ReservaViewRepository extends Repository<ReservaView, Integer> {
    // Spring Data JPA hace la magia: "SELECT * FROM v_reservas_completo ORDER BY fechareserva DESC"
    List<ReservaView> findAllByOrderByFechaReservaDesc();
}
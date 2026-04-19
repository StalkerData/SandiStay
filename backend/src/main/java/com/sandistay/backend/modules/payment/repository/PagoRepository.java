package com.sandistay.backend.modules.payment.repository;

import com.sandistay.backend.modules.payment.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    /**
     * Busca todos los pagos asociados a una reserva.
     * Útil para encontrar el pago original que vamos a "Cancelar" lógicamente.
     */
    List<Pago> findByIdReserva(Integer idReserva);
}
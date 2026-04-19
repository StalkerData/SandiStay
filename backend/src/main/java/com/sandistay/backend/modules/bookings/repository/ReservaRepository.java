package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository 
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    /**
     * Busca una reserva por su código único (ej: RES-2026-001).
     * Útil para cuando el cliente quiere consultar el estado de su reserva con el código que le llegó al correo.
     */
    Optional<Reserva> findByCodigoReserva(String codigoReserva);

    /**
     * Verifica si existe una reserva con un código específico.
     * Útil para generar códigos únicos sin colisiones.
     */
    boolean existsByCodigoReserva(String codigoReserva);
    
    /**
     * Llama al Procedimiento Almacenado de PostgreSQL para cancelar la reserva
     * y liberar el inventario de forma segura.
     */
    @Modifying
    @Query(value = "CALL sp_cancelar_reserva(:idReserva, :motivo, :idUsuario)", nativeQuery = true)
    void llamarSpCancelarReserva(
            @Param("idReserva") Integer idReserva,
            @Param("motivo") String motivo,
            @Param("idUsuario") Integer idUsuario
    );
}
package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.modules.bookings.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sandistay.backend.modules.bookings.model.ReservaTotalProjection;
import java.time.LocalDateTime;
import java.util.List;


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
    /**
     * Llama al Procedimiento Almacenado de PostgreSQL para realizar el Check-out.
     * Valida que el saldo sea 0 y cambia el estado de la habitación a Limpieza.
     */
    @Modifying
    @Query(value = "CALL sp_completar_reserva(:idReserva, :idUsuario)", nativeQuery = true)
    void llamarSpCompletarReserva(
            @Param("idReserva") Integer idReserva,
            @Param("idUsuario") Integer idUsuario
    );
    
    /**
     * Llama a la función SQL para calcular los totales y descuentos de una reserva.
     * Retorna una proyección con los 4 campos calculados por PostgreSQL.
     */
    @Query(value = "SELECT * FROM fn_calcular_total_reserva(:idReserva)", nativeQuery = true)
    ReservaTotalProjection calcularTotalReserva(@Param("idReserva") Integer idReserva);
    
    /**
     * Busca reservas por estado y que hayan sido creadas antes de una fecha/hora específica.
     * Útil para encontrar reservas pendientes que ya expiraron su tiempo de pago (24h).
     */
    List<Reserva> findByIdEstadoReservaAndFechaCreacionBefore(Integer idEstadoReserva, LocalDateTime fechaLimite);
}
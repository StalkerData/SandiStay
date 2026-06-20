package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.finance.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    @Query(value = "CALL sp_generar_factura(:p_id_reserva, :p_id_usuario, null)", nativeQuery = true)
    Integer generarFactura(
            @Param("p_id_reserva") Integer idReserva,
            @Param("p_id_usuario") Integer idUsuario
    );

    // NUEVO MÉTODO: Busca la última factura generada para una reserva
    Optional<Factura> findFirstByIdReservaOrderByFechaCreacionDesc(Integer idReserva);
}
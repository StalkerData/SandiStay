package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.finance.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    /**
     * Llama al Procedimiento Almacenado de PostgreSQL para generar la factura y sus detalles.
     * Usamos una consulta nativa (CALL) en lugar de @Procedure para evitar problemas de sintaxis
     * con los parámetros OUT en el driver JDBC de PostgreSQL.
     * 
     * @param idReserva El ID de la reserva a facturar.
     * @param idUsuario El ID del usuario (Staff) que genera la factura.
     * @return El ID de la factura generada (parámetro OUT del SP).
     */
    @Query(value = "CALL sp_generar_factura(:p_id_reserva, :p_id_usuario, null)", nativeQuery = true)
    Integer generarFactura(
            @Param("p_id_reserva") Integer idReserva,
            @Param("p_id_usuario") Integer idUsuario
    );
}
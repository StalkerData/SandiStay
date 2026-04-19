package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.finance.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    /**
     * Llama al Procedimiento Almacenado de PostgreSQL para generar la factura y sus detalles.
     * 
     * @param idReserva El ID de la reserva a facturar.
     * @param idUsuario El ID del usuario (Staff) que genera la factura.
     * @return El ID de la factura generada (parámetro OUT del SP).
     */
    @Procedure(procedureName = "sp_generar_factura")
    Integer generarFactura(
            @Param("p_id_reserva") Integer idReserva,
            @Param("p_id_usuario") Integer idUsuario
    );
}
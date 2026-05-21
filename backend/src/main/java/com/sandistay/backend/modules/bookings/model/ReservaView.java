package com.sandistay.backend.modules.bookings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "v_reservas_completo")
public class ReservaView {

    @Id
    @Column(name = "idreserva")
    private Integer idReserva;

    @Column(name = "codigoreserva")
    private String codigoReserva;

    @Column(name = "tiporeserva")
    private String tipoReserva;

    @Column(name = "nombrecliente")
    private String nombreCliente;

    @Column(name = "nombrehuesped")
    private String nombreHuesped;

    @Column(name = "fechareserva")
    private LocalDateTime fechaReserva;

    private String estado;

    @Column(name = "estadocolor")
    private String estadoColor;

    // Getters básicos que necesita SvelteKit
    public Integer getIdReserva() { return idReserva; }
    public String getCodigoReserva() { return codigoReserva; }
    public String getTipoReserva() { return tipoReserva; }
    public String getNombreCliente() { return nombreCliente; }
    public String getNombreHuesped() { return nombreHuesped; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public String getEstado() { return estado; }
    public String getEstadoColor() { return estadoColor; }
}
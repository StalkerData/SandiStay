package com.sandistay.backend.modules.finance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "v_estado_cuenta_reservas")
public class EstadoCuentaView {

    @Id
    @Column(name = "idreserva")
    private Integer idReserva;

    @Column(name = "codigoreserva")
    private String codigoReserva;

    @Column(name = "tiporeserva")
    private String tipoReserva;

    @Column(name = "codigocliente")
    private String codigoCliente;

    @Column(name = "nombrecliente")
    private String nombreCliente;

    @Column(name = "totalreserva")
    private BigDecimal totalReserva;

    @Column(name = "totalpagado")
    private BigDecimal totalPagado;

    @Column(name = "saldopendiente")
    private BigDecimal saldoPendiente;

    @Column(name = "estadoreserva")
    private String estadoReserva;

    @Column(name = "estadoreservanombre")
    private String estadoReservaNombre;

    // ==========================================
    // GETTERS (Sin Setters porque es @Immutable)
    // ==========================================
    public Integer getIdReserva() { return idReserva; }
    public String getCodigoReserva() { return codigoReserva; }
    public String getTipoReserva() { return tipoReserva; }
    public String getCodigoCliente() { return codigoCliente; }
    public String getNombreCliente() { return nombreCliente; }
    public BigDecimal getTotalReserva() { return totalReserva; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public String getEstadoReserva() { return estadoReserva; }
    public String getEstadoReservaNombre() { return estadoReservaNombre; }
}
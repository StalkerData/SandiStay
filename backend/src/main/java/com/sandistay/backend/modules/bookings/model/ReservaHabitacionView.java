package com.sandistay.backend.modules.bookings.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "v_reservas_habitaciones_detalle")
public class ReservaHabitacionView {

    @Id
    @Column(name = "idreservahabitacion")
    private Integer idReservaHabitacion;

    @Column(name = "idreserva")
    private Integer idReserva;

    @Column(name = "codigoreserva")
    private String codigoReserva;

    @Column(name = "idhabitacion")
    private Integer idHabitacion;

    @Column(name = "codigohabitacion")
    private String codigoHabitacion;

    @Column(name = "numerohabitacion")
    private String numeroHabitacion;

    @Column(name = "nombrehabitacion")
    private String nombreHabitacion;

    @Column(name = "fechainicio")
    private LocalDate fechaInicio;

    @Column(name = "fechafin")
    private LocalDate fechaFin;

    @Column(name = "numeronoches")
    private Integer numeroNoches;

    @Column(name = "numerodias")
    private Integer numeroDias;

    @Column(name = "precionoche")
    private BigDecimal precioNoche;

    @Column(name = "preciodia")
    private BigDecimal precioDia;

    @Column(name = "totalnoches")
    private BigDecimal totalNoches;

    @Column(name = "totaldias")
    private BigDecimal totalDias;

    @Column(name = "totalreserva")
    private BigDecimal totalReserva;

    private Boolean activo;

    @Column(name = "estadoreserva")
    private String estadoReserva;

    @Column(name = "estadoreservanombre")
    private String estadoReservaNombre;

    // ==========================================
    // GETTERS (Sin Setters porque es @Immutable)
    // ==========================================
    public Integer getIdReservaHabitacion() { return idReservaHabitacion; }
    public Integer getIdReserva() { return idReserva; }
    public String getCodigoReserva() { return codigoReserva; }
    public Integer getIdHabitacion() { return idHabitacion; }
    public String getCodigoHabitacion() { return codigoHabitacion; }
    public String getNumeroHabitacion() { return numeroHabitacion; }
    public String getNombreHabitacion() { return nombreHabitacion; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Integer getNumeroNoches() { return numeroNoches; }
    public Integer getNumeroDias() { return numeroDias; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public BigDecimal getPrecioDia() { return precioDia; }
    public BigDecimal getTotalNoches() { return totalNoches; }
    public BigDecimal getTotalDias() { return totalDias; }
    public BigDecimal getTotalReserva() { return totalReserva; }
    public Boolean getActivo() { return activo; }
    public String getEstadoReserva() { return estadoReserva; }
    public String getEstadoReservaNombre() { return estadoReservaNombre; }
}
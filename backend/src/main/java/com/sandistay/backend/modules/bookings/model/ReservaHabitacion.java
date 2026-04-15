package com.sandistay.backend.modules.bookings.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservahabitacion")
public class ReservaHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idreservahabitacion")
    private Integer idReservaHabitacion;

    @Column(name = "idreserva", nullable = false)
    private Integer idReserva;

    @Column(name = "idhabitacion", nullable = false)
    private Integer idHabitacion;

    @Column(name = "fechainicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fechafin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "numeronoches", nullable = false)
    private Integer numeroNoches;

    @Column(name = "numerodias", nullable = false)
    private Integer numeroDias;

    @Column(name = "precionoche", nullable = false)
    private BigDecimal precioNoche;

    @Column(name = "preciodia", nullable = false)
    private BigDecimal precioDia;

    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    public ReservaHabitacion() {}
     // ==========================================
    // GENERAR GETTERS Y SETTERS CON EL IDE AQUÍ
    // ==========================================

	public Integer getIdReservaHabitacion() {
		return idReservaHabitacion;
	}

	public void setIdReservaHabitacion(Integer idReservaHabitacion) {
		this.idReservaHabitacion = idReservaHabitacion;
	}

	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public Integer getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Integer idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(LocalDate fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDate fechaFin) {
		this.fechaFin = fechaFin;
	}

	public Integer getNumeroNoches() {
		return numeroNoches;
	}

	public void setNumeroNoches(Integer numeroNoches) {
		this.numeroNoches = numeroNoches;
	}

	public Integer getNumeroDias() {
		return numeroDias;
	}

	public void setNumeroDias(Integer numeroDias) {
		this.numeroDias = numeroDias;
	}

	public BigDecimal getPrecioNoche() {
		return precioNoche;
	}

	public void setPrecioNoche(BigDecimal precioNoche) {
		this.precioNoche = precioNoche;
	}

	public BigDecimal getPrecioDia() {
		return precioDia;
	}

	public void setPrecioDia(BigDecimal precioDia) {
		this.precioDia = precioDia;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

    
    
}
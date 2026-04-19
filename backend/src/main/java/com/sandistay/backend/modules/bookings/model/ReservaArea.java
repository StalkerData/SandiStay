package com.sandistay.backend.modules.bookings.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservaarea")
public class ReservaArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idreservaarea")
    private Integer idReservaArea;

    @Column(name = "idreserva", nullable = false)
    private Integer idReserva;

    @Column(name = "idarea", nullable = false)
    private Integer idArea;

    @Column(name = "fechaevento", nullable = false)
    private LocalDate fechaEvento;

    @Column(name = "horainicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "horafin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "duracionhoras", nullable = false)
    private BigDecimal duracionHoras;

    @Column(name = "preciohora", nullable = false)
    private BigDecimal precioHora;

    @Column(name = "cantidadasistentes")
    private Integer cantidadAsistentes;

    @Column(name = "requiereanticipo")
    private Boolean requiereAnticipo = false;

    @Column(name = "porcentajeanticipo")
    private BigDecimal porcentajeAnticipo = BigDecimal.ZERO;

    private Boolean activo = true;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    public ReservaArea() {}
    
     // ==========================================
    // GETTERS Y SETTERS
    // ==========================================
    

	public Integer getIdReservaArea() {
		return idReservaArea;
	}

	public void setIdReservaArea(Integer idReservaArea) {
		this.idReservaArea = idReservaArea;
	}

	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public Integer getIdArea() {
		return idArea;
	}

	public void setIdArea(Integer idArea) {
		this.idArea = idArea;
	}

	public LocalDate getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(LocalDate fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	public BigDecimal getDuracionHoras() {
		return duracionHoras;
	}

	public void setDuracionHoras(BigDecimal duracionHoras) {
		this.duracionHoras = duracionHoras;
	}

	public BigDecimal getPrecioHora() {
		return precioHora;
	}

	public void setPrecioHora(BigDecimal precioHora) {
		this.precioHora = precioHora;
	}

	public Integer getCantidadAsistentes() {
		return cantidadAsistentes;
	}

	public void setCantidadAsistentes(Integer cantidadAsistentes) {
		this.cantidadAsistentes = cantidadAsistentes;
	}

	public Boolean getRequiereAnticipo() {
		return requiereAnticipo;
	}

	public void setRequiereAnticipo(Boolean requiereAnticipo) {
		this.requiereAnticipo = requiereAnticipo;
	}

	public BigDecimal getPorcentajeAnticipo() {
		return porcentajeAnticipo;
	}

	public void setPorcentajeAnticipo(BigDecimal porcentajeAnticipo) {
		this.porcentajeAnticipo = porcentajeAnticipo;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

    
    
}
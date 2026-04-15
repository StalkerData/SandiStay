package com.sandistay.backend.modules.bookings.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public class Reserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idreserva")
	private Integer idReserva;

	@Column(name = "codigoreserva", unique = true, nullable = false, length = 50)
	private String codigoReserva;

	@Column(name = "tiporeserva", nullable = false, length = 20)
	private String tipoReserva; // 'Habitacion' o 'Area'

	// Referencia al ID del Cliente (DDD: Referencia por ID a otro Agregado)
	@Column(name = "idcliente", nullable = false)
	private Integer idCliente;

	@Column(name = "nombrehuesped", nullable = false, length = 200)
	private String nombreHuesped;

	@Column(name = "documentohuesped", length = 50)
	private String documentoHuesped;

	@Column(name = "telefonohuesped", length = 20)
	private String telefonoHuesped;

	@Column(name = "fechareserva", insertable = false, updatable = false)
	private LocalDateTime fechaReserva;

	@Column(name = "numeroadultos")
	private Integer numeroAdultos = 0;

	@Column(name = "numeroninos")
	private Integer numeroNinos = 0;

	@Column(name = "idestadoreserva", nullable = false)
	private Integer idEstadoReserva;

	@Column(name = "motivocancelacion", columnDefinition = "TEXT")
	private String motivoCancelacion;

	@Column(name = "fechacancelacion")
	private LocalDateTime fechaCancelacion;

	@Column(columnDefinition = "TEXT")
	private String observaciones;

	@Column(name = "idusuariocreacion", nullable = false)
	private Integer idUsuarioCreacion;

	@Column(name = "fechacreacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	// ==========================================
	// CONSTRUCTORES
	// ==========================================
	public Reserva() {
	}

	// ==========================================
	// GENERAR GETTERS Y SETTERS CON EL IDE AQUÍ
	// ==========================================

	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public String getCodigoReserva() {
		return codigoReserva;
	}

	public void setCodigoReserva(String codigoReserva) {
		this.codigoReserva = codigoReserva;
	}

	public String getTipoReserva() {
		return tipoReserva;
	}

	public void setTipoReserva(String tipoReserva) {
		this.tipoReserva = tipoReserva;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Integer idCliente) {
		this.idCliente = idCliente;
	}

	public String getNombreHuesped() {
		return nombreHuesped;
	}

	public void setNombreHuesped(String nombreHuesped) {
		this.nombreHuesped = nombreHuesped;
	}

	public String getDocumentoHuesped() {
		return documentoHuesped;
	}

	public void setDocumentoHuesped(String documentoHuesped) {
		this.documentoHuesped = documentoHuesped;
	}

	public String getTelefonoHuesped() {
		return telefonoHuesped;
	}

	public void setTelefonoHuesped(String telefonoHuesped) {
		this.telefonoHuesped = telefonoHuesped;
	}

	public LocalDateTime getFechaReserva() {
		return fechaReserva;
	}

	public void setFechaReserva(LocalDateTime fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	public Integer getNumeroAdultos() {
		return numeroAdultos;
	}

	public void setNumeroAdultos(Integer numeroAdultos) {
		this.numeroAdultos = numeroAdultos;
	}

	public Integer getNumeroNinos() {
		return numeroNinos;
	}

	public void setNumeroNinos(Integer numeroNinos) {
		this.numeroNinos = numeroNinos;
	}

	public Integer getIdEstadoReserva() {
		return idEstadoReserva;
	}

	public void setIdEstadoReserva(Integer idEstadoReserva) {
		this.idEstadoReserva = idEstadoReserva;
	}

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

	public LocalDateTime getFechaCancelacion() {
		return fechaCancelacion;
	}

	public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Integer getIdUsuarioCreacion() {
		return idUsuarioCreacion;
	}

	public void setIdUsuarioCreacion(Integer idUsuarioCreacion) {
		this.idUsuarioCreacion = idUsuarioCreacion;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

}
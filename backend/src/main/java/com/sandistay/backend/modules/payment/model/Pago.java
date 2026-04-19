package com.sandistay.backend.modules.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpago")
    private Integer idPago;

    @Column(name = "codigopago", unique = true, nullable = false, length = 50)
    private String codigoPago;

    @Column(name = "idreserva", nullable = false)
    private Integer idReserva;

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(name = "idmetodopago", nullable = false)
    private Integer idMetodoPago;

    @Column(name = "numeroreferencia", length = 100)
    private String numeroReferencia;

    @Column(name = "idestadopago", nullable = false)
    private Integer idEstadoPago;

    @Column(name = "fechapago", insertable = false, updatable = false)
    private LocalDateTime fechaPago;

    @Column(name = "idusuarioregistro", nullable = false)
    private Integer idUsuarioRegistro;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================
    public Pago() {}
    
    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

	public Integer getIdPago() {
		return idPago;
	}

	public void setIdPago(Integer idPago) {
		this.idPago = idPago;
	}

	public String getCodigoPago() {
		return codigoPago;
	}

	public void setCodigoPago(String codigoPago) {
		this.codigoPago = codigoPago;
	}

	public Integer getIdReserva() {
		return idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public void setMonto(BigDecimal monto) {
		this.monto = monto;
	}

	public Integer getIdMetodoPago() {
		return idMetodoPago;
	}

	public void setIdMetodoPago(Integer idMetodoPago) {
		this.idMetodoPago = idMetodoPago;
	}

	public String getNumeroReferencia() {
		return numeroReferencia;
	}

	public void setNumeroReferencia(String numeroReferencia) {
		this.numeroReferencia = numeroReferencia;
	}

	public Integer getIdEstadoPago() {
		return idEstadoPago;
	}

	public void setIdEstadoPago(Integer idEstadoPago) {
		this.idEstadoPago = idEstadoPago;
	}

	public LocalDateTime getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(LocalDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}

	public Integer getIdUsuarioRegistro() {
		return idUsuarioRegistro;
	}

	public void setIdUsuarioRegistro(Integer idUsuarioRegistro) {
		this.idUsuarioRegistro = idUsuarioRegistro;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}


}
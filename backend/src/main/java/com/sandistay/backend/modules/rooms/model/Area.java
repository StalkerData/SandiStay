package com.sandistay.backend.modules.rooms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "area")
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idarea")
    private Integer idArea;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "idtipoarea", nullable = false)
    private Integer idTipoArea;

    @Column(name = "capacidadpersonas", nullable = false)
    private Integer capacidadPersonas;

    @Column(name = "idestadolugar", nullable = false)
    private Integer idEstadoLugar;

    @Column(name = "preciohora", nullable = false)
    private BigDecimal precioHora;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Boolean activo = true;

    public Area() {}
    
    // ==========================================
    // GENERAR GETTERS Y SETTERS 
    // ==========================================

	public Integer getIdArea() {
		return idArea;
	}

	public void setIdArea(Integer idArea) {
		this.idArea = idArea;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getIdTipoArea() {
		return idTipoArea;
	}

	public void setIdTipoArea(Integer idTipoArea) {
		this.idTipoArea = idTipoArea;
	}

	public Integer getCapacidadPersonas() {
		return capacidadPersonas;
	}

	public void setCapacidadPersonas(Integer capacidadPersonas) {
		this.capacidadPersonas = capacidadPersonas;
	}

	public Integer getIdEstadoLugar() {
		return idEstadoLugar;
	}

	public void setIdEstadoLugar(Integer idEstadoLugar) {
		this.idEstadoLugar = idEstadoLugar;
	}

	public BigDecimal getPrecioHora() {
		return precioHora;
	}

	public void setPrecioHora(BigDecimal precioHora) {
		this.precioHora = precioHora;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
    
    

}
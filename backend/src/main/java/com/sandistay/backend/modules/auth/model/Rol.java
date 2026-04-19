package com.sandistay.backend.modules.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rol")
public class Rol {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idrol")
	private Integer idRol;

	@Column(unique = true, nullable = false, length = 20)
	private String codigo;

	@Column(nullable = false, length = 100)
	private String nombre;

	private String descripcion;

	@Column(name = "nivelacceso")
	private Integer nivelAcceso;

	private Boolean activo = true;

	// ==========================================
	// CONSTRUCTORES
	// ==========================================
	public Rol() {
	}
	
	// ==========================================
	// GETTERS Y SETTERS
	// ==========================================
	public Rol(String codigo, String nombre) {
		this.codigo = codigo;
		this.nombre = nombre;
	}

	public Integer getIdRol() {
		return idRol;
	}

	public void setIdRol(Integer idRol) {
		this.idRol = idRol;
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

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getNivelAcceso() {
		return nivelAcceso;
	}

	public void setNivelAcceso(Integer nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

}
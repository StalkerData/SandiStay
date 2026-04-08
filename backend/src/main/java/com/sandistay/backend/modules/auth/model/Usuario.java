package com.sandistay.backend.modules.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "idusuario")
	private Integer idUsuario;

	// Mapeamos solo el ID de la Persona para mantener los módulos desacoplados
	// (Bounded Context)
	@Column(name = "idpersona", unique = true, nullable = false)
	private Integer idPersona;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(name = "passwordhash", nullable = false)
	private String passwordHash;

	@Column(name = "passwordsalt", nullable = false)
	private String passwordSalt;

	@Column(name = "intentosfallidos")
	private Integer intentosFallidos = 0;

	private Boolean bloqueado = false;

	@Column(name = "ultimologin")
	private LocalDateTime ultimoLogin;

	@Column(name = "requierecambiopassword")
	private Boolean requiereCambioPassword = false;

	private Boolean activo = true;

	@Column(name = "fechacreacion", insertable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	// Relación Many-to-Many con la tabla Rol a través de UsuarioRol
	// Usamos EAGER porque siempre que carguemos un usuario para login, necesitamos
	// saber sus roles
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "usuariorol", joinColumns = @JoinColumn(name = "idusuario"), inverseJoinColumns = @JoinColumn(name = "idrol"))
	private Set<Rol> roles = new HashSet<>();

	// ==========================================
	// CONSTRUCTORES
	// ==========================================
	public Usuario() {
	}

	public Usuario(Integer idPersona, String username, String passwordHash, String passwordSalt) {
		this.idPersona = idPersona;
		this.username = username;
		this.passwordHash = passwordHash;
		this.passwordSalt = passwordSalt;
	}

	// ==========================================
	// GETTERS Y SETTERS
	// ==========================================
	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(Integer idPersona) {
		this.idPersona = idPersona;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getPasswordSalt() {
		return passwordSalt;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	public Integer getIntentosFallidos() {
		return intentosFallidos;
	}

	public void setIntentosFallidos(Integer intentosFallidos) {
		this.intentosFallidos = intentosFallidos;
	}

	public Boolean getBloqueado() {
		return bloqueado;
	}

	public void setBloqueado(Boolean bloqueado) {
		this.bloqueado = bloqueado;
	}

	public LocalDateTime getUltimoLogin() {
		return ultimoLogin;
	}

	public void setUltimoLogin(LocalDateTime ultimoLogin) {
		this.ultimoLogin = ultimoLogin;
	}

	public Boolean getRequiereCambioPassword() {
		return requiereCambioPassword;
	}

	public void setRequiereCambioPassword(Boolean requiereCambioPassword) {
		this.requiereCambioPassword = requiereCambioPassword;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Set<Rol> getRoles() {
		return roles;
	}

	public void setRoles(Set<Rol> roles) {
		this.roles = roles;
	}
}
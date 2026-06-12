package com.sandistay.backend.modules.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "v_usuarios_roles")
public class UsuarioView {
    @Id
    @Column(name = "idusuario")
    private Integer idUsuario;
    private String username;
    @Column(name = "nombrecompleto")
    private String nombreCompleto;
    private String email;
    private String rol;
    @Column(name = "usuarioactivo")
    private Boolean usuarioActivo;
    @Column(name = "ultimologin")
    private LocalDateTime ultimoLogin;

    // Getters
    public Integer getIdUsuario() { return idUsuario; }
    public String getUsername() { return username; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public Boolean getUsuarioActivo() { return usuarioActivo; }
    public LocalDateTime getUltimoLogin() { return ultimoLogin; }
}
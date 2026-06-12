package com.sandistay.backend.modules.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "persona")
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpersona")
    private Integer idPersona;

    private String nombres;
    private String apellidos;

    @Column(name = "idtipodocumento")
    private Integer idTipoDocumento;

    @Column(name = "numerodocumento")
    private String numeroDocumento;

    private String email;
    
    @Column(name = "telefono")
    private String telefono;

    @Column(name = "idpais")
    private Integer idPais;

    @Column(name = "idestadopersona")
    private Integer idEstadoPersona;

    // Genera los Getters y Setters con tu IDE
    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public Integer getIdTipoDocumento() { return idTipoDocumento; }
    public void setIdTipoDocumento(Integer idTipoDocumento) { this.idTipoDocumento = idTipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() {return telefono;}
    public void setTelefono(String telefono) {this.telefono = telefono;}
    public Integer getIdPais() { return idPais; }
    public void setIdPais(Integer idPais) { this.idPais = idPais; }
    public Integer getIdEstadoPersona() { return idEstadoPersona; }
    public void setIdEstadoPersona(Integer idEstadoPersona) { this.idEstadoPersona = idEstadoPersona; }
}
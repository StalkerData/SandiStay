package com.sandistay.backend.modules.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Integer idCliente;

    @Column(name = "idpersona", nullable = false, unique = true)
    private Integer idPersona;

    @Column(name = "codigocliente", nullable = false, unique = true, length = 50)
    private String codigoCliente;

    @Column(name = "idtipocliente", nullable = false)
    private Integer idTipoCliente;

    public Cliente() {}

    // Genera los Getters y Setters con tu IDE
    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }
    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }
    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }
    public Integer getIdTipoCliente() { return idTipoCliente; }
    public void setIdTipoCliente(Integer idTipoCliente) { this.idTipoCliente = idTipoCliente; }
}
package com.sandistay.backend.modules.rooms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Entidad JPA de solo lectura mapeada a la vista SQL 'v_habitaciones_disponibles'.
 * Se utiliza para consultar el catálogo sin hacer JOINs complejos en Java.
 */
@Entity
@Immutable
@Table(name = "v_habitaciones_disponibles")
public class HabitacionView {

    @Id
    @Column(name = "idhabitacion")
    private Integer idHabitacion;

    private String codigo;

    private String numero;

    private String nombre;

    @Column(name = "tipohabitacioncodigo")
    private String tipoHabitacionCodigo;

    @Column(name = "tipohabitacion")
    private String tipoHabitacion;

    @Column(name = "capacidadadultos")
    private Integer capacidadAdultos;

    @Column(name = "capacidadninos")
    private Integer capacidadNinos;

    @Column(name = "capacidadmaxima")
    private Integer capacidadMaxima;

    @Column(name = "estadocodigo")
    private String estadoCodigo;

    private String estado;

    @Column(name = "estadocolor")
    private String estadoColor;

    @Column(name = "precionoche")
    private BigDecimal precioNoche;

    @Column(name = "preciodia")
    private BigDecimal precioDia;

    private String descripcion;

    private Boolean activo;

    @Column(name = "disponiblereserva")
    private Boolean disponibleReserva;

    // ==========================================
    // CONSTRUCTOR VACÍO (Requerido por JPA)
    // ==========================================
    public HabitacionView() {
    }

    // ==========================================
    // GETTERS (No ponemos Setters porque es @Immutable)
    // ==========================================
    public Integer getIdHabitacion() { return idHabitacion; }
    public String getCodigo() { return codigo; }
    public String getNumero() { return numero; }
    public String getNombre() { return nombre; }
    public String getTipoHabitacionCodigo() { return tipoHabitacionCodigo; }
    public String getTipoHabitacion() { return tipoHabitacion; }
    public Integer getCapacidadAdultos() { return capacidadAdultos; }
    public Integer getCapacidadNinos() { return capacidadNinos; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public String getEstadoCodigo() { return estadoCodigo; }
    public String getEstado() { return estado; }
    public String getEstadoColor() { return estadoColor; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public BigDecimal getPrecioDia() { return precioDia; }
    public String getDescripcion() { return descripcion; }
    public Boolean getActivo() { return activo; }
    public Boolean getDisponibleReserva() { return disponibleReserva; }
}
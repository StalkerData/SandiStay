package com.sandistay.backend.modules.rooms.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "habitacion")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idhabitacion")
    private Integer idHabitacion;

    @Column(unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(length = 100)
    private String nombre;

    @Column(name = "idtipohabitacion", nullable = false)
    private Integer idTipoHabitacion;

    @Column(name = "capacidadadultos", nullable = false)
    private Integer capacidadAdultos;

    @Column(name = "capacidadninos")
    private Integer capacidadNinos = 0;

    @Column(name = "capacidadmaxima", nullable = false)
    private Integer capacidadMaxima;

    @Column(name = "idestadolugar", nullable = false)
    private Integer idEstadoLugar;

    @Column(name = "precionoche", nullable = false)
    private BigDecimal precioNoche;

    @Column(name = "preciodia", nullable = false)
    private BigDecimal precioDia;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Boolean activo = true;

    // Relación para traer las imágenes reales de la base de datos
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "idhabitacion", referencedColumnName = "idhabitacion", insertable = false, updatable = false)
    private List<ImagenHabitacion> imagenes;

    public Habitacion() {}

    public Integer getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(Integer idHabitacion) { this.idHabitacion = idHabitacion; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getIdTipoHabitacion() { return idTipoHabitacion; }
    public void setIdTipoHabitacion(Integer idTipoHabitacion) { this.idTipoHabitacion = idTipoHabitacion; }
    public Integer getCapacidadAdultos() { return capacidadAdultos; }
    public void setCapacidadAdultos(Integer capacidadAdultos) { this.capacidadAdultos = capacidadAdultos; }
    public Integer getCapacidadNinos() { return capacidadNinos; }
    public void setCapacidadNinos(Integer capacidadNinos) { this.capacidadNinos = capacidadNinos; }
    public Integer getCapacidadMaxima() { return capacidadMaxima; }
    public void setCapacidadMaxima(Integer capacidadMaxima) { this.capacidadMaxima = capacidadMaxima; }
    public Integer getIdEstadoLugar() { return idEstadoLugar; }
    public void setIdEstadoLugar(Integer idEstadoLugar) { this.idEstadoLugar = idEstadoLugar; }
    public BigDecimal getPrecioNoche() { return precioNoche; }
    public void setPrecioNoche(BigDecimal precioNoche) { this.precioNoche = precioNoche; }
    public BigDecimal getPrecioDia() { return precioDia; }
    public void setPrecioDia(BigDecimal precioDia) { this.precioDia = precioDia; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public List<ImagenHabitacion> getImagenes() { return imagenes; }
    public void setImagenes(List<ImagenHabitacion> imagenes) { this.imagenes = imagenes; }
}
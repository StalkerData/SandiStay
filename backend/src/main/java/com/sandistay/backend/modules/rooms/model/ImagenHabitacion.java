package com.sandistay.backend.modules.rooms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "imagenhabitacion")
public class ImagenHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idimagen")
    private Integer idImagen;

    @Column(name = "idhabitacion", nullable = false)
    private Integer idHabitacion;

    @Column(nullable = false, length = 500)
    private String url;

    private Boolean activo = true;

    public ImagenHabitacion() {}
    
    // ==========================================
    // GENERAR GETTERS Y SETTERS 
    // ==========================================

	public Integer getIdImagen() {
		return idImagen;
	}

	public void setIdImagen(Integer idImagen) {
		this.idImagen = idImagen;
	}

	public Integer getIdHabitacion() {
		return idHabitacion;
	}

	public void setIdHabitacion(Integer idHabitacion) {
		this.idHabitacion = idHabitacion;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}


    
}
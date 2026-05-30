package com.sandistay.backend.modules.rooms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "imagenarea")
public class ImagenArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idimagen")
    private Integer idImagen;

    @Column(name = "idarea", nullable = false)
    private Integer idArea;

    @Column(nullable = false, length = 500)
    private String url;

    private Boolean activo = true;

    public ImagenArea() {}
    
    // ==========================================
    // GENERAR GETTERS Y SETTERS 
    // ==========================================

	public Integer getIdImagen() {
		return idImagen;
	}

	public void setIdImagen(Integer idImagen) {
		this.idImagen = idImagen;
	}

	public Integer getIdArea() {
		return idArea;
	}

	public void setIdArea(Integer idArea) {
		this.idArea = idArea;
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
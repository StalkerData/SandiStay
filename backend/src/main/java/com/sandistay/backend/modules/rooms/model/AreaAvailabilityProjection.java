package com.sandistay.backend.modules.rooms.model;

import java.math.BigDecimal;

/**
 * Proyección JPA para mapear el resultado de la función SQL
 * 'fn_buscar_areas_disponibles'.
 */
public interface AreaAvailabilityProjection {
	Integer getIdArea();

	String getCodigo();

	String getNombre();

	String getTipoArea();

	Integer getCapacidadPersonas();

	BigDecimal getPrecioHora();

	String getDescripcion();
}

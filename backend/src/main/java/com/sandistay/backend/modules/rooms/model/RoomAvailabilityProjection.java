package com.sandistay.backend.modules.rooms.model;

import java.math.BigDecimal;

/**
 * Proyección JPA para mapear el resultado de la función SQL
 * 'fn_buscar_habitaciones_disponibles'. Los nombres de los métodos (getters)
 * deben coincidir exactamente con los alias de las columnas en SQL.
 */
public interface RoomAvailabilityProjection {
	Integer getIdHabitacion();

	String getCodigo();

	String getNumero();

	String getNombre();

	String getTipoHabitacion();

	Integer getCapacidadAdultos();

	Integer getCapacidadNinos();

	BigDecimal getPrecioNoche();

	BigDecimal getPrecioDia();

	String getDescripcion();
}
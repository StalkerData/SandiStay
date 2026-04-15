package com.sandistay.backend.modules.rooms.dto;

import java.math.BigDecimal;

/**
 * DTO inmutable que representa una Habitación en el catálogo público.
 * Contiene solo la información necesaria para que SvelteKit renderice la tarjeta de la habitación.
 */
public record CatalogRoomResponse(
        Integer id,
        String codigo,
        String numero,
        String nombre,
        String tipo,
        Integer capacidadAdultos,
        Integer capacidadNinos,
        BigDecimal precioNoche,
        BigDecimal precioDia,
        String descripcion,
        String imagenPrincipal // URL de la imagen para mostrar en el frontend
) {}
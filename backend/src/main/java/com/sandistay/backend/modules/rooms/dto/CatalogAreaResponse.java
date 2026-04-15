package com.sandistay.backend.modules.rooms.dto;

import java.math.BigDecimal;

/**
 * DTO inmutable que representa un Área Social en el catálogo público.
 */
public record CatalogAreaResponse(
        Integer id,
        String codigo,
        String nombre,
        String tipo,
        Integer capacidadPersonas,
        BigDecimal precioHora,
        String descripcion,
        String imagenPrincipal // URL de la imagen para mostrar en el frontend
) {}
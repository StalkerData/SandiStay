package com.sandistay.backend.modules.rooms.controller;

import com.sandistay.backend.modules.rooms.dto.CatalogAreaResponse;
import com.sandistay.backend.modules.rooms.dto.CatalogRoomResponse;
import com.sandistay.backend.modules.rooms.service.CatalogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * Endpoint público para buscar habitaciones disponibles.
     * Todos los parámetros son opcionales (required = false) para permitir búsquedas generales.
     * Spring Boot convierte automáticamente los strings de la URL (ej: "2026-05-01") a LocalDate.
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<CatalogRoomResponse>> searchRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer adultos,
            @RequestParam(required = false) Integer ninos,
            @RequestParam(required = false) Integer tipoHabitacionId) {

        // Si no envían fechas, por defecto buscamos disponibilidad para hoy y mañana
        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now().plusDays(1);

        List<CatalogRoomResponse> rooms = catalogService.searchAvailableRooms(
                fechaInicio, fechaFin, adultos, ninos, tipoHabitacionId
        );

        return ResponseEntity.ok(rooms);
    }

    /**
     * Endpoint público para buscar áreas sociales disponibles.
     */
    @GetMapping("/areas")
    public ResponseEntity<List<CatalogAreaResponse>> searchAreas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Integer tipoAreaId) {

        // Si no envían fecha, por defecto buscamos para hoy
        if (fechaEvento == null) fechaEvento = LocalDate.now();
        // Si no envían horas, por defecto buscamos todo el día (08:00 a 22:00)
        if (horaInicio == null) horaInicio = LocalTime.of(8, 0);
        if (horaFin == null) horaFin = LocalTime.of(22, 0);

        List<CatalogAreaResponse> areas = catalogService.searchAvailableAreas(
                fechaEvento, horaInicio, horaFin, capacidadMinima, tipoAreaId
        );

        return ResponseEntity.ok(areas);
    }

    /**
     * Endpoint público para ver el detalle de una habitación específica.
     * (Nota: La implementación completa de este método requeriría un método en el servicio 
     * que consulte la vista HabitacionView por ID. Por ahora devolvemos un 501 Not Implemented 
     * para mantener el foco en la búsqueda principal).
     */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<CatalogRoomResponse> getRoomDetails(@PathVariable Integer id) {
        // TODO: Implementar la consulta individual usando HabitacionView
        return ResponseEntity.status(501).build();
    }
}
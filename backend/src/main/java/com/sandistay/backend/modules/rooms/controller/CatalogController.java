package com.sandistay.backend.modules.rooms.controller;

import com.sandistay.backend.modules.rooms.dto.CatalogAreaResponse;
import com.sandistay.backend.modules.rooms.dto.CatalogRoomResponse;
import com.sandistay.backend.modules.rooms.model.Area;
import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.model.ImagenArea;
import com.sandistay.backend.modules.rooms.service.CatalogService;
import com.sandistay.backend.modules.rooms.model.ImagenHabitacion;
import com.sandistay.backend.modules.rooms.repository.HabitacionRepository;
import com.sandistay.backend.modules.rooms.repository.AreaRepository;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final HabitacionRepository habitacionRepository;
    private final AreaRepository areaRepository;

    public CatalogController(CatalogService catalogService, HabitacionRepository habitacionRepository, AreaRepository areaRepository) {
        this.catalogService = catalogService;
        this.habitacionRepository=habitacionRepository;
        this.areaRepository = areaRepository;
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
     */
    @GetMapping("/rooms/{id}")
    public ResponseEntity<CatalogRoomResponse> getRoomDetails(@PathVariable Integer id) {
        CatalogRoomResponse room = catalogService.getRoomDetails(id);
        return ResponseEntity.ok(room);
    }
    // =================================================================
    // ENDPOINTS ADMINISTRATIVOS (Requieren Rol ADMIN o STAFF)
    // =================================================================

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habitacion> createRoom(@RequestBody Habitacion habitacion) {
        Habitacion nuevaHabitacion = catalogService.createRoom(habitacion);
        return ResponseEntity.status(201).body(nuevaHabitacion);
    }

    @PutMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habitacion> updateRoom(@PathVariable Integer id, @RequestBody Habitacion habitacion) {
        Habitacion habitacionActualizada = catalogService.updateRoom(id, habitacion);
        return ResponseEntity.ok(habitacionActualizada);
    }

    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteRoom(@PathVariable Integer id) {
        catalogService.deleteRoomLogical(id);
        return ResponseEntity.ok("Habitación eliminada lógicamente con éxito.");
    }

    @PatchMapping("/rooms/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<String> updateRoomStatus(@PathVariable Integer id, @RequestParam Integer idEstadoLugar) {
        catalogService.updateRoomStatus(id, idEstadoLugar);
        return ResponseEntity.ok("Estado de la habitación actualizado con éxito.");
    }
    
    /**
     * Sube una imagen para una habitación específica.
     * Consume MULTIPART_FORM_DATA porque recibe un archivo binario, no un JSON.
     */
    @PostMapping(value = "/rooms/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication") 
    public ResponseEntity<ImagenHabitacion> uploadRoomImage(
            @PathVariable Integer id, 
            @RequestParam("file") MultipartFile file) {
        
        ImagenHabitacion imagenGuardada = catalogService.uploadRoomImage(id, file);
        return ResponseEntity.status(201).body(imagenGuardada);
    }
    
    /**
     * Elimina una imagen específica de una habitación (Física y Lógicamente).
     */
    @DeleteMapping("/rooms/images/{idImagen}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteRoomImage(@PathVariable Integer idImagen) {
        catalogService.deleteRoomImage(idImagen);
        return ResponseEntity.ok("Imagen eliminada exitosamente del servidor y la base de datos.");
    }
    
    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Area> createArea(@RequestBody Area area) {
        return ResponseEntity.status(201).body(catalogService.createArea(area));
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Area> updateArea(@PathVariable Integer id, @RequestBody Area area) {
        return ResponseEntity.ok(catalogService.updateArea(id, area));
    }

    @DeleteMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteArea(@PathVariable Integer id) {
        catalogService.deleteAreaLogical(id);
        return ResponseEntity.ok("Área eliminada lógicamente con éxito.");
    }

    @PatchMapping("/areas/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> updateAreaStatus(@PathVariable Integer id, @RequestParam Integer idEstadoLugar) {
        catalogService.updateAreaStatus(id, idEstadoLugar);
        return ResponseEntity.ok("Estado del área actualizado con éxito.");
    }

    @PostMapping(value = "/areas/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ImagenArea> uploadAreaImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(catalogService.uploadAreaImage(id, file));
    }

    @DeleteMapping("/areas/images/{idImagen}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<String> deleteAreaImage(@PathVariable Integer idImagen) {
        catalogService.deleteAreaImage(idImagen);
        return ResponseEntity.ok("Imagen de área eliminada exitosamente.");
    }
    
    /**
     * Obtiene TODO el inventario de habitaciones (sin filtros de disponibilidad).
     * Exclusivo para el panel de administración.
     */
    @GetMapping("/admin/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Habitacion>> getAllRoomsAdmin() {
        // Traemos todas las habitaciones que no estén eliminadas lógicamente
        List<Habitacion> rooms = habitacionRepository.findAll().stream()
                .filter(Habitacion::getActivo)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Obtiene TODO el inventario de áreas.
     */
    @GetMapping("/admin/areas")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Area>> getAllAreasAdmin() {
        List<Area> areas = areaRepository.findAll().stream()
                .filter(Area::getActivo)
                .toList();
        return ResponseEntity.ok(areas);
    }
}
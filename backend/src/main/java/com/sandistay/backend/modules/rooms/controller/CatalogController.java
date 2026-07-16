package com.sandistay.backend.modules.rooms.controller;

import com.sandistay.backend.modules.rooms.dto.CatalogAreaResponse;
import com.sandistay.backend.modules.rooms.dto.CatalogRoomResponse;
import com.sandistay.backend.modules.rooms.model.Area;
import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.model.ImagenArea;
import com.sandistay.backend.modules.rooms.service.CatalogService;
import com.sandistay.backend.modules.rooms.model.ImagenHabitacion;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador del Catálogo.
 * Sigue el principio de "Thin Controller", delegando toda la lógica al CatalogService.
 */
@RestController
@RequestMapping("/catalog")
@SecurityRequirement(name = "Bearer Authentication") // Aplica a toda la clase para Swagger
public class CatalogController {

    private final CatalogService catalogService;

    // Constructor manual explícito (Sin Lombok)
    // Se eliminaron los repositorios para respetar la arquitectura de capas
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // =================================================================
    // ENDPOINTS PÚBLICOS
    // =================================================================

    @GetMapping("/rooms")
    public ResponseEntity<List<CatalogRoomResponse>> searchRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Integer adultos,
            @RequestParam(required = false) Integer ninos,
            @RequestParam(required = false) Integer tipoHabitacionId) {

        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now().plusDays(1);

        return ResponseEntity.ok(catalogService.searchAvailableRooms(fechaInicio, fechaFin, adultos, ninos, tipoHabitacionId));
    }

    @GetMapping("/areas")
    public ResponseEntity<List<CatalogAreaResponse>> searchAreas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEvento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @RequestParam(required = false) Integer capacidadMinima,
            @RequestParam(required = false) Integer tipoAreaId) {

        if (fechaEvento == null) fechaEvento = LocalDate.now();
        if (horaInicio == null) horaInicio = LocalTime.of(8, 0);
        if (horaFin == null) horaFin = LocalTime.of(22, 0);

        return ResponseEntity.ok(catalogService.searchAvailableAreas(fechaEvento, horaInicio, horaFin, capacidadMinima, tipoAreaId));
    }

    @GetMapping("/rooms/{id}")
    public ResponseEntity<CatalogRoomResponse> getRoomDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogService.getRoomDetails(id));
    }

    @GetMapping("/discount")
    public ResponseEntity<Map<String, BigDecimal>> getDiscount(@RequestParam String documento) {
        return ResponseEntity.ok(Map.of("descuento", catalogService.getClientDiscount(documento)));
    }

    // =================================================================
    // ENDPOINTS ADMINISTRATIVOS (Requieren Rol ADMIN o STAFF)
    // =================================================================

    @PostMapping("/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habitacion> createRoom(@RequestBody Habitacion habitacion) {
        return ResponseEntity.status(201).body(catalogService.createRoom(habitacion));
    }

    @PutMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Habitacion> updateRoom(@PathVariable Integer id, @RequestBody Habitacion habitacion) {
        return ResponseEntity.ok(catalogService.updateRoom(id, habitacion));
    }

    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRoom(@PathVariable Integer id) {
        catalogService.deleteRoomLogical(id);
        return ResponseEntity.ok(Map.of("message", "Habitación eliminada lógicamente con éxito."));
    }

    @PatchMapping("/rooms/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, String>> updateRoomStatus(@PathVariable Integer id, @RequestParam Integer idEstadoLugar) {
        catalogService.updateRoomStatus(id, idEstadoLugar);
        return ResponseEntity.ok(Map.of("message", "Estado de la habitación actualizado con éxito."));
    }

    @PostMapping(value = "/rooms/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImagenHabitacion> uploadRoomImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(catalogService.uploadRoomImage(id, file));
    }

    @DeleteMapping("/rooms/images/{idImagen}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRoomImage(@PathVariable Integer idImagen) {
        catalogService.deleteRoomImage(idImagen);
        return ResponseEntity.ok(Map.of("message", "Imagen eliminada exitosamente del servidor y la base de datos."));
    }

    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Area> createArea(@RequestBody Area area) {
        return ResponseEntity.status(201).body(catalogService.createArea(area));
    }

    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Area> updateArea(@PathVariable Integer id, @RequestBody Area area) {
        return ResponseEntity.ok(catalogService.updateArea(id, area));
    }

    @DeleteMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteArea(@PathVariable Integer id) {
        catalogService.deleteAreaLogical(id);
        return ResponseEntity.ok(Map.of("message", "Área eliminada lógicamente con éxito."));
    }

    @PatchMapping("/areas/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<Map<String, String>> updateAreaStatus(@PathVariable Integer id, @RequestParam Integer idEstadoLugar) {
        catalogService.updateAreaStatus(id, idEstadoLugar);
        return ResponseEntity.ok(Map.of("message", "Estado del área actualizado con éxito."));
    }

    @PostMapping(value = "/areas/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ImagenArea> uploadAreaImage(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(catalogService.uploadAreaImage(id, file));
    }

    @DeleteMapping("/areas/images/{idImagen}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAreaImage(@PathVariable Integer idImagen) {
        catalogService.deleteAreaImage(idImagen);
        return ResponseEntity.ok(Map.of("message", "Imagen de área eliminada exitosamente."));
    }

    @GetMapping("/admin/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<Habitacion>> getAllRoomsAdmin() {
        return ResponseEntity.ok(catalogService.getAllRoomsAdmin());
    }

    @GetMapping("/admin/areas")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<Area>> getAllAreasAdmin() {
        return ResponseEntity.ok(catalogService.getAllAreasAdmin());
    }
}

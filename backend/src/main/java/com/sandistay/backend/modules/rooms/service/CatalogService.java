package com.sandistay.backend.modules.rooms.service;

import com.sandistay.backend.modules.rooms.dto.CatalogAreaResponse;
import com.sandistay.backend.modules.rooms.dto.CatalogRoomResponse;
import com.sandistay.backend.modules.rooms.model.AreaAvailabilityProjection;
import com.sandistay.backend.modules.rooms.model.RoomAvailabilityProjection;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    /**
     * Busca habitaciones disponibles delegando la lógica pesada a la función de PostgreSQL.
     * Convierte las proyecciones SQL en DTOs inmutables para el Frontend.
     */
    public List<CatalogRoomResponse> searchAvailableRooms(
            LocalDate fechaInicio, 
            LocalDate fechaFin, 
            Integer adultos, 
            Integer ninos, 
            Integer tipoHabitacionId) {

        // 1. Llamamos a la función nativa de PostgreSQL
        List<RoomAvailabilityProjection> projections = catalogRepository.findAvailableRooms(
                fechaInicio, fechaFin, adultos, ninos, tipoHabitacionId
        );

        // 2. Mapeamos el resultado al DTO
        return projections.stream().map(proj -> {
            // Construimos la ruta de la imagen principal basada en nuestra convención de carpetas
            String imagenPrincipal = "/uploads/Habitaciones/" + proj.getCodigo() + "/" + proj.getCodigo() + "_1.jpg";

            return new CatalogRoomResponse(
                    proj.getIdHabitacion(),
                    proj.getCodigo(),
                    proj.getNumero(),
                    proj.getNombre(),
                    proj.getTipoHabitacion(),
                    proj.getCapacidadAdultos(),
                    proj.getCapacidadNinos(),
                    proj.getPrecioNoche(),
                    proj.getPrecioDia(),
                    proj.getDescripcion(),
                    imagenPrincipal
            );
        }).collect(Collectors.toList());
    }

    /**
     * Busca áreas sociales disponibles delegando la lógica a PostgreSQL.
     */
    public List<CatalogAreaResponse> searchAvailableAreas(
            LocalDate fechaEvento, 
            LocalTime horaInicio, 
            LocalTime horaFin, 
            Integer capacidadMinima, 
            Integer tipoAreaId) {

        // 1. Llamamos a la función nativa de PostgreSQL
        List<AreaAvailabilityProjection> projections = catalogRepository.findAvailableAreas(
                fechaEvento, horaInicio, horaFin, capacidadMinima, tipoAreaId
        );

        // 2. Mapeamos el resultado al DTO
        return projections.stream().map(proj -> {
            // Construimos la ruta de la imagen principal
            String imagenPrincipal = "/uploads/Areas/" + proj.getCodigo() + "/" + proj.getCodigo() + "_1.jpg";

            return new CatalogAreaResponse(
                    proj.getIdArea(),
                    proj.getCodigo(),
                    proj.getNombre(),
                    proj.getTipoArea(),
                    proj.getCapacidadPersonas(),
                    proj.getPrecioHora(),
                    proj.getDescripcion(),
                    imagenPrincipal
            );
        }).collect(Collectors.toList());
    }
}
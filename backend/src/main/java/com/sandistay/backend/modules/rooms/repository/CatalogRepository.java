package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.modules.rooms.model.AreaAvailabilityProjection;
import com.sandistay.backend.modules.rooms.model.HabitacionView;
import com.sandistay.backend.modules.rooms.model.RoomAvailabilityProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de solo lectura para el Catálogo.
 * Extiende de la interfaz base 'Repository' (no JpaRepository) porque no vamos a guardar ni borrar nada aquí,
 * solo ejecutaremos consultas nativas hacia nuestras funciones de PostgreSQL.
 */
@org.springframework.stereotype.Repository
public interface CatalogRepository extends Repository<HabitacionView, Integer> {

    /**
     * Llama a la función SQL para buscar habitaciones disponibles.
     * Se usa CAST para evitar que Hibernate envíe tipos de datos incorrectos cuando los filtros opcionales son NULL.
     */
    @Query(value = "SELECT * FROM fn_buscar_habitaciones_disponibles(" +
            "CAST(:fechaInicio AS DATE), " +
            "CAST(:fechaFin AS DATE), " +
            "CAST(:adultos AS INTEGER), " +
            "CAST(:ninos AS INTEGER), " +
            "CAST(:tipo AS INTEGER))", 
            nativeQuery = true)
    List<RoomAvailabilityProjection> findAvailableRooms(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("adultos") Integer adultos,
            @Param("ninos") Integer ninos,
            @Param("tipo") Integer tipo
    );

    /**
     * Llama a la función SQL para buscar áreas sociales disponibles.
     */
    @Query(value = "SELECT * FROM fn_buscar_areas_disponibles(" +
            "CAST(:fechaEvento AS DATE), " +
            "CAST(:horaInicio AS TIME), " +
            "CAST(:horaFin AS TIME), " +
            "CAST(:capacidad AS INTEGER), " +
            "CAST(:tipo AS INTEGER))", 
            nativeQuery = true)
    List<AreaAvailabilityProjection> findAvailableAreas(
            @Param("fechaEvento") LocalDate fechaEvento,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("capacidad") Integer capacidad,
            @Param("tipo") Integer tipo
    );
    
    /**
     * Busca el detalle de una habitación específica usando la vista SQL.
     */
    Optional<HabitacionView> findById(Integer id);

}
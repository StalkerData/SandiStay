package com.sandistay.backend.modules.rooms.service;

import com.sandistay.backend.modules.rooms.dto.CatalogAreaResponse;
import com.sandistay.backend.modules.rooms.dto.CatalogRoomResponse;
import com.sandistay.backend.modules.rooms.model.Area;
import com.sandistay.backend.modules.rooms.model.AreaAvailabilityProjection;
import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.model.HabitacionView;
import com.sandistay.backend.modules.rooms.model.ImagenArea;
import com.sandistay.backend.modules.rooms.model.RoomAvailabilityProjection;
import com.sandistay.backend.modules.rooms.repository.AreaRepository;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import com.sandistay.backend.modules.rooms.repository.HabitacionRepository;
import com.sandistay.backend.modules.rooms.repository.ImagenAreaRepository;
import com.sandistay.backend.modules.rooms.repository.ImagenHabitacionRepository;
import com.sandistay.backend.modules.rooms.model.ImagenHabitacion;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class CatalogService {
	
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogRepository catalogRepository;
    private final HabitacionRepository habitacionRepository; 
    private final ImagenHabitacionRepository imagenHabitacionRepository;
    private final AreaRepository areaRepository;
    private final ImagenAreaRepository imagenAreaRepository;

    public CatalogService(CatalogRepository catalogRepository, 
    		HabitacionRepository habitacionRepository,
    		ImagenHabitacionRepository imagenHabitacionRepository,
    		AreaRepository areaRepository,
    		ImagenAreaRepository imagenAreaRepository) {
        this.catalogRepository = catalogRepository;
        this.habitacionRepository = habitacionRepository;
        this.imagenHabitacionRepository = imagenHabitacionRepository;
        this.areaRepository = areaRepository;
        this.imagenAreaRepository = imagenAreaRepository;
    }

    /**
     * Busca habitaciones disponibles delegando la lógica pesada a la función de PostgreSQL.
     * Convierte las proyecciones SQL en DTOs inmutables para el Frontend.
     */
    public List<CatalogRoomResponse> searchAvailableRooms(
            LocalDate fechaInicio, LocalDate fechaFin, Integer adultos, Integer ninos, Integer tipoHabitacionId) {

        List<RoomAvailabilityProjection> projections = catalogRepository.findAvailableRooms(
                fechaInicio, fechaFin, adultos, ninos, tipoHabitacionId
        );

        return projections.stream().map(proj -> {
            // BUSCAMOS LA IMAGEN REAL EN LA BASE DE DATOS
            String imagenPrincipal = imagenHabitacionRepository.findByIdHabitacion(proj.getIdHabitacion())
                    .stream().findFirst().map(ImagenHabitacion::getUrl).orElse(null);

            return new CatalogRoomResponse(
                    proj.getIdHabitacion(), proj.getCodigo(), proj.getNumero(), proj.getNombre(),
                    proj.getTipoHabitacion(), proj.getCapacidadAdultos(), proj.getCapacidadNinos(),
                    proj.getPrecioNoche(), proj.getPrecioDia(), proj.getDescripcion(), imagenPrincipal
            );
        }).collect(Collectors.toList());
    }

    /**
     * Busca áreas sociales disponibles delegando la lógica a PostgreSQL.
     */
    public List<CatalogAreaResponse> searchAvailableAreas(
            LocalDate fechaEvento, LocalTime horaInicio, LocalTime horaFin, Integer capacidadMinima, Integer tipoAreaId) {

        List<AreaAvailabilityProjection> projections = catalogRepository.findAvailableAreas(
                fechaEvento, horaInicio, horaFin, capacidadMinima, tipoAreaId
        );

        return projections.stream().map(proj -> {
            // BUSCAMOS LA IMAGEN REAL EN LA BASE DE DATOS
            String imagenPrincipal = imagenAreaRepository.findByIdArea(proj.getIdArea())
                    .stream().findFirst().map(ImagenArea::getUrl).orElse(null);

            return new CatalogAreaResponse(
                    proj.getIdArea(), proj.getCodigo(), proj.getNombre(), proj.getTipoArea(),
                    proj.getCapacidadPersonas(), proj.getPrecioHora(), proj.getDescripcion(), imagenPrincipal
            );
        }).collect(Collectors.toList());
    }
    
    
    /**
     * Obtiene el detalle de una habitación específica.
     */

    public CatalogRoomResponse getRoomDetails(Integer id) {
        HabitacionView view = catalogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada en el catálogo."));

        // BUSCAMOS LA IMAGEN REAL EN LA BASE DE DATOS
        String imagenPrincipal = imagenHabitacionRepository.findByIdHabitacion(view.getIdHabitacion())
                .stream().findFirst().map(ImagenHabitacion::getUrl).orElse(null);

        return new CatalogRoomResponse(
                view.getIdHabitacion(), view.getCodigo(), view.getNumero(), view.getNombre(),
                view.getTipoHabitacion(), view.getCapacidadAdultos(), view.getCapacidadNinos(),
                view.getPrecioNoche(), view.getPrecioDia(), view.getDescripcion(), imagenPrincipal
        );
    }
   
    
    // =================================================================
    // MÉTODOS ADMINISTRATIVOS (CRUD HABITACIONES)
    // =================================================================
    
    @Transactional
    public Habitacion createRoom(Habitacion habitacion) {
        habitacion.setActivo(true);
        // Por defecto, nace Disponible (ID 1 según seed_data.sql)
        if (habitacion.getIdEstadoLugar() == null) {
            habitacion.setIdEstadoLugar(1); 
        }
        return habitacionRepository.save(habitacion);
    }

    @Transactional
    public Habitacion updateRoom(Integer id, Habitacion habitacionActualizada) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // Actualizamos los campos permitidos
        habitacion.setCodigo(habitacionActualizada.getCodigo());
        habitacion.setNumero(habitacionActualizada.getNumero());
        habitacion.setNombre(habitacionActualizada.getNombre());
        habitacion.setIdTipoHabitacion(habitacionActualizada.getIdTipoHabitacion());
        habitacion.setCapacidadAdultos(habitacionActualizada.getCapacidadAdultos());
        habitacion.setCapacidadNinos(habitacionActualizada.getCapacidadNinos());
        habitacion.setCapacidadMaxima(habitacionActualizada.getCapacidadMaxima());
        habitacion.setPrecioNoche(habitacionActualizada.getPrecioNoche());
        habitacion.setPrecioDia(habitacionActualizada.getPrecioDia());
        habitacion.setDescripcion(habitacionActualizada.getDescripcion());

        return habitacionRepository.save(habitacion);
    }

    @Transactional
    public void deleteRoomLogical(Integer id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
        
        // Borrado lógico: No hacemos DELETE, solo la ocultamos
        habitacion.setActivo(false);
        habitacionRepository.save(habitacion);
    }

    @Transactional
    public void updateRoomStatus(Integer id, Integer idEstadoLugar) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
        
        habitacion.setIdEstadoLugar(idEstadoLugar);
        habitacionRepository.save(habitacion);
    }
    
    /**
     * Sube una imagen física al servidor, crea los directorios necesarios 
     * y guarda la referencia en la base de datos.
     */
    @Transactional
    public ImagenHabitacion uploadRoomImage(Integer idHabitacion, MultipartFile file) {
        // 1. Validar que la habitación exista
        Habitacion habitacion = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        try {
            // 2. Definir la ruta física: uploads/Habitaciones/{codigo}/
            String folderPath = "uploads/Habitaciones/" + habitacion.getCodigo();
            Path uploadPath = Paths.get(folderPath);

            // 3. Crear los directorios si no existen
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. Generar un nombre de archivo único (Ej: H101_1716900000.jpg)
            // Usamos el timestamp para evitar que dos fotos se sobrescriban si se llaman igual
            String fileName = habitacion.getCodigo() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            // 5. Guardar el archivo físico en el disco duro
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 6. Guardar la ruta en la base de datos
            ImagenHabitacion imagen = new ImagenHabitacion();
            imagen.setIdHabitacion(idHabitacion);
            // La URL que guardamos es la ruta web que SvelteKit consumirá
            imagen.setUrl("/" + folderPath + "/" + fileName);
            
            return imagenHabitacionRepository.save(imagen);

        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la imagen en el servidor: " + e.getMessage());
        }
    }
    
    /**
     * Elimina una imagen de la base de datos y borra el archivo físico del servidor.
     */
    @Transactional
    public void deleteRoomImage(Integer idImagen) {
        // 1. Buscar la imagen en la BD
        ImagenHabitacion imagen = imagenHabitacionRepository.findById(idImagen)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada"));

        // 2. Borrar el archivo físico del disco duro
        try {
            // La URL guardada es "/uploads/Habitaciones/H101/foto.jpg"
            // Le quitamos el primer "/" para que la ruta relativa funcione en Java
            String rutaRelativa = imagen.getUrl().substring(1);
            Path filePath = Paths.get(rutaRelativa);
            
            Files.deleteIfExists(filePath);
            log.info("Archivo físico eliminado: {}", filePath.toString());
        } catch (Exception e) {
            log.error("No se pudo eliminar el archivo físico: {}", e.getMessage());
            // No lanzamos excepción aquí para que al menos se borre de la BD
        }

        // 3. Borrar el registro de la base de datos (Hard Delete)
        imagenHabitacionRepository.delete(imagen);
    }
    
    // =================================================================
    // MÉTODOS ADMINISTRATIVOS (CRUD ÁREAS SOCIALES)
    // =================================================================

    @Transactional
    public Area createArea(Area area) {
        area.setActivo(true);
        if (area.getIdEstadoLugar() == null) area.setIdEstadoLugar(1); 
        return areaRepository.save(area);
    }

    @Transactional
    public Area updateArea(Integer id, Area areaActualizada) {
        Area area = areaRepository.findById(id).orElseThrow(() -> new RuntimeException("Área no encontrada"));
        area.setCodigo(areaActualizada.getCodigo());
        area.setNombre(areaActualizada.getNombre());
        area.setIdTipoArea(areaActualizada.getIdTipoArea());
        area.setCapacidadPersonas(areaActualizada.getCapacidadPersonas());
        area.setPrecioHora(areaActualizada.getPrecioHora());
        area.setDescripcion(areaActualizada.getDescripcion());
        return areaRepository.save(area);
    }

    @Transactional
    public void deleteAreaLogical(Integer id) {
        Area area = areaRepository.findById(id).orElseThrow(() -> new RuntimeException("Área no encontrada"));
        area.setActivo(false);
        areaRepository.save(area);
    }

    @Transactional
    public void updateAreaStatus(Integer id, Integer idEstadoLugar) {
        Area area = areaRepository.findById(id).orElseThrow(() -> new RuntimeException("Área no encontrada"));
        area.setIdEstadoLugar(idEstadoLugar);
        areaRepository.save(area);
    }

    @Transactional
    public ImagenArea uploadAreaImage(Integer idArea, MultipartFile file) {
        Area area = areaRepository.findById(idArea).orElseThrow(() -> new RuntimeException("Área no encontrada"));
        try {
            String folderPath = "uploads/Areas/" + area.getCodigo();
            Path uploadPath = Paths.get(folderPath);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String fileName = area.getCodigo() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            ImagenArea imagen = new ImagenArea();
            imagen.setIdArea(idArea);
            imagen.setUrl("/" + folderPath + "/" + fileName);
            return imagenAreaRepository.save(imagen);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteAreaImage(Integer idImagen) {
        ImagenArea imagen = imagenAreaRepository.findById(idImagen).orElseThrow(() -> new RuntimeException("Imagen no encontrada"));
        try {
            Files.deleteIfExists(Paths.get(imagen.getUrl().substring(1)));
            log.info("Archivo físico eliminado: {}", imagen.getUrl());
        } catch (Exception e) {
            log.error("No se pudo eliminar el archivo físico: {}", e.getMessage());
        }
        imagenAreaRepository.delete(imagen);
    }
    
}
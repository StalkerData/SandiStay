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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Servicio principal del Catálogo.
 * Maneja la disponibilidad pública y el CRUD administrativo del inventario.
 */
@Service
public class CatalogService {

    // Logger nativo (Sin Lombok)
    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final CatalogRepository catalogRepository;
    private final HabitacionRepository habitacionRepository;
    private final ImagenHabitacionRepository imagenHabitacionRepository;
    private final AreaRepository areaRepository;
    private final ImagenAreaRepository imagenAreaRepository;

    // Constructor manual explícito
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

    // =================================================================
    // MÉTODOS PÚBLICOS (BÚSQUEDA Y DISPONIBILIDAD)
    // =================================================================

    /**
     * Busca habitaciones disponibles delegando la lógica pesada a PostgreSQL.
     */
    @Transactional(readOnly = true)
    public List<CatalogRoomResponse> searchAvailableRooms(
            LocalDate fechaInicio, LocalDate fechaFin, Integer adultos, Integer ninos, Integer tipoHabitacionId) {

        List<RoomAvailabilityProjection> projections = catalogRepository.findAvailableRooms(
                fechaInicio, fechaFin, adultos, ninos, tipoHabitacionId
        );

        return projections.stream().map(proj -> {
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
    @Transactional(readOnly = true)
    public List<CatalogAreaResponse> searchAvailableAreas(
            LocalDate fechaEvento, LocalTime horaInicio, LocalTime horaFin, Integer capacidadMinima, Integer tipoAreaId) {

        List<AreaAvailabilityProjection> projections = catalogRepository.findAvailableAreas(
                fechaEvento, horaInicio, horaFin, capacidadMinima, tipoAreaId
        );

        return projections.stream().map(proj -> {
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
    @Transactional(readOnly = true)
    public CatalogRoomResponse getRoomDetails(Integer id) {
        HabitacionView view = catalogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada en el catálogo."));

        String imagenPrincipal = imagenHabitacionRepository.findByIdHabitacion(view.getIdHabitacion())
                .stream().findFirst().map(ImagenHabitacion::getUrl).orElse(null);

        return new CatalogRoomResponse(
                view.getIdHabitacion(), view.getCodigo(), view.getNumero(), view.getNombre(),
                view.getTipoHabitacion(), view.getCapacidadAdultos(), view.getCapacidadNinos(),
                view.getPrecioNoche(), view.getPrecioDia(), view.getDescripcion(), imagenPrincipal
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getClientDiscount(String documento) {
        BigDecimal descuento = catalogRepository.getDescuentoByDocumento(documento);
        return descuento != null ? descuento : BigDecimal.ZERO;
    }

    // =================================================================
    // MÉTODOS ADMINISTRATIVOS (CRUD HABITACIONES)
    // =================================================================

    @Transactional
    public Habitacion createRoom(Habitacion habitacion) {
        habitacion.setActivo(true);
        if (habitacion.getIdEstadoLugar() == null) {
            habitacion.setIdEstadoLugar(1); // 1 = Disponible
        }
        return habitacionRepository.save(habitacion);
    }

    @Transactional
    public Habitacion updateRoom(Integer id, Habitacion habitacionActualizada) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

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
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));
        habitacion.setActivo(false);
        habitacionRepository.save(habitacion);
    }

    @Transactional
    public void updateRoomStatus(Integer id, Integer idEstadoLugar) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));
        habitacion.setIdEstadoLugar(idEstadoLugar);
        habitacionRepository.save(habitacion);
    }

    @Transactional
    public ImagenHabitacion uploadRoomImage(Integer idHabitacion, MultipartFile file) {
        Habitacion habitacion = habitacionRepository.findById(idHabitacion)
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        try {
            String fileUrl = saveFileToDisk("Habitaciones", habitacion.getCodigo(), file);

            ImagenHabitacion imagen = new ImagenHabitacion();
            imagen.setIdHabitacion(idHabitacion);
            imagen.setUrl(fileUrl);

            return imagenHabitacionRepository.save(imagen);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al guardar la imagen en el servidor: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteRoomImage(Integer idImagen) {
        ImagenHabitacion imagen = imagenHabitacionRepository.findById(idImagen)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        deleteFileFromDisk(imagen.getUrl());
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
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));

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
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));
        area.setActivo(false);
        areaRepository.save(area);
    }

    @Transactional
    public void updateAreaStatus(Integer id, Integer idEstadoLugar) {
        Area area = areaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));
        area.setIdEstadoLugar(idEstadoLugar);
        areaRepository.save(area);
    }

    @Transactional
    public ImagenArea uploadAreaImage(Integer idArea, MultipartFile file) {
        Area area = areaRepository.findById(idArea)
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));

        try {
            String fileUrl = saveFileToDisk("Areas", area.getCodigo(), file);

            ImagenArea imagen = new ImagenArea();
            imagen.setIdArea(idArea);
            imagen.setUrl(fileUrl);

            return imagenAreaRepository.save(imagen);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteAreaImage(Integer idImagen) {
        ImagenArea imagen = imagenAreaRepository.findById(idImagen)
                .orElseThrow(() -> new IllegalArgumentException("Imagen no encontrada"));

        deleteFileFromDisk(imagen.getUrl());
        imagenAreaRepository.delete(imagen);
    }

    // =================================================================
    // MÉTODOS PRIVADOS DE UTILIDAD (CLEAN CODE & SECURITY)
    // =================================================================

    /**
     * Guarda un archivo físico en el disco duro previniendo ataques de Path Traversal.
     */
    private String saveFileToDisk(String folderType, String entityCode, MultipartFile file) throws IOException {
        String folderPath = "uploads/" + folderType + "/" + entityCode;
        Path uploadPath = Paths.get(folderPath);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Security by Design: Sanitizamos el nombre del archivo para evitar Path Traversal (Ej: ../../etc/passwd)
        String originalFilename = file.getOriginalFilename();
        String safeFilename = (originalFilename != null) ? Paths.get(originalFilename).getFileName().toString() : "image.jpg";

        String fileName = entityCode + "_" + System.currentTimeMillis() + "_" + safeFilename;
        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/" + folderPath + "/" + fileName;
    }

    /**
     * Elimina un archivo físico del disco duro de forma segura.
     */
    private void deleteFileFromDisk(String url) {
        try {
            if (url != null && url.startsWith("/")) {
                Path filePath = Paths.get(url.substring(1));
                Files.deleteIfExists(filePath);
                log.info("Archivo físico eliminado: {}", filePath.toString());
            }
        } catch (Exception e) {
            log.error("No se pudo eliminar el archivo físico: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Habitacion> getAllRoomsAdmin() {
        return habitacionRepository.findAll().stream()
                .filter(Habitacion::getActivo)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Area> getAllAreasAdmin() {
        return areaRepository.findAll().stream()
                .filter(Area::getActivo)
                .toList();
    }
}

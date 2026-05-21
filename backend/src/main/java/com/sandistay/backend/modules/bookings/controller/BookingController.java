package com.sandistay.backend.modules.bookings.controller;

import com.sandistay.backend.modules.bookings.dto.BookingDetailResponse;
import com.sandistay.backend.modules.bookings.dto.BookingResponse;
import com.sandistay.backend.modules.bookings.dto.CancelBookingRequest;
import com.sandistay.backend.modules.bookings.dto.CancelBookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.sandistay.backend.modules.bookings.model.ReservaHabitacionView;
import com.sandistay.backend.modules.bookings.model.ReservaView;

@RestController
@RequestMapping("/bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Endpoint para crear una nueva reserva (Habitación o Área).
     * 
     * @param request El payload JSON validado automáticamente por @Valid.
     * @param authentication Inyectado por Spring Security si el usuario está logueado.
     * @return BookingResponse con el código de reserva generado.
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {

        // 1. Determinar quién está creando la reserva
        // Si es un Guest Checkout (cliente público), authentication será null o anónimo.
        // Si es la Secretaria/Admin, extraeremos su ID de la base de datos.
        Integer idUsuarioActual = obtenerIdUsuario(authentication);

        // 2. Delegar la orquestación al servicio transaccional
        BookingResponse response = bookingService.createBooking(request, idUsuarioActual);

        // 3. Retornar 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =================================================================================
    // MÉTODOS PRIVADOS DE APOYO
    // =================================================================================

    /**
     * Extrae el ID del usuario que realiza la petición.
     * Si es un cliente público (Guest), asignamos un ID por defecto o null, 
     * dependiendo de cómo definamos la auditoría de reservas públicas.
     */
    private Integer obtenerIdUsuario(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser")) {
            
            // TODO: En una implementación completa, aquí buscaríamos el ID real del usuario 
            // en la base de datos usando authentication.getName() (el username).
            // Por ahora, simulamos que el Admin (ID 1) está creando la reserva.
            return 1; 
        }
        
        // Si es un Guest Checkout desde la web pública, asignamos el ID del usuario "Sistema" o "Web"
        // Asumimos que el ID 1 es el Admin/Sistema según nuestro seed_data.sql
        return 1; 
    }
    
    /**
     * Endpoint protegido para cancelar una reserva existente.
     * Aplica las políticas de penalidad y genera los registros de reembolso correspondientes.
     * 
     * @param id El ID interno de la reserva a cancelar.
     * @param request El payload JSON con el motivo de la cancelación.
     * @param authentication Inyectado por Spring Security (requiere estar logueado).
     * @return CancelBookingResponse con el desglose financiero de la operación.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable Integer id,
            @Valid @RequestBody CancelBookingRequest request,
            Authentication authentication) {

        // 1. Extraer el ID del usuario que ejecuta la cancelación (Staff)
        // A diferencia de la creación, la cancelación SIEMPRE requiere un usuario autenticado.
        Integer idUsuarioActual = obtenerIdUsuario(authentication);

        // 2. Delegar la lógica financiera y operativa al servicio
        CancelBookingResponse response = bookingService.cancelBooking(id, request, idUsuarioActual);

        // 3. Retornar 200 OK con el resumen de la operación
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint protegido para listar todas las reservas en el Dashboard.
     */
    @GetMapping
    public ResponseEntity<List<ReservaView>> getAllBookings() {
        List<ReservaView> reservas = bookingService.getAllBookings();
        return ResponseEntity.ok(reservas);
    }
    
    /**
     * Endpoint protegido para ver el detalle de las habitaciones de una reserva.
     */
    @GetMapping("/{id}/details/rooms")
    public ResponseEntity<List<ReservaHabitacionView>> getBookingRoomDetails(@PathVariable Integer id) {
        List<ReservaHabitacionView> detalles = bookingService.getDetallesHabitacion(id);
        return ResponseEntity.ok(detalles);
    }
    
    
    /**
     * Endpoint protegido para realizar el Check-out de una reserva.
     * 
     * @param id El ID interno de la reserva.
     * @param authentication Inyectado por Spring Security (Usuario Staff).
     */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<BookingResponse> checkoutBooking(
            @PathVariable Integer id,
            Authentication authentication) {

        // Extraemos el ID del usuario que ejecuta la acción (Staff)
        Integer idUsuarioActual = obtenerIdUsuario(authentication);

        // Delegamos al servicio
        BookingResponse response = bookingService.checkout(id, idUsuarioActual);

        // Retornamos 200 OK
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint protegido para ver el detalle completo de una reserva (Cabecera, Ítems y Pagos).
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingFullDetails(@PathVariable Integer id) {
        BookingDetailResponse response = bookingService.getBookingFullDetails(id);
        return ResponseEntity.ok(response);
    }
}
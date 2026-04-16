package com.sandistay.backend.modules.bookings.controller;

import com.sandistay.backend.modules.bookings.dto.BookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
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
}
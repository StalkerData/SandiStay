package com.sandistay.backend.modules.bookings.controller;

import com.sandistay.backend.modules.auth.model.Usuario;
import com.sandistay.backend.modules.auth.repository.UsuarioRepository;
import com.sandistay.backend.modules.bookings.dto.BookingDetailResponse;
import com.sandistay.backend.modules.bookings.dto.BookingResponse;
import com.sandistay.backend.modules.bookings.dto.CancelBookingRequest;
import com.sandistay.backend.modules.bookings.dto.CancelBookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.dto.ReservaTotalResponse;
import com.sandistay.backend.modules.bookings.model.ReservaHabitacionView;
import com.sandistay.backend.modules.bookings.model.ReservaView;
import com.sandistay.backend.modules.bookings.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Reservas.
 * Maneja tanto el Guest Checkout (Público) como la gestión operativa (Privado).
 */
@RestController
@RequestMapping("/bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;
    private final UsuarioRepository usuarioRepository;

    // Constructor manual explícito (Sin Lombok)
    public BookingController(BookingService bookingService, UsuarioRepository usuarioRepository) {
        this.bookingService = bookingService;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Endpoint mixto: Permite crear reservas desde la web pública (Guest)
     * o desde el panel administrativo (Walk-in).
     */
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {

        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        BookingResponse response = bookingService.createBooking(request, idUsuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ReservaView>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookingDetailResponse> getBookingFullDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getBookingFullDetails(id));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable Integer id,
            @Valid @RequestBody CancelBookingRequest request,
            Authentication authentication) {

        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        CancelBookingResponse response = bookingService.cancelBooking(id, request, idUsuarioActual);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<BookingResponse> checkoutBooking(
            @PathVariable Integer id,
            Authentication authentication) {

        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        BookingResponse response = bookingService.checkout(id, idUsuarioActual);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/total")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ReservaTotalResponse> getReservaTotal(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.calculateTotal(id));
    }

    @GetMapping("/{id}/details/rooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ReservaHabitacionView>> getBookingRoomDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(bookingService.getDetallesHabitacion(id));
    }

    @GetMapping("/client/{codigoCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<ReservaView>> getClientBookings(@PathVariable String codigoCliente) {
        return ResponseEntity.ok(bookingService.getClientBookings(codigoCliente));
    }

    /**
     * Método de utilidad para resolver el ID real del usuario que ejecuta la acción.
     * Vital para mantener la trazabilidad y auditoría en la base de datos.
     */
    private Integer obtenerIdUsuario(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {

            // Buscamos el ID real del empleado en la base de datos
            return usuarioRepository.findByUsername(authentication.getName())
                    .map(Usuario::getIdUsuario)
                    .orElse(1); // Fallback de seguridad
        }
        // Si es una petición pública (Guest Checkout), usamos el ID 1 (Sistema/Web)
        return 1;
    }
}

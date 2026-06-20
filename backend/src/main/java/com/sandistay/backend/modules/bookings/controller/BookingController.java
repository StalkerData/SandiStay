package com.sandistay.backend.modules.bookings.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {
        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        BookingResponse response = bookingService.createBooking(request, idUsuarioActual);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservaView>> getAllBookings() {
        List<ReservaView> reservas = bookingService.getAllBookings();
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingFullDetails(@PathVariable Integer id) {
        BookingDetailResponse response = bookingService.getBookingFullDetails(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<CancelBookingResponse> cancelBooking(
            @PathVariable Integer id,
            @Valid @RequestBody CancelBookingRequest request,
            Authentication authentication) {
        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        CancelBookingResponse response = bookingService.cancelBooking(id, request, idUsuarioActual);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<BookingResponse> checkoutBooking(
            @PathVariable Integer id,
            Authentication authentication) {
        Integer idUsuarioActual = obtenerIdUsuario(authentication);
        BookingResponse response = bookingService.checkout(id, idUsuarioActual);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<ReservaTotalResponse> getReservaTotal(@PathVariable Integer id) {
        ReservaTotalResponse response = bookingService.calculateTotal(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details/rooms")
    public ResponseEntity<List<ReservaHabitacionView>> getBookingRoomDetails(@PathVariable Integer id) {
        List<ReservaHabitacionView> detalles = bookingService.getDetallesHabitacion(id);
        return ResponseEntity.ok(detalles);
    }

    // NUEVO ENDPOINT: Obtiene las reservas de un cliente específico para el CRM
    @GetMapping("/client/{codigoCliente}")
    public ResponseEntity<List<ReservaView>> getClientBookings(@PathVariable String codigoCliente) {
        List<ReservaView> reservas = bookingService.getClientBookings(codigoCliente);
        return ResponseEntity.ok(reservas);
    }

    private Integer obtenerIdUsuario(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser")) {
            return 1; 
        }
        return 1; 
    }
}
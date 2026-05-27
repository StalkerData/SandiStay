package com.sandistay.backend.modules.bookings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO inmutable que recibe el payload desde SvelteKit para crear una reserva.
 * Diseñado para soportar tanto reservas de Habitaciones como de Áreas Sociales.
 */
public record CreateBookingRequest(
        
        // --- 1. TIPO DE RESERVA ---
        @NotBlank(message = "El tipo de reserva es obligatorio (Habitacion o Area)")
        @Pattern(regexp = "^(Habitacion|Area)$", message = "El tipo debe ser 'Habitacion' o 'Area'")
        String tipoReserva,
        
        @NotNull(message = "El ID del ítem (Habitación o Área) es obligatorio")
        Integer itemId,

        // --- 2. FECHAS Y HORARIOS ---
        @NotNull(message = "La fecha de inicio/evento es obligatoria")
        LocalDate fechaInicio,
        
        LocalDate fechaFin, // Solo para Habitaciones
        LocalTime horaInicio, // Solo para Áreas
        LocalTime horaFin, // Solo para Áreas

        // --- 3. CAPACIDAD ---
        Integer numeroAdultos,
        Integer numeroNinos,

        // --- 4. DATOS DEL HUÉSPED (Guest Checkout) ---
        @NotBlank(message = "El nombre del huésped es obligatorio")
        String nombreHuesped,
        
        @NotBlank(message = "El documento del huésped es obligatorio")
        String documentoHuesped,
        
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String emailHuesped,
        
        String telefonoHuesped,

        
        String paymentTxId
) {}
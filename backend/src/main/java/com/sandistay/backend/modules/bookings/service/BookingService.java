package com.sandistay.backend.modules.bookings.service;

import com.sandistay.backend.modules.bookings.dto.BookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.model.ReservaArea;
import com.sandistay.backend.modules.bookings.model.ReservaHabitacion;
import com.sandistay.backend.modules.bookings.repository.ReservaAreaRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaHabitacionRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BookingService {

    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final ReservaAreaRepository reservaAreaRepository;

    public BookingService(ReservaRepository reservaRepository,
                          ReservaHabitacionRepository reservaHabitacionRepository,
                          ReservaAreaRepository reservaAreaRepository,
                          CatalogRepository catalogRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.reservaAreaRepository = reservaAreaRepository;
    }

    /**
     * Orquesta la creación de una reserva.
     * @Transactional garantiza que si algo falla (ej. el pago es rechazado), 
     * se hace un ROLLBACK automático en la base de datos.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Integer idUsuarioActual) {

        // 1. VALIDACIÓN DE NEGOCIO (Disponibilidad)
        validarDisponibilidad(request);

        // 2. VERIFICACIÓN DE PAGO (Anti-Fraude)
        // Llamamos al Mock de Pagos para validar el TX-ID enviado por SvelteKit
        boolean pagoValido = mockVerifyPayment(request.paymentTxId());
        if (!pagoValido) {
            throw new RuntimeException("El pago no pudo ser verificado o fue rechazado.");
        }

        // 3. GESTIÓN DEL CLIENTE (Guest Checkout)
        Integer idCliente = getOrCreateCliente(request);

        // 4. PERSISTENCIA: Crear Cabecera de Reserva
        Reserva reserva = new Reserva();
        reserva.setCodigoReserva(generarCodigoUnico());
        reserva.setTipoReserva(request.tipoReserva());
        reserva.setIdCliente(idCliente);
        reserva.setNombreHuesped(request.nombreHuesped());
        reserva.setDocumentoHuesped(request.documentoHuesped());
        reserva.setTelefonoHuesped(request.telefonoHuesped());
        reserva.setNumeroAdultos(request.numeroAdultos() != null ? request.numeroAdultos() : 0);
        reserva.setNumeroNinos(request.numeroNinos() != null ? request.numeroNinos() : 0);
        reserva.setIdEstadoReserva(2); // 2 = CONFIRMADA (Según nuestro seed_data.sql)
        reserva.setIdUsuarioCreacion(idUsuarioActual);
        
        // Guardamos la cabecera para obtener el ID generado por PostgreSQL
        reserva = reservaRepository.save(reserva);

        // 5. PERSISTENCIA: Crear Detalle (Habitación o Área)
        if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
            guardarDetalleHabitacion(request, reserva.getIdReserva());
        } else {
            guardarDetalleArea(request, reserva.getIdReserva());
        }

        // 6. NOTIFICACIÓN ASÍNCRONA
        // TODO: Llamar a EmailService.sendConfirmationEmail(reserva.getEmailHuesped());

        // 7. RETORNAR RESPUESTA AL FRONTEND
        return new BookingResponse(
                reserva.getCodigoReserva(),
                "CONFIRMADA",
                "Reserva creada y pagada exitosamente."
        );
    }

    // =================================================================================
    // MÉTODOS PRIVADOS DE APOYO (Lógica Interna)
    // =================================================================================

    private void validarDisponibilidad(CreateBookingRequest request) {
        // Aquí usaríamos el catalogRepository para llamar a las funciones SQL
        // fn_buscar_habitaciones_disponibles o fn_buscar_areas_disponibles
        // y verificar que el request.itemId() esté en la lista de resultados.
        // Por simplicidad en este paso, asumimos que está disponible.
    }

    private void guardarDetalleHabitacion(CreateBookingRequest request, Integer idReserva) {
        ReservaHabitacion detalle = new ReservaHabitacion();
        detalle.setIdReserva(idReserva);
        detalle.setIdHabitacion(request.itemId());
        detalle.setFechaInicio(request.fechaInicio());
        detalle.setFechaFin(request.fechaFin());
        
        long noches = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin());
        detalle.setNumeroNoches((int) noches);
        detalle.setNumeroDias((int) noches + 1);
        
        // TODO: Obtener precios reales de la BD. Usamos valores dummy por ahora.
        detalle.setPrecioNoche(new BigDecimal("250000.00"));
        detalle.setPrecioDia(new BigDecimal("150000.00"));
        
        reservaHabitacionRepository.save(detalle);
    }

    private void guardarDetalleArea(CreateBookingRequest request, Integer idReserva) {
        ReservaArea detalle = new ReservaArea();
        detalle.setIdReserva(idReserva);
        detalle.setIdArea(request.itemId());
        detalle.setFechaEvento(request.fechaInicio());
        detalle.setHoraInicio(request.horaInicio());
        detalle.setHoraFin(request.horaFin());
        
        long minutos = ChronoUnit.MINUTES.between(request.horaInicio(), request.horaFin());
        detalle.setDuracionHoras(BigDecimal.valueOf(minutos / 60.0));
        
        // TODO: Obtener precios reales de la BD.
        detalle.setPrecioHora(new BigDecimal("80000.00"));
        detalle.setCantidadAsistentes(request.numeroAdultos());
        detalle.setRequiereAnticipo(true);
        detalle.setPorcentajeAnticipo(new BigDecimal("30.00"));
        
        reservaAreaRepository.save(detalle);
    }

    private String generarCodigoUnico() {
        // Genera un código tipo: RES-A1B2C3D4
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // TODO: Mover al módulo de Pagos (PaymentIntegrationService)
    private boolean mockVerifyPayment(String txId) {
        // Simulamos que el Mock de Python siempre nos aprueba el pago
        return txId != null && !txId.isEmpty();
    }

    // TODO: Mover al módulo de Clientes (ClienteService)
    private Integer getOrCreateCliente(CreateBookingRequest request) {
        // Simulamos que el cliente siempre es el ID 1 (Para que no falle la Foreign Key en BD)
        return 1; 
    }
}
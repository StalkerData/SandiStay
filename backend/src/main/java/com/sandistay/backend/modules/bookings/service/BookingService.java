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
import com.sandistay.backend.modules.payment.repository.PagoRepository;
import com.sandistay.backend.modules.payment.client.PaymentClient;
import com.sandistay.backend.modules.payment.model.Pago;
import com.sandistay.backend.modules.bookings.dto.CancelBookingRequest;
import com.sandistay.backend.modules.bookings.dto.CancelBookingResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final ReservaAreaRepository reservaAreaRepository;
    private final CatalogRepository catalogRepository;
    private final PagoRepository pagoRepository;
    private final PaymentClient paymentClient; 


    public BookingService(ReservaRepository reservaRepository,
            ReservaHabitacionRepository reservaHabitacionRepository,
            ReservaAreaRepository reservaAreaRepository,
            CatalogRepository catalogRepository,
            PagoRepository pagoRepository,
            PaymentClient paymentClient) { 
		this.reservaRepository = reservaRepository;
		this.reservaHabitacionRepository = reservaHabitacionRepository;
		this.reservaAreaRepository = reservaAreaRepository;
		this.catalogRepository = catalogRepository;
		this.pagoRepository = pagoRepository;
		this.paymentClient = paymentClient;
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
        // Llamamos al Cliente HTTP que se comunica con Mocki.io
        boolean pagoValido = paymentClient.verifyPayment(request.paymentTxId()); 
        
        if (!pagoValido) {
            throw new RuntimeException("El pago no pudo ser verificado o fue rechazado por la pasarela.");
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
     // =========================================================
        // NUEVO PASO 5.5: PERSISTENCIA DEL PAGO
        // =========================================================
        Pago pago = new Pago();
        pago.setCodigoPago("PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setIdReserva(reserva.getIdReserva());
        
        // Como los precios están fijos por ahora en los métodos de abajo, 
        // guardamos un monto representativo para que la matemática de cancelación funcione.
        pago.setMonto(new BigDecimal("400000.00")); 
        
        pago.setIdMetodoPago(2); // 2 = Nequi (Según nuestro seed_data.sql)
        pago.setNumeroReferencia(request.paymentTxId());
        pago.setIdEstadoPago(2); // 2 = APROBADO
        pago.setIdUsuarioRegistro(idUsuarioActual);
        pago.setObservaciones("Pago procesado y verificado automáticamente vía Mock.");
        
        pagoRepository.save(pago);

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
        if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
            // Consultamos la función SQL a través del repositorio
            boolean disponible = catalogRepository.findAvailableRooms(
                    request.fechaInicio(), request.fechaFin(),
                    request.numeroAdultos(), request.numeroNinos(), null
            ).stream().anyMatch(room -> room.getIdHabitacion().equals(request.itemId()));

            if (!disponible) {
                throw new RuntimeException("La habitación seleccionada no está disponible en esas fechas.");
            }
        } else {
            // Consultamos la función SQL para áreas
            boolean disponible = catalogRepository.findAvailableAreas(
                    request.fechaInicio(), request.horaInicio(), request.horaFin(),
                    request.numeroAdultos(), null
            ).stream().anyMatch(area -> area.getIdArea().equals(request.itemId()));

            if (!disponible) {
                throw new RuntimeException("El área seleccionada no está disponible en ese horario.");
            }
        }
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

    // TODO: Mover al módulo de Clientes (ClienteService)
    private Integer getOrCreateCliente(CreateBookingRequest request) {
        // Simulamos que el cliente siempre es el ID 1 (Para que no falle la Foreign Key en BD)
        return 1; 
    }
    
    
    /**
     * Orquesta la cancelación de una reserva aplicando políticas de reembolso (Split Payment).
     */
    @Transactional
    public CancelBookingResponse cancelBooking(Integer idReserva, CancelBookingRequest request, Integer idUsuarioActual) {
        
        // 1. Buscar la reserva
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Validar que no esté ya cancelada, en curso o completada (Estados 3, 4, 5, 6)
        if (reserva.getIdEstadoReserva() > 2) {
            throw new RuntimeException("La reserva no se puede cancelar en su estado actual.");
        }

        // 2. Buscar el pago original aprobado (Estado 2 = APROBADO)
        List<Pago> pagos = pagoRepository.findByIdReserva(idReserva);
        Pago pagoOriginal = pagos.stream()
                .filter(p -> p.getIdEstadoPago() == 2)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró un pago aprobado para esta reserva."));

        BigDecimal montoPagado = pagoOriginal.getMonto();
        BigDecimal montoMulta = BigDecimal.ZERO;
        BigDecimal montoReembolso = BigDecimal.ZERO;

        // 3. LÓGICA MATEMÁTICA DE PENALIDADES
        if ("Habitacion".equalsIgnoreCase(reserva.getTipoReserva())) {
            ReservaHabitacion detalle = reservaHabitacionRepository.findByIdReserva(idReserva).get(0);
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalle.getFechaInicio());

            if (diasRestantes >= 5) {
                // Penalidad 35% del total pagado
                montoMulta = montoPagado.multiply(new BigDecimal("0.35"));
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                // Penalidad 100%
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        } else {
            // Es un Área Social
            ReservaArea detalle = reservaAreaRepository.findByIdReserva(idReserva).get(0);
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalle.getFechaEvento());
            
            // Calculamos el valor TOTAL del evento para sacar el 15%
            BigDecimal valorTotalEvento = detalle.getPrecioHora().multiply(detalle.getDuracionHoras());

            if (diasRestantes >= 7) {
                // Penalidad 15% del VALOR TOTAL
                montoMulta = valorTotalEvento.multiply(new BigDecimal("0.15"));
                // El reembolso es lo que depositó (30%) menos la multa (15%)
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                // Pierde todo el depósito
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        }

        // 4. SPLIT PAYMENT (Contabilidad)
        // A. Anular lógicamente el pago original (Estado 4 = CANCELADO)
        pagoOriginal.setIdEstadoPago(4);
        pagoOriginal.setObservaciones("Anulado por cancelación. Reemplazado por Multa/Reembolso.");
        pagoRepository.save(pagoOriginal);

        // B. Crear registro de Multa (Estado 2 = APROBADO/PAGADO)
        Pago multa = new Pago();
        multa.setCodigoPago(generarCodigoUnico().replace("RES", "MUL"));
        multa.setIdReserva(idReserva);
        multa.setMonto(montoMulta);
        multa.setIdMetodoPago(pagoOriginal.getIdMetodoPago());
        multa.setIdEstadoPago(2); 
        multa.setIdUsuarioRegistro(idUsuarioActual);
        multa.setObservaciones("Penalidad por cancelación aplicada.");
        pagoRepository.save(multa);

        // C. Crear registro de Reembolso si aplica (Estado 5 = A_DEVOLVER)
        if (montoReembolso.compareTo(BigDecimal.ZERO) > 0) {
            Pago reembolso = new Pago();
            reembolso.setCodigoPago(generarCodigoUnico().replace("RES", "DEV"));
            reembolso.setIdReserva(idReserva);
            reembolso.setMonto(montoReembolso);
            reembolso.setIdMetodoPago(pagoOriginal.getIdMetodoPago());
            reembolso.setIdEstadoPago(5); 
            reembolso.setIdUsuarioRegistro(idUsuarioActual);
            reembolso.setObservaciones("Saldo a favor del cliente pendiente de devolución manual.");
            pagoRepository.save(reembolso);
        }

        // 5. LLAMAR AL PROCEDIMIENTO ALMACENADO (Libera inventario y cambia estado)
        reservaRepository.llamarSpCancelarReserva(idReserva, request.motivo(), idUsuarioActual);

        // 6. RETORNAR RESPUESTA
        return new CancelBookingResponse(
                reserva.getCodigoReserva(),
                "CANCELADA",
                montoMulta,
                montoReembolso,
                "Reserva cancelada exitosamente. Inventario liberado."
        );
    }
    
    
}
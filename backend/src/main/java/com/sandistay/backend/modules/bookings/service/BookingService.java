package com.sandistay.backend.modules.bookings.service;

import com.sandistay.backend.modules.bookings.dto.*;
import com.sandistay.backend.modules.bookings.model.*;
import com.sandistay.backend.modules.bookings.repository.*;
import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.payment.client.PaymentClient;
import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import com.sandistay.backend.modules.payment.model.Pago;
import com.sandistay.backend.modules.payment.repository.PagoRepository;
import com.sandistay.backend.modules.rooms.model.Area;
import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import com.sandistay.backend.modules.rooms.repository.HabitacionRepository;
import com.sandistay.backend.modules.rooms.repository.AreaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio principal del dominio de Reservas (Core Business Logic).
 * Maneja la creación, cancelación (Split Payment) y el ciclo de vida de las reservas.
 */
@Service
public class BookingService {

    // Logger nativo (Sin Lombok)
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final ReservaRepository reservaRepository;
    private final ReservaHabitacionRepository reservaHabitacionRepository;
    private final ReservaAreaRepository reservaAreaRepository;
    private final CatalogRepository catalogRepository;
    private final PagoRepository pagoRepository;
    private final PaymentClient paymentClient;
    private final ReservaViewRepository reservaViewRepository;
    private final ReservaHabitacionViewRepository reservaHabitacionViewRepository;
    private final PersonaRepository personaRepository;
    private final ClienteRepository clienteRepository;
    private final HabitacionRepository habitacionRepository;
    private final AreaRepository areaRepository;

    // Constructor manual explícito
    public BookingService(ReservaRepository reservaRepository,
            ReservaHabitacionRepository reservaHabitacionRepository,
            ReservaAreaRepository reservaAreaRepository,
            CatalogRepository catalogRepository,
            PagoRepository pagoRepository,
            PaymentClient paymentClient,
            ReservaViewRepository reservaViewRepository,
            ReservaHabitacionViewRepository reservaHabitacionViewRepository,
            PersonaRepository personaRepository,
            ClienteRepository clienteRepository,
            HabitacionRepository habitacionRepository,
            AreaRepository areaRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaHabitacionRepository = reservaHabitacionRepository;
        this.reservaAreaRepository = reservaAreaRepository;
        this.catalogRepository = catalogRepository;
        this.pagoRepository = pagoRepository;
        this.paymentClient = paymentClient;
        this.reservaViewRepository = reservaViewRepository;
        this.reservaHabitacionViewRepository = reservaHabitacionViewRepository;
        this.personaRepository = personaRepository;
        this.clienteRepository = clienteRepository;
        this.habitacionRepository = habitacionRepository;
        this.areaRepository = areaRepository;
    }

    // =================================================================
    // CREACIÓN DE RESERVAS (GUEST CHECKOUT & WALK-IN)
    // =================================================================

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Integer idUsuarioActual) {

        // 1. VALIDACIÓN DE NEGOCIO (Disponibilidad)
        validarDisponibilidad(request);

        // 2. GESTIÓN DEL CLIENTE (Guest Checkout)
        Integer idCliente = getOrCreateCliente(request);

        // 3. PERSISTENCIA: Crear Cabecera de Reserva
        Reserva reserva = new Reserva();
        reserva.setCodigoReserva(generarCodigoUnico());
        reserva.setTipoReserva(request.tipoReserva());
        reserva.setIdCliente(idCliente);
        reserva.setNombreHuesped(request.nombresHuesped() + " " + request.apellidosHuesped());
        reserva.setDocumentoHuesped(request.documentoHuesped());
        reserva.setTelefonoHuesped(request.telefonoHuesped());
        reserva.setNumeroAdultos(request.numeroAdultos() != null ? request.numeroAdultos() : 0);
        reserva.setNumeroNinos(request.numeroNinos() != null ? request.numeroNinos() : 0);
        reserva.setIdUsuarioCreacion(idUsuarioActual);

        // TODA RESERVA NACE PENDIENTE HASTA QUE SE PROCESE EL PAGO
        reserva.setIdEstadoReserva(1); // 1 = PENDIENTE
        String estadoFinalString = "PENDIENTE";
        String mensajeFinal = "Reserva creada. Tienes 24 horas para realizar el pago.";

        reserva = reservaRepository.save(reserva);

        // 4. PERSISTENCIA: Crear Detalle (Habitación o Área)
        if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
            guardarDetalleHabitacion(request, reserva.getIdReserva());
        } else {
            guardarDetalleArea(request, reserva.getIdReserva());
        }

        // 5. PROCESAMIENTO DE PAGO (Si el cliente envió el comprobante)
        if (request.paymentTxId() != null && !request.paymentTxId().isBlank()) {
            boolean esPagoCompletoArea = "Area".equalsIgnoreCase(request.tipoReserva()) && Boolean.TRUE.equals(request.pagoCompleto());

            procesarPagoReserva(reserva.getIdReserva(), request.paymentTxId(), request.tipoReserva(), esPagoCompletoArea, idUsuarioActual);

            // 6. ACTUALIZACIÓN DE ESTADO SEGÚN EL PAGO
            if ("Habitacion".equalsIgnoreCase(request.tipoReserva()) || esPagoCompletoArea) {
                reserva.setIdEstadoReserva(2); // 2 = CONFIRMADA
                reservaRepository.save(reserva);
                estadoFinalString = "CONFIRMADA";
                mensajeFinal = "Reserva creada y pago total conciliado exitosamente.";
            } else {
                // Es un Área con pago parcial (30%). Se queda en PENDIENTE.
                estadoFinalString = "PENDIENTE";
                mensajeFinal = "Reserva creada. Anticipo del 30% conciliado. Saldo pendiente.";
            }
        }

        return new BookingResponse(reserva.getCodigoReserva(), estadoFinalString, mensajeFinal);
    }

    private void procesarPagoReserva(Integer idReserva, String paymentTxId, String tipoReserva, boolean esPagoCompletoArea, Integer idUsuarioActual) {
        ReservaTotalResponse totales = calculateTotal(idReserva);
        BigDecimal montoAPagar = totales.totalFinal();

        if ("Area".equalsIgnoreCase(tipoReserva) && !esPagoCompletoArea) {
            // Si no paga completo, solo cobramos el 30% de anticipo
            montoAPagar = montoAPagar.multiply(new BigDecimal("0.30"));
        }

        // Validación del pago (Efectivo vs Mock)
        if ("EFECTIVO".equals(paymentTxId)) {
            log.info("Pago en efectivo registrado por el Staff para la reserva ID: {}", idReserva);
        } else {
            PaymentVerificationResponse bankResponse = paymentClient.verifyPayment(paymentTxId);
            if (!bankResponse.found()) {
                throw new IllegalArgumentException("Pago rechazado: " + bankResponse.message());
            }
            if (bankResponse.amount().compareTo(montoAPagar) != 0) {
                throw new IllegalArgumentException(String.format("Fraude detectado: El comprobante es por $%s, pero la reserva requiere $%s.", bankResponse.amount(), montoAPagar));
            }
        }

        // Guardar el recibo de pago
        Pago pago = new Pago();
        pago.setCodigoPago("PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setIdReserva(idReserva);
        pago.setMonto(montoAPagar);
        pago.setIdMetodoPago("EFECTIVO".equals(paymentTxId) ? 1 : 2);
        pago.setNumeroReferencia(paymentTxId);
        pago.setIdEstadoPago(2); // 2 = APROBADO
        pago.setIdUsuarioRegistro(idUsuarioActual);
        pago.setObservaciones("Pago procesado y verificado.");
        pagoRepository.save(pago);
    }

    private void validarDisponibilidad(CreateBookingRequest request) {
        if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
            boolean disponible = catalogRepository.findAvailableRooms(
                    request.fechaInicio(), request.fechaFin(), request.numeroAdultos(), request.numeroNinos(), null
            ).stream().anyMatch(room -> room.getIdHabitacion().equals(request.itemId()));
            if (!disponible) throw new IllegalArgumentException("La habitación seleccionada no está disponible en esas fechas.");
        } else {
            boolean disponible = catalogRepository.findAvailableAreas(
                    request.fechaInicio(), request.horaInicio(), request.horaFin(), request.numeroAdultos(), null
            ).stream().anyMatch(area -> area.getIdArea().equals(request.itemId()));
            if (!disponible) throw new IllegalArgumentException("El área seleccionada no está disponible en ese horario.");
        }
    }

    private void guardarDetalleHabitacion(CreateBookingRequest request, Integer idReserva) {
        Habitacion habitacion = habitacionRepository.findById(request.itemId())
                .orElseThrow(() -> new IllegalArgumentException("Habitación no encontrada"));

        ReservaHabitacion detalle = new ReservaHabitacion();
        detalle.setIdReserva(idReserva);
        detalle.setIdHabitacion(request.itemId());
        detalle.setFechaInicio(request.fechaInicio());
        detalle.setFechaFin(request.fechaFin());

        long noches = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin());
        if (noches < 1) throw new IllegalArgumentException("La reserva debe ser de al menos 1 noche.");

        detalle.setNumeroNoches((int) noches);
        detalle.setNumeroDias(0);
        detalle.setPrecioNoche(habitacion.getPrecioNoche());
        detalle.setPrecioDia(BigDecimal.ZERO);

        reservaHabitacionRepository.save(detalle);
    }

    private void guardarDetalleArea(CreateBookingRequest request, Integer idReserva) {
        Area area = areaRepository.findById(request.itemId())
                .orElseThrow(() -> new IllegalArgumentException("Área no encontrada"));

        ReservaArea detalle = new ReservaArea();
        detalle.setIdReserva(idReserva);
        detalle.setIdArea(request.itemId());
        detalle.setFechaEvento(request.fechaInicio());
        detalle.setHoraInicio(request.horaInicio());
        detalle.setHoraFin(request.horaFin());

        long minutos = ChronoUnit.MINUTES.between(request.horaInicio(), request.horaFin());
        if (minutos <= 0) throw new IllegalArgumentException("La hora de fin debe ser mayor a la hora de inicio.");

        detalle.setDuracionHoras(BigDecimal.valueOf(minutos / 60.0));
        detalle.setPrecioHora(area.getPrecioHora());
        detalle.setCantidadAsistentes(request.numeroAdultos());
        detalle.setRequiereAnticipo(true);
        detalle.setPorcentajeAnticipo(new BigDecimal("30.00"));

        reservaAreaRepository.save(detalle);
    }

    private String generarCodigoUnico() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Integer getOrCreateCliente(CreateBookingRequest request) {
        Optional<Persona> personaOpt = personaRepository.findByNumeroDocumento(request.documentoHuesped());
        Persona persona;
        if (personaOpt.isPresent()) {
            persona = personaOpt.get();
            boolean emailCoincide = persona.getEmail().equalsIgnoreCase(request.emailHuesped());
            boolean nombreCoincide = persona.getNombres().equalsIgnoreCase(request.nombresHuesped());
            if (!emailCoincide && !nombreCoincide) {
                throw new IllegalArgumentException("El número de documento ya está registrado con otra identidad. Por favor verifique sus datos o contacte a recepción.");
            }
        } else {
            Persona nuevaPersona = new Persona();
            nuevaPersona.setNombres(request.nombresHuesped());
            nuevaPersona.setApellidos(request.apellidosHuesped());
            nuevaPersona.setIdTipoDocumento(request.idTipoDocumento());
            nuevaPersona.setNumeroDocumento(request.documentoHuesped());
            nuevaPersona.setEmail(request.emailHuesped());
            nuevaPersona.setTelefono(request.telefonoHuesped());
            nuevaPersona.setIdPais(request.idPais());
            nuevaPersona.setIdEstadoPersona(1);
            persona = personaRepository.save(nuevaPersona);
        }

        Cliente cliente = clienteRepository.findByIdPersona(persona.getIdPersona())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setIdPersona(persona.getIdPersona());
                    nuevoCliente.setCodigoCliente("CLI-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    nuevoCliente.setIdTipoCliente(1);
                    return clienteRepository.save(nuevoCliente);
                });
        return cliente.getIdCliente();
    }

    // =================================================================
    // CANCELACIÓN Y SPLIT PAYMENT
    // =================================================================

    @Transactional
    public CancelBookingResponse cancelBooking(Integer idReserva, CancelBookingRequest request, Integer idUsuarioActual) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (reserva.getIdEstadoReserva() > 2) {
            throw new IllegalArgumentException("La reserva no se puede cancelar en su estado actual.");
        }

        List<Pago> pagosAprobados = pagoRepository.findByIdReserva(idReserva).stream()
                .filter(p -> p.getIdEstadoPago() == 2).toList();

        if (pagosAprobados.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron pagos aprobados para esta reserva.");
        }

        BigDecimal montoPagado = pagosAprobados.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal montoMulta = BigDecimal.ZERO;
        BigDecimal montoReembolso = BigDecimal.ZERO;

        if ("Habitacion".equalsIgnoreCase(reserva.getTipoReserva())) {
            List<ReservaHabitacion> detalles = reservaHabitacionRepository.findByIdReserva(idReserva);
            if (detalles.isEmpty()) throw new IllegalArgumentException("Detalle de habitación no encontrado.");

            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalles.get(0).getFechaInicio());
            if (diasRestantes >= 5) {
                montoMulta = montoPagado.multiply(new BigDecimal("0.35"));
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        } else {
            List<ReservaArea> detalles = reservaAreaRepository.findByIdReserva(idReserva);
            if (detalles.isEmpty()) throw new IllegalArgumentException("Detalle de área no encontrado.");

            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalles.get(0).getFechaEvento());
            BigDecimal valorTotalEvento = detalles.get(0).getPrecioHora().multiply(detalles.get(0).getDuracionHoras());
            if (diasRestantes >= 7) {
                montoMulta = valorTotalEvento.multiply(new BigDecimal("0.15"));
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        }

        // Anular pagos originales
        for (Pago p : pagosAprobados) {
            p.setIdEstadoPago(4);
            p.setObservaciones(p.getObservaciones() + " | Anulado por cancelación.");
            pagoRepository.save(p);
        }

        // Registrar Multa
        Pago multa = new Pago();
        multa.setCodigoPago(generarCodigoUnico().replace("RES", "MUL"));
        multa.setIdReserva(idReserva);
        multa.setMonto(montoMulta);
        multa.setIdMetodoPago(pagosAprobados.get(0).getIdMetodoPago());
        multa.setIdEstadoPago(2);
        multa.setIdUsuarioRegistro(idUsuarioActual);
        multa.setObservaciones("Penalidad por cancelación aplicada.");
        pagoRepository.save(multa);

        // Registrar Reembolso (Si aplica)
        if (montoReembolso.compareTo(BigDecimal.ZERO) > 0) {
            Pago reembolso = new Pago();
            reembolso.setCodigoPago(generarCodigoUnico().replace("RES", "DEV"));
            reembolso.setIdReserva(idReserva);
            reembolso.setMonto(montoReembolso);
            reembolso.setIdMetodoPago(pagosAprobados.get(0).getIdMetodoPago());
            reembolso.setIdEstadoPago(5); // 5 = PENDIENTE DE REEMBOLSO
            reembolso.setIdUsuarioRegistro(idUsuarioActual);
            reembolso.setObservaciones("Saldo a favor del cliente pendiente de devolución manual.");
            pagoRepository.save(reembolso);
        }

        // Llamar al Procedimiento Almacenado para liberar inventario
        reservaRepository.llamarSpCancelarReserva(idReserva, request.motivo(), idUsuarioActual);

        return new CancelBookingResponse(reserva.getCodigoReserva(), "CANCELADA", montoMulta, montoReembolso, "Reserva cancelada exitosamente. Inventario liberado.");
    }

    // =================================================================
    // CHECK-OUT Y FIDELIZACIÓN
    // =================================================================

    @Transactional
    public BookingResponse checkout(Integer idReserva, Integer idUsuarioActual) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // 1. Llamar al SP para completar la reserva
        reservaRepository.llamarSpCompletarReserva(idReserva, idUsuarioActual);

        // 2. LÓGICA DE FIDELIZACIÓN (Ascenso Automático a Premium)
        long completadas = reservaRepository.countByIdClienteAndIdEstadoReserva(reserva.getIdCliente(), 4);

        if (completadas >= 5) {
            clienteRepository.findById(reserva.getIdCliente()).ifPresent(cliente -> {
                if (cliente.getIdTipoCliente() == 1) {
                    cliente.setIdTipoCliente(2);
                    clienteRepository.save(cliente);
                    log.info("Cliente ID {} ascendido automáticamente a PREMIUM por fidelidad.", cliente.getIdCliente());
                }
            });
        }

        return new BookingResponse(reserva.getCodigoReserva(), "COMPLETADA", "Check-out realizado exitosamente.");
    }

    // =================================================================
    // CONSULTAS (READ-ONLY)
    // =================================================================

    @Transactional(readOnly = true)
    public List<ReservaView> getAllBookings() {
        return reservaViewRepository.findAllByOrderByFechaReservaDesc();
    }

    @Transactional(readOnly = true)
    public List<ReservaHabitacionView> getDetallesHabitacion(Integer idReserva) {
        return reservaHabitacionViewRepository.findByIdReserva(idReserva);
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingFullDetails(Integer idReserva) {
        ReservaView reservaView = reservaViewRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        Reserva reservaEntity = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        List<ReservaHabitacionView> items = reservaHabitacionViewRepository.findByIdReserva(idReserva);
        List<Pago> pagos = pagoRepository.findByIdReserva(idReserva);

        return new BookingDetailResponse(reservaView, items, pagos, reservaEntity.getIdCliente());
    }

    @Transactional(readOnly = true)
    public ReservaTotalResponse calculateTotal(Integer idReserva) {
        ReservaTotalProjection projection = reservaRepository.calcularTotalReserva(idReserva);
        if (projection == null) {
            throw new IllegalArgumentException("No se pudo calcular el total para la reserva especificada.");
        }
        return new ReservaTotalResponse(
                projection.getSubtotalHabitaciones(),
                projection.getSubtotalAreas(),
                projection.getDescuentoCliente(),
                projection.getTotalFinal()
        );
    }

    @Transactional(readOnly = true)
    public List<ReservaView> getClientBookings(String codigoCliente) {
        return reservaViewRepository.findByCodigoClienteOrderByFechaReservaDesc(codigoCliente);
    }
}

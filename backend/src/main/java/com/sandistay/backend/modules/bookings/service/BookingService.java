package com.sandistay.backend.modules.bookings.service;

import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.bookings.dto.BookingDetailResponse;
import com.sandistay.backend.modules.bookings.dto.BookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.dto.ReservaTotalResponse;
import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.model.ReservaArea;
import com.sandistay.backend.modules.bookings.model.ReservaHabitacion;
import com.sandistay.backend.modules.bookings.model.ReservaHabitacionView;
import com.sandistay.backend.modules.bookings.model.ReservaTotalProjection;
import com.sandistay.backend.modules.bookings.model.ReservaView;
import com.sandistay.backend.modules.bookings.repository.ReservaAreaRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaHabitacionRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaHabitacionViewRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaViewRepository;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import com.sandistay.backend.modules.rooms.repository.HabitacionRepository;
import com.sandistay.backend.modules.rooms.repository.AreaRepository;
import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.model.Area;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sandistay.backend.modules.payment.repository.PagoRepository;
import com.sandistay.backend.modules.payment.client.PaymentClient;
import com.sandistay.backend.modules.payment.model.Pago;
import com.sandistay.backend.modules.bookings.dto.CancelBookingRequest;
import com.sandistay.backend.modules.bookings.dto.CancelBookingResponse;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
	
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

	public BookingService(ReservaRepository reservaRepository, ReservaHabitacionRepository reservaHabitacionRepository,
			ReservaAreaRepository reservaAreaRepository, CatalogRepository catalogRepository,
			PagoRepository pagoRepository, PaymentClient paymentClient, ReservaViewRepository reservaViewRepository,
			ReservaHabitacionViewRepository reservaHabitacionViewRepository, PersonaRepository personaRepository,
			ClienteRepository clienteRepository, HabitacionRepository habitacionRepository, AreaRepository areaRepository) {
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

	/**
	 * Orquesta la creación de una reserva.
	 * 
	 * @Transactional garantiza que si algo falla (ej. el pago es rechazado), se
	 *                hace un ROLLBACK automático en la base de datos.
	 */

	@Transactional
	public BookingResponse createBooking(CreateBookingRequest request, Integer idUsuarioActual) {

		// 1. VALIDACIÓN DE NEGOCIO (Disponibilidad)
		validarDisponibilidad(request);

		// 2. GESTIÓN DEL CLIENTE
		Integer idCliente = getOrCreateCliente(request);

		// 3. PERSISTENCIA: Crear Cabecera de Reserva (SIEMPRE NACE PENDIENTE)
		Reserva reserva = new Reserva();
		reserva.setCodigoReserva(generarCodigoUnico());
		reserva.setTipoReserva(request.tipoReserva());
		reserva.setIdCliente(idCliente);
		reserva.setNombreHuesped(request.nombresHuesped() + " " + request.apellidosHuesped());
		reserva.setDocumentoHuesped(request.documentoHuesped());
		reserva.setTelefonoHuesped(request.telefonoHuesped());
		reserva.setNumeroAdultos(request.numeroAdultos() != null ? request.numeroAdultos() : 0);
		reserva.setNumeroNinos(request.numeroNinos() != null ? request.numeroNinos() : 0);
		reserva.setIdEstadoReserva(1); // 1 = PENDIENTE (Esperando pago)
		reserva.setIdUsuarioCreacion(idUsuarioActual);

		reserva = reservaRepository.save(reserva);

		// 4. PERSISTENCIA: Crear Detalle (Habitación o Área)
		if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
			guardarDetalleHabitacion(request, reserva.getIdReserva());
		} else {
			guardarDetalleArea(request, reserva.getIdReserva());
		}

		String estadoFinalString = "PENDIENTE";
		String mensajeFinal = "Reserva creada. Tienes 24 horas para realizar el pago.";


		// =========================================================
		// 5. PROCESAMIENTO DE PAGO Y CONCILIACIÓN BANCARIA
		// =========================================================
		if (request.paymentTxId() != null && !request.paymentTxId().isBlank()) {

			// A. Calculamos cuánto DEBE pagar el cliente
			ReservaTotalResponse totales = calculateTotal(reserva.getIdReserva());
			BigDecimal montoAPagar = totales.totalFinal();

			// 2. Lógica de Anticipo para Áreas Sociales
	        if ("Area".equalsIgnoreCase(request.tipoReserva())) {
	            // Si el cliente NO marcó "pagoCompleto", le cobramos solo el 30%
	            if (request.pagoCompleto() == null || !request.pagoCompleto()) {
	                montoAPagar = montoAPagar.multiply(new BigDecimal("0.30"));
	            }
	        }

			// B. Lógica de Efectivo (Staff)
			if ("EFECTIVO".equals(request.paymentTxId())) {
				// Asumimos que la Secretaria ya tiene el dinero físico
				log.info("Pago en efectivo registrado por el Staff.");
			}
			// C. Lógica Digital (Conciliación con el Mock de Python)
			else {

				PaymentVerificationResponse bankResponse = paymentClient.verifyPayment(request.paymentTxId());

				if (!bankResponse.found()) {
					throw new RuntimeException("Pago rechazado: " + bankResponse.message());
				}

				// ¡LA PRUEBA ANTI-FRAUDE! Comparamos el monto del banco con el monto a pagar
				// Usamos compareTo() porque en BigDecimal 320000.0 es igual a 320000.00
				if (bankResponse.amount().compareTo(montoAPagar) != 0) {
					throw new RuntimeException(
							String.format("Fraude detectado: El comprobante es por $%s, pero la reserva requiere $%s.",
									bankResponse.amount(), montoAPagar));
				}
			}

			// D. Guardamos el recibo en nuestra base de datos
			Pago pago = new Pago();
			pago.setCodigoPago("PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
			pago.setIdReserva(reserva.getIdReserva());
			pago.setMonto(montoAPagar);
			pago.setIdMetodoPago("EFECTIVO".equals(request.paymentTxId()) ? 1 : 2);
			pago.setNumeroReferencia(request.paymentTxId());
			pago.setIdEstadoPago(2); // APROBADO
			pago.setIdUsuarioRegistro(idUsuarioActual);
			pago.setObservaciones("Pago conciliado exitosamente.");
			pagoRepository.save(pago);

			// E. Actualizamos el estado de la reserva
			if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
				reserva.setIdEstadoReserva(2); // CONFIRMADA
				reservaRepository.save(reserva);
				estadoFinalString = "CONFIRMADA";
				mensajeFinal = "Reserva creada y pago conciliado exitosamente.";
			} else {
				mensajeFinal = "Reserva creada. Anticipo del 30% conciliado. Saldo pendiente.";
			}
		}
		// 6. RETORNAR RESPUESTA AL FRONTEND
		return new BookingResponse(reserva.getCodigoReserva(), estadoFinalString, mensajeFinal);
	}

	// =================================================================================
	// MÉTODOS PRIVADOS DE APOYO (Lógica Interna)
	// =================================================================================

	private void validarDisponibilidad(CreateBookingRequest request) {
		if ("Habitacion".equalsIgnoreCase(request.tipoReserva())) {
			// Consultamos la función SQL a través del repositorio
			boolean disponible = catalogRepository
					.findAvailableRooms(request.fechaInicio(), request.fechaFin(), request.numeroAdultos(),
							request.numeroNinos(), null)
					.stream().anyMatch(room -> room.getIdHabitacion().equals(request.itemId()));

			if (!disponible) {
				throw new RuntimeException("La habitación seleccionada no está disponible en esas fechas.");
			}
		} else {
			// Consultamos la función SQL para áreas
			boolean disponible = catalogRepository
					.findAvailableAreas(request.fechaInicio(), request.horaInicio(), request.horaFin(),
							request.numeroAdultos(), null)
					.stream().anyMatch(area -> area.getIdArea().equals(request.itemId()));

			if (!disponible) {
				throw new RuntimeException("El área seleccionada no está disponible en ese horario.");
			}
		}
	}

	private void guardarDetalleHabitacion(CreateBookingRequest request, Integer idReserva) {
        Habitacion habitacion = habitacionRepository.findById(request.itemId())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        ReservaHabitacion detalle = new ReservaHabitacion();
        detalle.setIdReserva(idReserva);
        detalle.setIdHabitacion(request.itemId());
        detalle.setFechaInicio(request.fechaInicio());
        detalle.setFechaFin(request.fechaFin());
        
        long noches = ChronoUnit.DAYS.between(request.fechaInicio(), request.fechaFin());
        if (noches < 1) throw new RuntimeException("La reserva debe ser de al menos 1 noche.");

        detalle.setNumeroNoches((int) noches);
        detalle.setNumeroDias(0); // Simplificamos: solo cobramos noches
        
        // AHORA SÍ: PRECIOS REALES DE LA BD
        detalle.setPrecioNoche(habitacion.getPrecioNoche());
        detalle.setPrecioDia(BigDecimal.ZERO); 
        
        reservaHabitacionRepository.save(detalle);
    }

    private void guardarDetalleArea(CreateBookingRequest request, Integer idReserva) {
        Area area = areaRepository.findById(request.itemId())
                .orElseThrow(() -> new RuntimeException("Área no encontrada"));

        ReservaArea detalle = new ReservaArea();
        detalle.setIdReserva(idReserva);
        detalle.setIdArea(request.itemId());
        detalle.setFechaEvento(request.fechaInicio());
        detalle.setHoraInicio(request.horaInicio());
        detalle.setHoraFin(request.horaFin());
        
        long minutos = ChronoUnit.MINUTES.between(request.horaInicio(), request.horaFin());
        if (minutos <= 0) throw new RuntimeException("La hora de fin debe ser mayor a la hora de inicio.");
        
        detalle.setDuracionHoras(BigDecimal.valueOf(minutos / 60.0));
        
        // AHORA SÍ: PRECIOS REALES DE LA BD
        detalle.setPrecioHora(area.getPrecioHora());
        detalle.setCantidadAsistentes(request.numeroAdultos());
        detalle.setRequiereAnticipo(true);
        detalle.setPorcentajeAnticipo(new BigDecimal("30.00"));
        
        reservaAreaRepository.save(detalle);
    }

	private String generarCodigoUnico() {
		// Genera un código tipo: RES-A1B2C3D4
		return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	/**
     * Busca si el huésped ya existe en la BD. Si no existe, crea la Persona y el Cliente.
     * Incluye validación estricta de identidad para evitar suplantación.
     */
    private Integer getOrCreateCliente(CreateBookingRequest request) {
        // 1. Buscar Persona por documento
        java.util.Optional<Persona> personaOpt = personaRepository.findByNumeroDocumento(request.documentoHuesped());
        
        Persona persona;
        
        if (personaOpt.isPresent()) {
            // Si la persona ya existe en la BD, la extraemos
            persona = personaOpt.get();
            
            // VALIDACIÓN DE IDENTIDAD (Anti-Suplantación)
            // Comparamos ignorando mayúsculas/minúsculas. Si el email Y el nombre son distintos, bloqueamos.
            boolean emailCoincide = persona.getEmail().equalsIgnoreCase(request.emailHuesped());
            boolean nombreCoincide = persona.getNombres().equalsIgnoreCase(request.nombresHuesped());
            
            if (!emailCoincide && !nombreCoincide) {
                throw new RuntimeException("El número de documento ya está registrado con otra identidad. Por favor verifique sus datos o contacte a recepción.");
            }
        } else {
            // Si no existe, creamos una nueva Persona
            Persona nuevaPersona = new Persona();
            nuevaPersona.setNombres(request.nombresHuesped());
            nuevaPersona.setApellidos(request.apellidosHuesped());
            nuevaPersona.setIdTipoDocumento(request.idTipoDocumento());
            nuevaPersona.setNumeroDocumento(request.documentoHuesped());
            nuevaPersona.setEmail(request.emailHuesped());
            nuevaPersona.setTelefono(request.telefonoHuesped());
            nuevaPersona.setIdPais(request.idPais());
            nuevaPersona.setIdEstadoPersona(1); // 1 = ACTIVO
            
            // Guardamos y asignamos a la variable principal
            persona = personaRepository.save(nuevaPersona);
        }

        // 2. Buscar o Crear Cliente asociado a esa Persona
        // Aquí sí podemos usar orElseGet porque la lógica es muy simple y directa
        Cliente cliente = clienteRepository.findByIdPersona(persona.getIdPersona())
                .orElseGet(() -> {
                    Cliente nuevoCliente = new Cliente();
                    nuevoCliente.setIdPersona(persona.getIdPersona());
                    nuevoCliente.setCodigoCliente("CLI-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    nuevoCliente.setIdTipoCliente(1); // 1 = REGULAR (Por defecto)
                    return clienteRepository.save(nuevoCliente);
                });

        return cliente.getIdCliente();
    }
	
	/**
	 * Orquesta la cancelación de una reserva aplicando políticas de reembolso
	 * (Split Payment).
	 */
	@Transactional
    public CancelBookingResponse cancelBooking(Integer idReserva, CancelBookingRequest request, Integer idUsuarioActual) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (reserva.getIdEstadoReserva() > 2) {
            throw new RuntimeException("La reserva no se puede cancelar en su estado actual.");
        }

        // 1. Buscar TODOS los pagos aprobados
        List<Pago> pagosAprobados = pagoRepository.findByIdReserva(idReserva).stream()
                .filter(p -> p.getIdEstadoPago() == 2)
                .toList();

        if (pagosAprobados.isEmpty()) {
            throw new RuntimeException("No se encontraron pagos aprobados para esta reserva.");
        }

        // 2. Sumar todo lo que el cliente ha pagado realmente
        BigDecimal montoPagado = pagosAprobados.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal montoMulta = BigDecimal.ZERO;
        BigDecimal montoReembolso = BigDecimal.ZERO;

        // 3. LÓGICA MATEMÁTICA DE PENALIDADES
        if ("Habitacion".equalsIgnoreCase(reserva.getTipoReserva())) {
            ReservaHabitacion detalle = reservaHabitacionRepository.findByIdReserva(idReserva).get(0);
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalle.getFechaInicio());

            if (diasRestantes >= 5) {
                montoMulta = montoPagado.multiply(new BigDecimal("0.35"));
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        } else {
            ReservaArea detalle = reservaAreaRepository.findByIdReserva(idReserva).get(0);
            long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), detalle.getFechaEvento());
            BigDecimal valorTotalEvento = detalle.getPrecioHora().multiply(detalle.getDuracionHoras());

            if (diasRestantes >= 7) {
                montoMulta = valorTotalEvento.multiply(new BigDecimal("0.15"));
                montoReembolso = montoPagado.subtract(montoMulta);
            } else {
                montoMulta = montoPagado;
                montoReembolso = BigDecimal.ZERO;
            }
        }

        // 4. SPLIT PAYMENT (Contabilidad)
        // A. Anular lógicamente TODOS los pagos originales
        for (Pago p : pagosAprobados) {
            p.setIdEstadoPago(4);
            p.setObservaciones(p.getObservaciones() + " | Anulado por cancelación.");
            pagoRepository.save(p);
        }

        // B. Crear registro de Multa
        Pago multa = new Pago();
        multa.setCodigoPago(generarCodigoUnico().replace("RES", "MUL"));
        multa.setIdReserva(idReserva);
        multa.setMonto(montoMulta);
        multa.setIdMetodoPago(pagosAprobados.get(0).getIdMetodoPago());
        multa.setIdEstadoPago(2); 
        multa.setIdUsuarioRegistro(idUsuarioActual);
        multa.setObservaciones("Penalidad por cancelación aplicada.");
        pagoRepository.save(multa);

        // C. Crear registro de Reembolso
        if (montoReembolso.compareTo(BigDecimal.ZERO) > 0) {
            Pago reembolso = new Pago();
            reembolso.setCodigoPago(generarCodigoUnico().replace("RES", "DEV"));
            reembolso.setIdReserva(idReserva);
            reembolso.setMonto(montoReembolso);
            reembolso.setIdMetodoPago(pagosAprobados.get(0).getIdMetodoPago());
            reembolso.setIdEstadoPago(5); 
            reembolso.setIdUsuarioRegistro(idUsuarioActual);
            reembolso.setObservaciones("Saldo a favor del cliente pendiente de devolución manual.");
            pagoRepository.save(reembolso);
        }

        reservaRepository.llamarSpCancelarReserva(idReserva, request.motivo(), idUsuarioActual);

        return new CancelBookingResponse(
                reserva.getCodigoReserva(), "CANCELADA", montoMulta, montoReembolso,
                "Reserva cancelada exitosamente. Inventario liberado."
        );
    }

	/**
	 * Obtiene todas las reservas usando la vista optimizada de PostgreSQL.
	 */
	public List<com.sandistay.backend.modules.bookings.model.ReservaView> getAllBookings() {
		return reservaViewRepository.findAllByOrderByFechaReservaDesc();
	}

	/**
	 * Obtiene los detalles de las habitaciones de una reserva usando la vista SQL.
	 */
	public List<com.sandistay.backend.modules.bookings.model.ReservaHabitacionView> getDetallesHabitacion(
			Integer idReserva) {
		return reservaHabitacionViewRepository.findByIdReserva(idReserva);
	}

	/**
	 * Ejecuta el proceso de Check-out delegando la validación financiera a
	 * PostgreSQL.
	 */
	@Transactional
	public BookingResponse checkout(Integer idReserva, Integer idUsuarioActual) {

		// 1. Validar que la reserva exista
		Reserva reserva = reservaRepository.findById(idReserva)
				.orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

		// 2. Llamar al Procedimiento Almacenado
		// Si el cliente tiene saldo pendiente, Postgres lanzará un RAISE EXCEPTION
		// que Spring Boot atrapará y convertirá en un error HTTP automáticamente.
		reservaRepository.llamarSpCompletarReserva(idReserva, idUsuarioActual);

		// 3. Retornar respuesta de éxito
		return new BookingResponse(reserva.getCodigoReserva(), "COMPLETADA",
				"Check-out realizado exitosamente. Habitación enviada a limpieza.");
	}

	/**
	 * Obtiene el detalle completo de una reserva agrupando cabecera, ítems y pagos.
	 */

	public BookingDetailResponse getBookingFullDetails(Integer idReserva) {
		ReservaView reservaView = reservaViewRepository.findById(idReserva)
				.orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

		// NUEVO: Buscamos la entidad real para extraer el idCliente
		Reserva reservaEntity = reservaRepository.findById(idReserva)
				.orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

		List<ReservaHabitacionView> items = reservaHabitacionViewRepository.findByIdReserva(idReserva);
		List<Pago> pagos = pagoRepository.findByIdReserva(idReserva);

		// Pasamos el idCliente al DTO
		return new BookingDetailResponse(reservaView, items, pagos, reservaEntity.getIdCliente());
	}

	/**
	 * Obtiene el cálculo financiero total de una reserva delegando a PostgreSQL.
	 * Este método es utilizado tanto por el endpoint GET /{id}/total como
	 * internamente durante la creación de la reserva para determinar el monto a
	 * cobrar.
	 */
	public ReservaTotalResponse calculateTotal(Integer idReserva) {
		// Llamamos a la proyección que creamos en ReservaRepository
		ReservaTotalProjection projection = reservaRepository.calcularTotalReserva(idReserva);

		if (projection == null) {
			throw new RuntimeException("No se pudo calcular el total para la reserva especificada.");
		}

		return new ReservaTotalResponse(projection.getSubtotalHabitaciones(), projection.getSubtotalAreas(),
				projection.getDescuentoCliente(), projection.getTotalFinal());
	}

	// ...

}
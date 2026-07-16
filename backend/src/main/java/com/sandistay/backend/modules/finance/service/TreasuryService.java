package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.finance.dto.RegisterPaymentRequest;
import com.sandistay.backend.modules.finance.model.EstadoCuentaView;
import com.sandistay.backend.modules.finance.repository.EstadoCuentaViewRepository;
import com.sandistay.backend.modules.payment.model.Pago;
import com.sandistay.backend.modules.payment.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de Tesorería.
 * Maneja el flujo de caja manual: Cuentas por cobrar (Abonos) y Cuentas por pagar (Reembolsos).
 */
@Service
public class TreasuryService {

    private final EstadoCuentaViewRepository estadoCuentaViewRepository;
    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    // Constructor manual explícito
    public TreasuryService(EstadoCuentaViewRepository estadoCuentaViewRepository,
                           PagoRepository pagoRepository,
                           ReservaRepository reservaRepository) {
        this.estadoCuentaViewRepository = estadoCuentaViewRepository;
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
    }

    /**
     * 1. CUENTAS POR COBRAR: Obtiene todas las reservas con saldo pendiente.
     * Excluye las reservas Canceladas (CANC) y No Show (NOSHW) porque su deuda ya fue saldada con la multa.
     */
    @Transactional(readOnly = true)
    public List<EstadoCuentaView> getReceivables() {
        return estadoCuentaViewRepository.findBySaldoPendienteGreaterThan(BigDecimal.ZERO)
                .stream()
                .filter(ec -> !"CANC".equals(ec.getEstadoReserva()) && !"NOSHW".equals(ec.getEstadoReserva()))
                .toList();
    }

    /**
     * 2. REEMBOLSOS PENDIENTES: Obtiene todos los pagos en estado A_DEVOLVER (ID 5).
     */
    @Transactional(readOnly = true)
    public List<Pago> getPendingRefunds() {
        return pagoRepository.findByIdEstadoPago(5);
    }

    /**
     * 3. REGISTRAR ABONO/PAGO: Registra un pago manual y actualiza el estado de la reserva si ya se pagó todo.
     */
    @Transactional
    public Pago registerPayment(RegisterPaymentRequest request, Integer idUsuarioActual) {
        Reserva reserva = reservaRepository.findById(request.idReserva())
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        // A. Guardar el nuevo pago
        Pago pago = new Pago();
        pago.setCodigoPago("PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pago.setIdReserva(request.idReserva());
        pago.setMonto(request.monto());
        pago.setIdMetodoPago(request.idMetodoPago());
        pago.setNumeroReferencia(request.referencia());
        pago.setIdEstadoPago(2); // 2 = APROBADO
        pago.setIdUsuarioRegistro(idUsuarioActual);
        pago.setObservaciones(request.observaciones() != null ? request.observaciones() : "Abono registrado por Tesorería.");

        Pago pagoGuardado = pagoRepository.save(pago);

        // B. Lógica de Negocio: Si la reserva estaba PENDIENTE (1) y con este pago el saldo llega a 0, la CONFIRMAMOS (2).
        EstadoCuentaView estadoCuenta = estadoCuentaViewRepository.findById(request.idReserva())
                .orElseThrow(() -> new IllegalArgumentException("Error al calcular el estado de cuenta"));

        // Si el saldo pendiente menos este nuevo pago es <= 0
        if (estadoCuenta.getSaldoPendiente().subtract(request.monto()).compareTo(BigDecimal.ZERO) <= 0) {
            if (reserva.getIdEstadoReserva() == 1) { // Si estaba PENDIENTE
                reserva.setIdEstadoReserva(2); // Pasa a CONFIRMADA
                reservaRepository.save(reserva);
            }
        }

        return pagoGuardado;
    }

    /**
     * 4. PROCESAR REEMBOLSO: Marca un reembolso pendiente como DEVUELTO (ID 6).
     */
    @Transactional
    public void processRefund(Integer idPago, Integer idUsuarioActual) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new IllegalArgumentException("Pago no encontrado"));

        if (pago.getIdEstadoPago() != 5) { // 5 = A_DEVOLVER
            throw new IllegalArgumentException("Este registro no es un reembolso pendiente.");
        }

        pago.setIdEstadoPago(6); // 6 = DEVUELTO
        pago.setObservaciones(pago.getObservaciones() + " | Reembolso procesado por Usuario ID: " + idUsuarioActual);
        pagoRepository.save(pago);
    }
}

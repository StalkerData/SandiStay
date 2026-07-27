package com.sandistay.backend.modules.bookings.service;

import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.bookings.dto.CancelBookingRequest;
import com.sandistay.backend.modules.bookings.dto.CancelBookingResponse;
import com.sandistay.backend.modules.bookings.dto.CreateBookingRequest;
import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.model.ReservaArea;
import com.sandistay.backend.modules.bookings.model.ReservaHabitacion;
import com.sandistay.backend.modules.bookings.model.ReservaTotalProjection;
import com.sandistay.backend.modules.bookings.repository.ReservaAreaRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaHabitacionRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.payment.client.PaymentClient;
import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import com.sandistay.backend.modules.payment.model.Pago;
import com.sandistay.backend.modules.payment.repository.PagoRepository;

import com.sandistay.backend.modules.rooms.model.Habitacion;
import com.sandistay.backend.modules.rooms.model.RoomAvailabilityProjection;
import com.sandistay.backend.modules.rooms.repository.CatalogRepository;
import com.sandistay.backend.modules.rooms.repository.HabitacionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaHabitacionRepository reservaHabitacionRepository;
    @Mock private ReservaAreaRepository reservaAreaRepository;
    @Mock private PagoRepository pagoRepository;
    @Mock private CatalogRepository catalogRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private HabitacionRepository habitacionRepository;
    @Mock private PaymentClient paymentClient;

    @InjectMocks
    private BookingService bookingService;

    private Reserva reservaMock;
    private Pago pagoMock;

    @BeforeEach
    void setUp() {
        reservaMock = new Reserva();
        reservaMock.setIdReserva(100);
        reservaMock.setCodigoReserva("RES-TEST01");
        reservaMock.setIdEstadoReserva(2); // Confirmada

        pagoMock = new Pago();
        pagoMock.setIdPago(1);
        pagoMock.setMonto(new BigDecimal("1000.00"));
        pagoMock.setIdEstadoPago(2); // Aprobado
        pagoMock.setIdMetodoPago(2); // Nequi
        pagoMock.setObservaciones("Pago OK");
    }

    @Test
    @DisplayName("UT-01: Cancelar Habitación con >= 5 días de anticipación (Multa 35%, Reembolso 65%)")
    void cancelRoom_WithMoreThan5Days_ShouldApply35PercentPenalty() {
        // Arrange
        reservaMock.setTipoReserva("Habitacion");
        CancelBookingRequest request = new CancelBookingRequest("Enfermedad");

        ReservaHabitacion detalle = new ReservaHabitacion();
        // Simulamos que el Check-in es en 6 días
        detalle.setFechaInicio(LocalDate.now().plusDays(6));

        when(reservaRepository.findById(100)).thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByIdReserva(100)).thenReturn(List.of(pagoMock));
        when(reservaHabitacionRepository.findByIdReserva(100)).thenReturn(List.of(detalle));

        // Act
        CancelBookingResponse response = bookingService.cancelBooking(100, request, 1);

        // Assert
        assertThat(response.nuevoEstado()).isEqualTo("CANCELADA");
        // 35% de 1000 = 350
        assertThat(response.montoMulta()).isEqualByComparingTo(new BigDecimal("350.00"));
        // 65% de 1000 = 650
        assertThat(response.montoReembolso()).isEqualByComparingTo(new BigDecimal("650.00"));

        // Verificamos que se llamó al SP de PostgreSQL
        verify(reservaRepository, times(1)).llamarSpCancelarReserva(100, "Enfermedad", 1);
    }

    @Test
    @DisplayName("UT-02: Cancelar Área Social con < 7 días de anticipación (Multa 100%, Reembolso 0%)")
    void cancelArea_WithLessThan7Days_ShouldApply100PercentPenalty() {
        // Arrange
        reservaMock.setTipoReserva("Area");
        CancelBookingRequest request = new CancelBookingRequest("Cambio de planes");

        ReservaArea detalle = new ReservaArea();
        // Simulamos que el evento es en 3 días
        detalle.setFechaEvento(LocalDate.now().plusDays(3));
        detalle.setPrecioHora(new BigDecimal("100.00"));
        detalle.setDuracionHoras(new BigDecimal("5.0")); // Total evento = 500

        // Simulamos que el cliente pagó 500
        pagoMock.setMonto(new BigDecimal("500.00"));

        when(reservaRepository.findById(100)).thenReturn(Optional.of(reservaMock));
        when(pagoRepository.findByIdReserva(100)).thenReturn(List.of(pagoMock));
        when(reservaAreaRepository.findByIdReserva(100)).thenReturn(List.of(detalle));

        // Act
        CancelBookingResponse response = bookingService.cancelBooking(100, request, 1);

        // Assert
        assertThat(response.nuevoEstado()).isEqualTo("CANCELADA");
        // Multa del 100% de lo pagado
        assertThat(response.montoMulta()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.montoReembolso()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("UT-03: Intento de creación de reserva con pago inferior al requerido (Fraude)")
    void createBooking_WithInsufficientPayment_ShouldThrowFraudException() {
        // Arrange
        CreateBookingRequest request = new CreateBookingRequest(
                "Habitacion", 1, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                null, null, 2, 0, "John", "Doe", 1, "123456", "john@test.com", "555", 1, false, "TX-FRAUD"
        );

        // Mock Disponibilidad
        RoomAvailabilityProjection roomProj = mock(RoomAvailabilityProjection.class);
        when(roomProj.getIdHabitacion()).thenReturn(1);
        when(catalogRepository.findAvailableRooms(any(), any(), any(), any(), any())).thenReturn(List.of(roomProj));

        // Mock Cliente
        when(personaRepository.findByNumeroDocumento("123456")).thenReturn(Optional.empty());
        Persona p = new Persona(); p.setIdPersona(1);
        when(personaRepository.save(any())).thenReturn(p);
        Cliente c = new Cliente(); c.setIdCliente(1);
        when(clienteRepository.save(any())).thenReturn(c);

        // Mock Reserva y Detalle
        when(reservaRepository.save(any())).thenReturn(reservaMock);
        Habitacion h = new Habitacion(); h.setPrecioNoche(new BigDecimal("500.00"));
        when(habitacionRepository.findById(1)).thenReturn(Optional.of(h));

        // Mock Cálculo de Totales (La reserva cuesta $1000)
        ReservaTotalProjection totalProj = mock(ReservaTotalProjection.class);
        when(totalProj.getTotalFinal()).thenReturn(new BigDecimal("1000.00"));
        when(reservaRepository.calcularTotalReserva(100)).thenReturn(totalProj);

        // Mock Banco: El cliente alteró el frontend y el banco dice que solo transfirió $100
        PaymentVerificationResponse bankResponse = new PaymentVerificationResponse(
                true, "TX-FRAUD", "2026-07-20", new BigDecimal("100.00"), "COP", null, null, "OK"
        );
        when(paymentClient.verifyPayment("TX-FRAUD")).thenReturn(bankResponse);

        // Act & Assert
        assertThatThrownBy(() -> bookingService.createBooking(request, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fraude detectado")
                .hasMessageContaining("requiere $1000.00");
    }
}

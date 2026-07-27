package com.sandistay.backend.modules.bookings;

import com.sandistay.backend.AbstractIntegrationTest;
import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.bookings.service.NightAuditService;
import com.sandistay.backend.modules.payment.client.PaymentClient;
import com.sandistay.backend.modules.payment.dto.PaymentVerificationResponse;
import jakarta.persistence.EntityManager;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PaymentClient paymentClient;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private NightAuditService nightAuditService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("IT-03: PaymentClient debe soportar caídas del banco (Error 500) y retornar found=false")
    void paymentClient_WhenBankReturns500_ShouldHandleGracefully() {
        mockBackEnd.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        PaymentVerificationResponse response = paymentClient.verifyPayment("TX-12345");

        assertThat(response.found()).isFalse();
        assertThat(response.message()).contains("Error de conexión con la red bancaria");
    }

    @Test
    @DisplayName("IT-04: NightAuditService debe cancelar reservas PENDIENTES con más de 24h")
    void nightAudit_ShouldCancelExpiredPendingBookings() {

        // 1. PRE-ARRANGE: Creamos el ecosistema completo para que el SP no falle.
        // Insertamos un Cliente
        jdbcTemplate.execute("INSERT INTO cliente (idpersona, codigocliente, idtipocliente) VALUES (1, 'CLI-TEST-01', 1) ON CONFLICT DO NOTHING");

        // Insertamos una Habitación de prueba
        jdbcTemplate.execute("INSERT INTO habitacion (codigo, numero, nombre, idtipohabitacion, capacidadadultos, capacidadmaxima, idestadolugar, precionoche, preciodia) " +
                             "VALUES ('H999', '999', 'Test Room', 1, 2, 2, 1, 100.0, 100.0) ON CONFLICT DO NOTHING");

        // 2. Insertamos la Reserva forzando la fecha de creación a hace 25 horas
        jdbcTemplate.update(
            "INSERT INTO reserva (codigoreserva, tiporeserva, idcliente, nombrehuesped, documentohuesped, idestadoreserva, idusuariocreacion, fechacreacion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            "RES-EXPIRED", "Habitacion", 1, "Test Huesped", "123456", 1, 1, LocalDateTime.now().minusHours(25)
        );

        // Obtenemos los IDs generados
        Integer idReserva = jdbcTemplate.queryForObject("SELECT idreserva FROM reserva WHERE codigoreserva = 'RES-EXPIRED'", Integer.class);
        Integer idHabitacion = jdbcTemplate.queryForObject("SELECT idhabitacion FROM habitacion WHERE codigo = 'H999'", Integer.class);

        // 3. Insertamos el Detalle de la Reserva (Vital para que el SP funcione)
        jdbcTemplate.update(
            "INSERT INTO reservahabitacion (idreserva, idhabitacion, fechainicio, fechafin, numeronoches, numerodias, precionoche, preciodia) " +
            "VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + 1, 1, 0, 100.0, 0.0)",
            idReserva, idHabitacion
        );

        // 4. ACT: Ejecutamos el Cron Job manualmente
        nightAuditService.limpiarReservasExpiradas();

        // 5. ASSERT: Verificamos que el SP hizo su trabajo y la cambió a CANCELADA (Estado 5)
        Integer estadoActual = jdbcTemplate.queryForObject(
            "SELECT idestadoreserva FROM reserva WHERE idreserva = ?",
            Integer.class,
            idReserva
        );

        assertThat(estadoActual).isEqualTo(5); // 5 = CANCELADA
    }
}

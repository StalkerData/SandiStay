package com.sandistay.backend.modules.bookings.repository;

import com.sandistay.backend.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// CRÍTICO: Añadimos @Transactional porque estamos llamando a un SP que modifica datos directamente desde el test
@Transactional
class ReservaRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("IT-01: Ejecución del SP sp_cancelar_reserva cambia estado a CANC y libera habitación")
    void spCancelarReserva_ShouldChangeStatusAndFreeRoom() {
        // 1. ARRANGE: Preparamos el ecosistema usando SQL nativo
        jdbcTemplate.execute("INSERT INTO cliente (idpersona, codigocliente, idtipocliente) VALUES (1, 'CLI-SP-01', 1) ON CONFLICT DO NOTHING");

        // Insertamos una habitación y la forzamos a estado OCUPADA (2)
        jdbcTemplate.execute("INSERT INTO habitacion (codigo, numero, nombre, idtipohabitacion, capacidadadultos, capacidadmaxima, idestadolugar, precionoche, preciodia) " +
                             "VALUES ('H-SP', '101', 'Room SP', 1, 2, 2, 2, 100.0, 100.0) ON CONFLICT DO NOTHING");

        // Insertamos una Reserva en estado CONFIRMADA (2)
        jdbcTemplate.update("INSERT INTO reserva (codigoreserva, tiporeserva, idcliente, nombrehuesped, documentohuesped, idestadoreserva, idusuariocreacion) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                            "RES-SP-01", "Habitacion", 1, "Huesped SP", "111111", 2, 1);

        Integer idReserva = jdbcTemplate.queryForObject("SELECT idreserva FROM reserva WHERE codigoreserva = 'RES-SP-01'", Integer.class);
        Integer idHabitacion = jdbcTemplate.queryForObject("SELECT idhabitacion FROM habitacion WHERE codigo = 'H-SP'", Integer.class);

        // Insertamos el Detalle de la reserva
        jdbcTemplate.update("INSERT INTO reservahabitacion (idreserva, idhabitacion, fechainicio, fechafin, numeronoches, numerodias, precionoche, preciodia) " +
                            "VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + 1, 1, 0, 100.0, 0.0)",
                            idReserva, idHabitacion);

        // 2. ACT: Llamamos al Procedimiento Almacenado a través del repositorio
        reservaRepository.llamarSpCancelarReserva(idReserva, "Prueba de Integración SP", 1);

        // 3. ASSERT: Verificamos que la reserva pasó a CANCELADA (5)
        Integer estadoReserva = jdbcTemplate.queryForObject("SELECT idestadoreserva FROM reserva WHERE idreserva = ?", Integer.class, idReserva);
        assertThat(estadoReserva).isEqualTo(5); // 5 = CANCELADA

        // 4. ASSERT: Verificamos que el SP liberó la habitación pasándola a DISPONIBLE (1)
        Integer estadoHabitacion = jdbcTemplate.queryForObject("SELECT idestadolugar FROM habitacion WHERE idhabitacion = ?", Integer.class, idHabitacion);
        assertThat(estadoHabitacion).isEqualTo(1); // 1 = DISPONIBLE
    }
}

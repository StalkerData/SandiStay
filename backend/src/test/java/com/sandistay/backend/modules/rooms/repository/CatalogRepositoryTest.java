package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.AbstractIntegrationTest;
import com.sandistay.backend.modules.rooms.model.RoomAvailabilityProjection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("IT-02: Consulta de disponibilidad filtra correctamente las habitaciones ocupadas")
    void findAvailableRooms_ShouldFilterOverlappingBookings() {
        // 1. ARRANGE: Insertamos una habitación de prueba
        jdbcTemplate.execute("INSERT INTO habitacion (codigo, numero, nombre, idtipohabitacion, capacidadadultos, capacidadmaxima, idestadolugar, precionoche, preciodia) " +
                             "VALUES ('H-CAT', '202', 'Room Catalog', 1, 2, 2, 1, 100.0, 100.0) ON CONFLICT DO NOTHING");

        Integer idHabitacion = jdbcTemplate.queryForObject("SELECT idhabitacion FROM habitacion WHERE codigo = 'H-CAT'", Integer.class);

        // Insertamos cliente y reserva
        jdbcTemplate.execute("INSERT INTO cliente (idpersona, codigocliente, idtipocliente) VALUES (1, 'CLI-CAT-01', 1) ON CONFLICT DO NOTHING");

        jdbcTemplate.update("INSERT INTO reserva (codigoreserva, tiporeserva, idcliente, nombrehuesped, documentohuesped, idestadoreserva, idusuariocreacion) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                            "RES-CAT-01", "Habitacion", 1, "Huesped Cat", "222222", 2, 1);

        Integer idReserva = jdbcTemplate.queryForObject("SELECT idreserva FROM reserva WHERE codigoreserva = 'RES-CAT-01'", Integer.class);

        // Ocupamos la habitación del día 10 al día 15 del mes actual
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(15);

        jdbcTemplate.update("INSERT INTO reservahabitacion (idreserva, idhabitacion, fechainicio, fechafin, numeronoches, numerodias, precionoche, preciodia) " +
                            "VALUES (?, ?, ?, ?, 5, 0, 100.0, 0.0)",
                            idReserva, idHabitacion, checkIn, checkOut);

        // 2. ACT 1: Buscar disponibilidad en fechas que COLISIONAN (Día 12 al 14)
        List<RoomAvailabilityProjection> colision = catalogRepository.findAvailableRooms(
                checkIn.plusDays(2), checkOut.minusDays(1), 1, 0, null
        );

        // 3. ASSERT 1: La habitación H-CAT NO debe estar en la lista porque está ocupada
        boolean isRoomAvailableDuringCollision = colision.stream()
                .anyMatch(room -> room.getIdHabitacion().equals(idHabitacion));
        assertThat(isRoomAvailableDuringCollision).isFalse();

        // 4. ACT 2: Buscar disponibilidad en fechas LIBRES (Día 20 al 25)
        List<RoomAvailabilityProjection> libres = catalogRepository.findAvailableRooms(
                checkIn.plusDays(10), checkOut.plusDays(10), 1, 0, null
        );

        // 5. ASSERT 2: La habitación H-CAT SÍ debe estar en la lista porque está libre
        boolean isRoomAvailableWhenFree = libres.stream()
                .anyMatch(room -> room.getIdHabitacion().equals(idHabitacion));
        assertThat(isRoomAvailableWhenFree).isTrue();
    }
}

package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.finance.repository.FacturaRepository;
import com.sandistay.backend.modules.finance.repository.ReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    // 1. Mockeamos TODAS las dependencias (No necesitamos BD real)
    @Mock private FacturaRepository facturaRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private ReportRepository reportRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private PersonaRepository personaRepository;
    @Mock private DataSource dataSource;

    // 2. Inyectamos los mocks en el servicio que vamos a probar
    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("UT-05: Generación de PDF con código de reserva inválido debe lanzar IllegalArgumentException")
    void generarFacturaPdf_WithInvalidCodigoReserva_ShouldThrowIllegalArgumentException() {

        // Arrange: Le decimos a Mockito que cuando el servicio intente buscar la reserva "RES-INVALIDA",
        // simule que la base de datos devolvió un resultado vacío (Optional.empty).
        String codigoInvalido = "RES-INVALIDA";
        Integer idUsuarioAdmin = 1;

        when(reservaRepository.findByCodigoReserva(codigoInvalido)).thenReturn(Optional.empty());

        // Act & Assert: Verificamos que el servicio atrape el vacío y lance la excepción correcta
        // ANTES de intentar invocar a JasperReports.
        assertThatThrownBy(() -> reportService.generarFacturaPdf(codigoInvalido, idUsuarioAdmin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró ninguna reserva con el código: " + codigoInvalido);
    }

    @Test
    @DisplayName("UT-05.1: Generación de PDF con código nulo debe lanzar IllegalArgumentException")
    void generarFacturaPdf_WithNullCodigoReserva_ShouldThrowIllegalArgumentException() {

        // Arrange: Simulamos el comportamiento para un valor nulo
        Integer idUsuarioAdmin = 1;
        when(reservaRepository.findByCodigoReserva(null)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reportService.generarFacturaPdf(null, idUsuarioAdmin))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontró ninguna reserva con el código: null");
    }
}

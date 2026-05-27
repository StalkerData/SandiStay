package com.sandistay.backend.modules.bookings.service;

import com.sandistay.backend.modules.bookings.model.Reserva;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NightAuditService {
    
    private static final Logger log = LoggerFactory.getLogger(NightAuditService.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ReservaRepository reservaRepository;

    public NightAuditService(JdbcTemplate jdbcTemplate, ReservaRepository reservaRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservaRepository = reservaRepository;
    }

    /**
     * 1. PROCESO DE NO-SHOW
     * Se ejecuta todos los días a las 00:01 AM.
     * Marca como No-Show a los clientes que no llegaron en su fecha de Check-in.
     */
    @Scheduled(cron = "0 1 0 * * ?")
    public void ejecutarCierreDiario() {
        log.info("Iniciando proceso automático de Cierre Diario (Night Audit)...");
        jdbcTemplate.execute("CALL sp_cierre_diario_noshow()");
        log.info("Cierre Diario finalizado con éxito.");
    }

    /**
     * 2. PROCESO DE LIMPIEZA DE RESERVAS NO PAGADAS (24H)
     * Se ejecuta cada 12 horas (a las 00:00 y a las 12:00).
     * Cancela las reservas PENDIENTES que superaron las 24 horas sin registrar pago.
     */
    @Scheduled(cron = "0 0 0,12 * * ?")
    @Transactional
    public void limpiarReservasExpiradas() {
        log.info("Iniciando limpieza de reservas pendientes sin pago (>24h)...");
        
        // Calculamos la fecha límite (hace exactamente 24 horas)
        LocalDateTime limite24Horas = LocalDateTime.now().minusHours(24);
        
        // Buscamos las reservas en estado 1 (PENDIENTE) creadas antes de ese límite
        List<Reserva> expiradas = reservaRepository.findByIdEstadoReservaAndFechaCreacionBefore(1, limite24Horas);
        
        if (expiradas.isEmpty()) {
            log.info("No se encontraron reservas expiradas para limpiar.");
            return;
        }

        // Cancelamos cada reserva usando nuestro Procedimiento Almacenado
        for (Reserva reserva : expiradas) {
            try {
                // Usamos el ID 1 (Admin/Sistema) como el usuario que ejecuta la acción
                reservaRepository.llamarSpCancelarReserva(
                        reserva.getIdReserva(), 
                        "Cancelación automática: Tiempo de pago (24h) expirado.", 
                        1 
                );
                log.info("Reserva {} cancelada automáticamente por falta de pago.", reserva.getCodigoReserva());
            } catch (Exception e) {
                log.error("Error al cancelar automáticamente la reserva {}: {}", reserva.getCodigoReserva(), e.getMessage());
            }
        }
        
        log.info("Limpieza finalizada. Total de reservas canceladas: {}", expiradas.size());
    }
}
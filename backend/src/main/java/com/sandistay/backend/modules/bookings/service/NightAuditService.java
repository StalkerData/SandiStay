package com.sandistay.backend.modules.bookings.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NightAuditService {
    private static final Logger log = LoggerFactory.getLogger(NightAuditService.class);
    private final JdbcTemplate jdbcTemplate;

    public NightAuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Se ejecuta todos los días a las 00:01 AM
    @Scheduled(cron = "0 1 0 * * ?")
    public void ejecutarCierreDiario() {
        log.info("Iniciando proceso automático de Cierre Diario (Night Audit)...");
        jdbcTemplate.execute("CALL sp_cierre_diario_noshow()");
        log.info("Cierre Diario finalizado con éxito.");
    }
}
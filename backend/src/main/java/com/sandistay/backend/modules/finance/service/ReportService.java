package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.bookings.repository.ReservaRepository;
import com.sandistay.backend.modules.finance.model.Factura;
import com.sandistay.backend.modules.finance.model.HistorialClienteProjection;
import com.sandistay.backend.modules.finance.model.OcupacionProjection;
import com.sandistay.backend.modules.finance.repository.FacturaRepository;
import com.sandistay.backend.modules.finance.repository.ReportRepository;
import net.sf.jasperreports.engine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final FacturaRepository facturaRepository;
    private final ReservaRepository reservaRepository;
    private final ReportRepository reportRepository;
    private final ClienteRepository clienteRepository;
    private final DataSource dataSource;

    public ReportService(FacturaRepository facturaRepository, ReservaRepository reservaRepository, 
                         ReportRepository reportRepository, ClienteRepository clienteRepository, DataSource dataSource) {
        this.facturaRepository = facturaRepository;
        this.reservaRepository = reservaRepository;
        this.reportRepository = reportRepository;
        this.clienteRepository = clienteRepository;
        this.dataSource = dataSource;
    }

    @Transactional
    public byte[] generarFacturaPdf(String codigoReserva, Integer idUsuarioActual) {
        log.info("Iniciando generación de factura para la reserva: {}", codigoReserva);
        Integer idReserva = reservaRepository.findByCodigoReserva(codigoReserva)
                .orElseThrow(() -> new RuntimeException("No se encontró ninguna reserva con el código: " + codigoReserva))
                .getIdReserva();

        facturaRepository.generarFactura(idReserva, idUsuarioActual);

        Factura factura = facturaRepository.findFirstByIdReservaOrderByFechaCreacionDesc(idReserva)
                .orElseThrow(() -> new RuntimeException("La factura no se generó correctamente en la base de datos."));
        
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("P_ID_FACTURA", factura.getIdFactura());

        return compilarYGenerarPdfConParametros("reports/factura.jrxml", parametros);
    }

    @Transactional(readOnly = true)
    public byte[] generarReporteOcupacionPdf(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("P_FECHA_INICIO", java.sql.Date.valueOf(fechaInicio));
        parametros.put("P_FECHA_FIN", java.sql.Date.valueOf(fechaFin));
        return compilarYGenerarPdfConParametros("reports/reporte_ocupacion.jrxml", parametros);
    }

    private byte[] compilarYGenerarPdfConParametros(String rutaJrxml, Map<String, Object> parametros) {
        try {
            ClassPathResource resource = new ClassPathResource(rutaJrxml);
            if (!resource.exists()) throw new RuntimeException("El archivo de plantilla no existe: " + rutaJrxml);

            InputStream reportStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Connection connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(dataSource);
            try {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } finally {
                org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(connection, dataSource);
            }
        } catch (Exception e) {
            log.error("Error crítico al compilar JasperReports:", e);
            throw new RuntimeException("No se pudo generar el reporte PDF.");
        }
    }

    public List<OcupacionProjection> getOccupancyReport(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now().plusDays(7);
        return reportRepository.getReporteOcupacion(fechaInicio, fechaFin);
    }

    public HistorialClienteProjection getClientHistory(Integer idCliente) {
        HistorialClienteProjection historial = reportRepository.getHistorialCliente(idCliente);
        if (historial == null) throw new RuntimeException("No se encontró historial para el cliente especificado.");
        return historial;
    }

    // NUEVO: Método para que el Admin cambie la categoría del cliente manualmente
    @Transactional
    public void updateClientCategory(Integer idCliente, Integer idTipoCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setIdTipoCliente(idTipoCliente);
        clienteRepository.save(cliente);
        log.info("Categoría del cliente {} actualizada a {}", idCliente, idTipoCliente);
    }
}
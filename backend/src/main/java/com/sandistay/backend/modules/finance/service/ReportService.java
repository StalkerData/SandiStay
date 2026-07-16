package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.auth.model.Cliente;
import com.sandistay.backend.modules.auth.model.Persona;
import com.sandistay.backend.modules.auth.repository.ClienteRepository;
import com.sandistay.backend.modules.auth.repository.PersonaRepository;
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

/**
 * Servicio de Reportes y Facturación.
 * Integra JasperReports para la generación de documentos PDF y maneja las consultas analíticas.
 */
@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final FacturaRepository facturaRepository;
    private final ReservaRepository reservaRepository;
    private final ReportRepository reportRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final DataSource dataSource;

    // Constructor manual explícito
    public ReportService(FacturaRepository facturaRepository,
                         ReservaRepository reservaRepository,
                         ReportRepository reportRepository,
                         ClienteRepository clienteRepository,
                         PersonaRepository personaRepository,
                         DataSource dataSource) {
        this.facturaRepository = facturaRepository;
        this.reservaRepository = reservaRepository;
        this.reportRepository = reportRepository;
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.dataSource = dataSource;
    }

    @Transactional
    public byte[] generarFacturaPdf(String codigoReserva, Integer idUsuarioActual) {
        log.info("Iniciando generación de factura para la reserva: {}", codigoReserva);

        Integer idReserva = reservaRepository.findByCodigoReserva(codigoReserva)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ninguna reserva con el código: " + codigoReserva))
                .getIdReserva();

        // Llama al Procedimiento Almacenado para generar la factura en BD
        facturaRepository.generarFactura(idReserva, idUsuarioActual);

        Factura factura = facturaRepository.findFirstByIdReservaOrderByFechaCreacionDesc(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("La factura no se generó correctamente en la base de datos."));

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

    /**
     * Compila y llena un reporte JasperReports.
     * CORRECCIÓN: Usamos DataSourceUtils para obtener la conexión atada a la transacción actual,
     * permitiendo que JasperReports lea los datos antes del COMMIT final.
     */
    private byte[] compilarYGenerarPdfConParametros(String rutaJrxml, Map<String, Object> parametros) {
        try {
            ClassPathResource resource = new ClassPathResource(rutaJrxml);
            if (!resource.exists()) {
                throw new IllegalArgumentException("El archivo de plantilla no existe: " + rutaJrxml);
            }

            InputStream reportStream = resource.getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // Obtenemos la conexión transaccional de Spring
            Connection connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(dataSource);
            try {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } finally {
                // Liberamos la conexión correctamente a través de Spring
                org.springframework.jdbc.datasource.DataSourceUtils.releaseConnection(connection, dataSource);
                reportStream.close();
            }
        } catch (Exception e) {
            log.error("Error crítico al compilar JasperReports:", e);
            throw new IllegalArgumentException("No se pudo generar el reporte PDF. Verifique los logs del servidor.");
        }
    }

    @Transactional(readOnly = true)
    public List<OcupacionProjection> getOccupancyReport(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null) fechaInicio = LocalDate.now();
        if (fechaFin == null) fechaFin = LocalDate.now().plusDays(7);
        return reportRepository.getReporteOcupacion(fechaInicio, fechaFin);
    }

    @Transactional(readOnly = true)
    public HistorialClienteProjection getClientHistory(Integer idCliente) {
        HistorialClienteProjection historial = reportRepository.getHistorialCliente(idCliente);
        if (historial == null) {
            throw new IllegalArgumentException("No se encontró historial para el cliente especificado.");
        }
        return historial;
    }

    @Transactional
    public void updateClientCategory(Integer idCliente, Integer idTipoCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setIdTipoCliente(idTipoCliente);
        clienteRepository.save(cliente);
        log.info("Categoría del cliente {} actualizada a {}", idCliente, idTipoCliente);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchClientByDocument(String documento) {
        Persona persona = personaRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró ninguna persona con ese documento."));

        Cliente cliente = clienteRepository.findByIdPersona(persona.getIdPersona())
                .orElseThrow(() -> new IllegalArgumentException("La persona existe pero no está registrada como cliente."));

        Map<String, Object> response = new HashMap<>();
        response.put("idCliente", cliente.getIdCliente());
        response.put("codigoCliente", cliente.getCodigoCliente());
        response.put("idTipoCliente", cliente.getIdTipoCliente());
        response.put("nombreCompleto", persona.getNombres() + " " + persona.getApellidos());

        return response;
    }
}

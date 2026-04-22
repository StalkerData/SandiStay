package com.sandistay.backend.modules.finance.service;

import com.sandistay.backend.modules.finance.repository.FacturaRepository;
import net.sf.jasperreports.engine.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final FacturaRepository facturaRepository;
    private final DataSource dataSource;

    /**
     * Inyectamos el repositorio para generar la factura en BD y el DataSource 
     * para que JasperReports pueda ejecutar su propio SQL.
     */
    public ReportService(FacturaRepository facturaRepository, DataSource dataSource) {
        this.facturaRepository = facturaRepository;
        this.dataSource = dataSource;
    }

    /**
     * Orquesta la generación de una factura: primero en Base de Datos y luego en PDF.
     * 
     * @param idReserva El ID de la reserva a facturar.
     * @param idUsuarioActual El ID del Staff que genera la factura.
     * @return Un arreglo de bytes que representa el archivo PDF.
     */
    @Transactional
    public byte[] generarFacturaPdf(Integer idReserva, Integer idUsuarioActual) {
        log.info("Iniciando generación de factura para la reserva ID: {}", idReserva);

        // 1. Llamar al Procedimiento Almacenado para crear los registros en BD
        // El SP devuelve el ID de la factura recién creada (parámetro OUT)
        Integer idFacturaGenerada = facturaRepository.generarFactura(idReserva, idUsuarioActual);
        log.info("Factura generada en BD con ID: {}", idFacturaGenerada);

        // 2. Generar el PDF usando JasperReports
        return compilarYGenerarPdf("reports/factura.jrxml", idFacturaGenerada);
    }

    /**
     * Método privado reutilizable para compilar cualquier reporte Jasper.
     *
     * @param rutaJrxml  Ruta relativa al classpath (ej: "reports/factura.jrxml")
     * @param idFactura  Parámetro que se le pasa al reporte para filtrar datos
     * @return           Bytes del PDF generado
     */
    private byte[] compilarYGenerarPdf(String rutaJrxml, Integer idFactura) {
        try {
            // 1. Cargar el archivo .jrxml desde resources/ como InputStream
            InputStream reportStream = new ClassPathResource(rutaJrxml).getInputStream();

            // 2. Compilar el .jrxml en memoria → produce un JasperReport (el "molde" compilado)
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 3. Armar el mapa de parámetros que recibirá la plantilla
            //    En tu .jrxml debes declarar un parámetro llamado exactamente "ID_FACTURA"
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("P_ID_FACTURA", idFactura);

         // 4. Obtener la conexión TRANSACCIONAL actual de Spring (No una nueva)
            Connection connection = DataSourceUtils.getConnection(dataSource);
            try {
                // 5. Llenar el reporte
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, connection);

                // 6. Exportar a PDF
                return JasperExportManager.exportReportToPdf(jasperPrint);
            } finally {
                // Liberar la conexión de vuelta a Spring
                DataSourceUtils.releaseConnection(connection, dataSource);
            }

        } catch (Exception e) {
            log.error("Error al generar el PDF para el reporte '{}': {}", rutaJrxml, e.getMessage(), e);
            throw new RuntimeException("No se pudo generar el reporte PDF: " + rutaJrxml, e);
        }
    }
}
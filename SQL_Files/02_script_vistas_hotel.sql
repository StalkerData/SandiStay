-- ============================================
-- VISTAS ESENCIALES - SISTEMA HOTELERO (OPTIMIZADAS)
-- ============================================

-- ============================================
-- 1. VISTA: Información Completa de Clientes
-- Justificación: Evita múltiples JOINs repetitivos en consultas de clientes
-- ============================================
CREATE OR REPLACE VIEW v_clientes_completo AS
SELECT 
    c.idCliente,
    c.codigoCliente,
    p.idPersona,
    p.nombres,
    p.apellidos,
    p.nombres || ' ' || p.apellidos AS nombreCompleto,
    td.codigo AS tipoDocumentoCodigo,
    td.nombre AS tipoDocumento,
    p.numeroDocumento,
    p.email,
    p.telefono,
    p.direccion,
    p.ciudad,
    p.departamento,
    pa.nombre AS pais,
    pa.codigoTelefono,
    tc.codigo AS tipoClienteCodigo,
    tc.nombre AS tipoCliente,
    tc.porcentajeDescuento,
    ep.codigo AS estadoCodigo,
    ep.nombre AS estado,
    c.fechaRegistro
FROM Cliente c
INNER JOIN Persona p ON c.idPersona = p.idPersona
INNER JOIN TipoDocumento td ON p.idTipoDocumento = td.idTipoDocumento
INNER JOIN Pais pa ON p.idPais = pa.idPais
INNER JOIN TipoCliente tc ON c.idTipoCliente = tc.idTipoCliente
INNER JOIN EstadoPersona ep ON p.idEstadoPersona = ep.idEstado;

-- ============================================
-- 2. VISTA: Disponibilidad de Habitaciones
-- Justificación: Consulta crítica para el sistema de reservas
-- ============================================
CREATE OR REPLACE VIEW v_habitaciones_disponibles AS
SELECT 
    h.idHabitacion,
    h.codigo,
    h.numero,
    h.nombre,
    th.codigo AS tipoHabitacionCodigo,
    th.nombre AS tipoHabitacion,
    h.capacidadAdultos,
    h.capacidadNinos,
    h.capacidadMaxima,
    el.codigo AS estadoCodigo,
    el.nombre AS estado,
    el.color AS estadoColor,
    h.precioNoche,
    h.precioDia,
    h.descripcion,
    h.activo,
    CASE 
        WHEN el.codigo = 'DISP' AND h.activo = true THEN true
        ELSE false
    END AS disponibleReserva
FROM Habitacion h
INNER JOIN TipoHabitacion th ON h.idTipoHabitacion = th.idTipoHabitacion
INNER JOIN EstadoLugar el ON h.idEstadoLugar = el.idEstadoLugar;

-- ============================================
-- 3. VISTA: Reservas con Detalles Completos
-- Justificación: Dashboard principal, consulta más frecuente del sistema
-- ============================================
CREATE OR REPLACE VIEW v_reservas_completo AS
SELECT 
    r.idReserva,
    r.codigoReserva,
    r.tipoReserva,
    c.codigoCliente,
    p.nombres || ' ' || p.apellidos AS nombreCliente,
    p.email AS emailCliente,
    p.telefono AS telefonoCliente,
    r.nombreHuesped,
    r.documentoHuesped,
    r.telefonoHuesped,
    r.fechaReserva,
    r.numeroAdultos,
    r.numeroNinos,
    er.codigo AS estadoCodigo,
    er.nombre AS estado,
    er.color AS estadoColor,
    r.motivoCancelacion,
    r.fechaCancelacion,
    r.observaciones,
    u.username AS usuarioCreacion,
    r.fechaCreacion
FROM Reserva r
INNER JOIN Cliente c ON r.idCliente = c.idCliente
INNER JOIN Persona p ON c.idPersona = p.idPersona
INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
INNER JOIN Usuario u ON r.idUsuarioCreacion = u.idUsuario;

-- ============================================
-- 4. VISTA: Reservas de Habitaciones con Cálculos
-- Justificación: Incluye cálculos de costos, evita lógica repetitiva
-- ============================================
CREATE OR REPLACE VIEW v_reservas_habitaciones_detalle AS
SELECT 
    rh.idReservaHabitacion,
    r.idReserva,
    r.codigoReserva,
    h.idHabitacion,
    h.codigo AS codigoHabitacion,
    h.numero AS numeroHabitacion,
    h.nombre AS nombreHabitacion,
    rh.fechaInicio,
    rh.fechaFin,
    rh.numeroNoches,
    rh.numeroDias,
    rh.precioNoche,
    rh.precioDia,
    (rh.numeroNoches * rh.precioNoche) AS totalNoches,
    (rh.numeroDias * rh.precioDia) AS totalDias,
    (rh.numeroNoches * rh.precioNoche) + (rh.numeroDias * rh.precioDia) AS totalReserva,
    rh.activo,
    er.codigo AS estadoReserva,
    er.nombre AS estadoReservaNombre
FROM ReservaHabitacion rh
INNER JOIN Reserva r ON rh.idReserva = r.idReserva
INNER JOIN Habitacion h ON rh.idHabitacion = h.idHabitacion
INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva;

-- ============================================
-- 5. VISTA: Reservas de Áreas con Cálculos
-- Justificación: Similar a habitaciones, centraliza cálculos
-- ============================================
CREATE OR REPLACE VIEW v_reservas_areas_detalle AS
SELECT 
    ra.idReservaArea,
    r.idReserva,
    r.codigoReserva,
    a.idArea,
    a.codigo AS codigoArea,
    a.nombre AS nombreArea,
    ta.nombre AS tipoArea,
    ra.fechaEvento,
    ra.horaInicio,
    ra.horaFin,
    ra.duracionHoras,
    ra.precioHora,
    (ra.duracionHoras * ra.precioHora) AS totalReserva,
    ra.cantidadAsistentes,
    ra.requiereAnticipo,
    ra.porcentajeAnticipo,
    CASE 
        WHEN ra.requiereAnticipo THEN 
            ((ra.duracionHoras * ra.precioHora) * ra.porcentajeAnticipo / 100)
        ELSE 0
    END AS montoAnticipo,
    ra.activo,
    er.codigo AS estadoReserva,
    er.nombre AS estadoReservaNombre
FROM ReservaArea ra
INNER JOIN Reserva r ON ra.idReserva = r.idReserva
INNER JOIN Area a ON ra.idArea = a.idArea
INNER JOIN TipoArea ta ON a.idTipoArea = ta.idTipoArea
INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva;

-- ============================================
-- 6. VISTA: Estado de Cuenta por Reserva (OPTIMIZADA)
-- Justificación: Crucial para contabilidad, muestra pagos vs total
-- Optimización: Usa LEFT JOIN con agregaciones en lugar de subconsultas
-- ============================================
CREATE OR REPLACE VIEW v_estado_cuenta_reservas AS
WITH totales_habitaciones AS (
    SELECT 
        idReserva,
        SUM((numeroNoches * precioNoche) + (numeroDias * precioDia)) AS total
    FROM ReservaHabitacion
    WHERE activo = true
    GROUP BY idReserva
),
totales_areas AS (
    SELECT 
        idReserva,
        SUM(duracionHoras * precioHora) AS total
    FROM ReservaArea
    WHERE activo = true
    GROUP BY idReserva
),
totales_pagos AS (
    SELECT 
        p.idReserva,
        SUM(p.monto) AS totalPagado
    FROM Pago p
    INNER JOIN EstadoPago ep ON p.idEstadoPago = ep.idEstadoPago
    WHERE ep.codigo = 'APRO'
    GROUP BY p.idReserva
)
SELECT 
    r.idReserva,
    r.codigoReserva,
    r.tipoReserva,
    c.codigoCliente,
    p.nombres || ' ' || p.apellidos AS nombreCliente,
    COALESCE(th.total, 0) + COALESCE(ta.total, 0) AS totalReserva,
    COALESCE(tp.totalPagado, 0) AS totalPagado,
    (COALESCE(th.total, 0) + COALESCE(ta.total, 0)) - COALESCE(tp.totalPagado, 0) AS saldoPendiente,
    er.codigo AS estadoReserva,
    er.nombre AS estadoReservaNombre
FROM Reserva r
INNER JOIN Cliente c ON r.idCliente = c.idCliente
INNER JOIN Persona p ON c.idPersona = p.idPersona
INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
LEFT JOIN totales_habitaciones th ON r.idReserva = th.idReserva
LEFT JOIN totales_areas ta ON r.idReserva = ta.idReserva
LEFT JOIN totales_pagos tp ON r.idReserva = tp.idReserva;

-- ============================================
-- 7. VISTA: Usuarios con Roles y Permisos
-- Justificación: Seguridad, verificación rápida de accesos
-- ============================================
CREATE OR REPLACE VIEW v_usuarios_roles AS
SELECT 
    u.idUsuario,
    u.username,
    p.nombres || ' ' || p.apellidos AS nombreCompleto,
    p.email,
    r.codigo AS rolCodigo,
    r.nombre AS rol,
    r.nivelAcceso,
    ur.fechaAsignacion,
    u.activo AS usuarioActivo,
    u.bloqueado,
    u.ultimoLogin
FROM Usuario u
INNER JOIN Persona p ON u.idPersona = p.idPersona
INNER JOIN UsuarioRol ur ON u.idUsuario = ur.idUsuario
INNER JOIN Rol r ON ur.idRol = r.idRol;

-- ============================================
-- 8. VISTA: Ocupación de Habitaciones (OPTIMIZADA)
-- Justificación: Reporte más solicitado por administración
-- Optimización: Usa LEFT JOIN en lugar de EXISTS para mejor rendimiento
-- Nota: CURRENT_DATE >= fechaInicio AND CURRENT_DATE < fechaFin
--       significa que el día de check-out la habitación NO cuenta como ocupada
--       (disponible para nueva reserva nocturna después de limpieza)
-- ============================================
CREATE OR REPLACE VIEW v_ocupacion_habitaciones AS
WITH reservas_activas AS (
    SELECT 
        rh.idHabitacion,
        r.codigoReserva,
        rh.fechaInicio,
        rh.fechaFin
    FROM ReservaHabitacion rh
    INNER JOIN Reserva r ON rh.idReserva = r.idReserva
    INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
    WHERE rh.activo = true
    AND CURRENT_DATE >= rh.fechaInicio 
    AND CURRENT_DATE < rh.fechaFin  -- Check-out day = disponible
    AND er.codigo IN ('CONF', 'CURSO')
)
SELECT 
    h.idHabitacion,
    h.codigo,
    h.numero,
    h.nombre,
    th.nombre AS tipoHabitacion,
    el.codigo AS estadoCodigo,
    el.nombre AS estado,
    CASE 
        WHEN ra.idHabitacion IS NOT NULL THEN true
        ELSE false
    END AS ocupadaHoy,
    ra.codigoReserva AS reservaActual,
    ra.fechaInicio,
    ra.fechaFin
FROM Habitacion h
INNER JOIN TipoHabitacion th ON h.idTipoHabitacion = th.idTipoHabitacion
INNER JOIN EstadoLugar el ON h.idEstadoLugar = el.idEstadoLugar
LEFT JOIN reservas_activas ra ON h.idHabitacion = ra.idHabitacion
WHERE h.activo = true;

-- ============================================
-- 9. VISTA: Facturación Completa (OPTIMIZADA)
-- Justificación: Reportes contables, evita JOINs complejos
-- Optimización: Usa LEFT JOIN con agregaciones precalculadas
-- ============================================
CREATE OR REPLACE VIEW v_facturas_completo AS
WITH totales_factura AS (
    SELECT 
        idFactura,
        SUM(total) AS totalFactura,
        SUM(subtotal) AS subtotalFactura,
        SUM(IVA) AS totalIVA
    FROM FacturaDetalle
    GROUP BY idFactura
)
SELECT 
    f.idFactura,
    f.numeroFactura,
    c.codigoCliente,
    p.nombres || ' ' || p.apellidos AS nombreCliente,
    p.numeroDocumento AS documentoCliente,
    r.codigoReserva,
    f.fechaEmision,
    f.estado,
    COALESCE(tf.totalFactura, 0) AS totalFactura,
    COALESCE(tf.subtotalFactura, 0) AS subtotalFactura,
    COALESCE(tf.totalIVA, 0) AS totalIVA,
    u.username AS usuarioCreacion,
    f.fechaCreacion,
    f.observaciones
FROM Factura f
INNER JOIN Cliente c ON f.idCliente = c.idCliente
INNER JOIN Persona p ON c.idPersona = p.idPersona
INNER JOIN Reserva r ON f.idReserva = r.idReserva
INNER JOIN Usuario u ON f.idUsuarioCreacion = u.idUsuario
LEFT JOIN totales_factura tf ON f.idFactura = tf.idFactura;

-- ============================================
-- 10. VISTA: Auditoría Legible (SIN ORDER BY)
-- Justificación: Reportes de seguridad y seguimiento
-- Nota: ORDER BY removido - se aplica en la consulta final del cliente
-- ============================================
CREATE OR REPLACE VIEW v_auditoria_legible AS
SELECT 
    a.idAuditoria,
    u.username,
    p.nombres || ' ' || p.apellidos AS nombreUsuario,
    a.fechaHora,
    a.accion,
    a.modulo,
    a.tabla,
    a.idRegistro,
    a.descripcion,
    a.ipAddress
FROM Auditoria a
LEFT JOIN Usuario u ON a.idUsuario = u.idUsuario
LEFT JOIN Persona p ON u.idPersona = p.idPersona;

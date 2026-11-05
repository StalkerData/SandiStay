-- ============================================
-- FUNCIONES Y PROCEDIMIENTOS ESENCIALES
-- Sistema Hotelero
-- ============================================

-- ============================================
-- 1. FUNCIÓN: Buscar Habitaciones Disponibles por Fechas
-- Justificación: Consulta más frecuente del sistema, optimizada
-- CORRECCIÓN CRÍTICA: No filtrar por estado 'DISP', solo por reservas
-- Retorna: Lista de habitaciones disponibles con información completa
-- ============================================

CREATE OR REPLACE FUNCTION fn_buscar_habitaciones_disponibles(
    p_fecha_inicio DATE,
    p_fecha_fin DATE,
    p_num_adultos INTEGER DEFAULT NULL,
    p_num_ninos INTEGER DEFAULT NULL,
    p_tipo_habitacion INTEGER DEFAULT NULL
)
RETURNS TABLE (
    idHabitacion INTEGER,
    codigo VARCHAR(20),
    numero VARCHAR(10),
    nombre VARCHAR(100),
    tipoHabitacion VARCHAR(100),
    capacidadAdultos INTEGER,
    capacidadNinos INTEGER,
    precioNoche NUMERIC(10,2),
    precioDia NUMERIC(10,2),
    descripcion TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        h.idHabitacion,
        h.codigo,
        h.numero,
        h.nombre,
        th.nombre AS tipoHabitacion,
        h.capacidadAdultos,
        h.capacidadNinos,
        h.precioNoche,
        h.precioDia,
        h.descripcion
    FROM Habitacion h
    INNER JOIN TipoHabitacion th ON h.idTipoHabitacion = th.idTipoHabitacion
    INNER JOIN EstadoLugar el ON h.idEstadoLugar = el.idEstadoLugar
    WHERE h.activo = true
    -- CORRECCIÓN: Solo excluir si está en mantenimiento, no si está ocupada hoy
    AND el.codigo NOT IN ('MANT', 'REP')
    -- Filtro opcional por tipo de habitación
    AND (p_tipo_habitacion IS NULL OR h.idTipoHabitacion = p_tipo_habitacion)
    -- Filtro opcional por capacidad
    AND (p_num_adultos IS NULL OR h.capacidadAdultos >= p_num_adultos)
    AND (p_num_ninos IS NULL OR h.capacidadNinos >= p_num_ninos)
    -- No tiene reservas activas en el rango de fechas
    AND NOT EXISTS (
        SELECT 1 
        FROM ReservaHabitacion rh
        INNER JOIN Reserva r ON rh.idReserva = r.idReserva
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE rh.idHabitacion = h.idHabitacion
        AND rh.activo = true
        AND er.codigo IN ('CONF', 'CURSO', 'PEND')
        AND daterange(rh.fechaInicio, rh.fechaFin, '[]') && 
            daterange(p_fecha_inicio, p_fecha_fin, '[]')
    )
    ORDER BY h.precioNoche;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 2. FUNCIÓN: Buscar Áreas Disponibles por Fecha y Hora
-- Justificación: Para reservas de eventos y espacios
-- CORRECCIÓN CRÍTICA: No filtrar por estado 'DISP', solo por reservas
-- ============================================

CREATE OR REPLACE FUNCTION fn_buscar_areas_disponibles(
    p_fecha_evento DATE,
    p_hora_inicio TIME,
    p_hora_fin TIME,
    p_capacidad_minima INTEGER DEFAULT NULL,
    p_tipo_area INTEGER DEFAULT NULL
)
RETURNS TABLE (
    idArea INTEGER,
    codigo VARCHAR(20),
    nombre VARCHAR(100),
    tipoArea VARCHAR(100),
    capacidadPersonas INTEGER,
    precioHora NUMERIC(10,2),
    descripcion TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        a.idArea,
        a.codigo,
        a.nombre,
        ta.nombre AS tipoArea,
        a.capacidadPersonas,
        a.precioHora,
        a.descripcion
    FROM Area a
    INNER JOIN TipoArea ta ON a.idTipoArea = ta.idTipoArea
    INNER JOIN EstadoLugar el ON a.idEstadoLugar = el.idEstadoLugar
    WHERE a.activo = true
    -- CORRECCIÓN: Solo excluir si está en mantenimiento, no si está ocupada hoy
    AND el.codigo NOT IN ('MANT', 'REP')
    -- Filtros opcionales
    AND (p_tipo_area IS NULL OR a.idTipoArea = p_tipo_area)
    AND (p_capacidad_minima IS NULL OR a.capacidadPersonas >= p_capacidad_minima)
    -- No tiene reservas en el mismo día y horario
    AND NOT EXISTS (
        SELECT 1 
        FROM ReservaArea ra
        INNER JOIN Reserva r ON ra.idReserva = r.idReserva
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE ra.idArea = a.idArea
        AND ra.activo = true
        AND er.codigo IN ('CONF', 'CURSO', 'PEND')
        AND ra.fechaEvento = p_fecha_evento
        AND tsrange(
            (p_fecha_evento + p_hora_inicio)::timestamp,
            (p_fecha_evento + p_hora_fin)::timestamp,
            '[]'
        ) && tsrange(
            (ra.fechaEvento + ra.horaInicio)::timestamp,
            (ra.fechaEvento + ra.horaFin)::timestamp,
            '[]'
        )
    )
    ORDER BY a.precioHora;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 3. FUNCIÓN: Calcular Total de Reserva
-- Justificación: Centraliza lógica de cálculo con descuentos
-- ============================================

CREATE OR REPLACE FUNCTION fn_calcular_total_reserva(
    p_id_reserva INTEGER
)
RETURNS TABLE (
    subtotalHabitaciones NUMERIC(10,2),
    subtotalAreas NUMERIC(10,2),
    descuentoCliente NUMERIC(10,2),
    totalFinal NUMERIC(10,2)
) AS $$
DECLARE
    v_porcentaje_descuento NUMERIC(5,2);
    v_subtotal_hab NUMERIC(10,2);
    v_subtotal_areas NUMERIC(10,2);
    v_descuento NUMERIC(10,2);
    v_total NUMERIC(10,2);
BEGIN
    -- Obtener porcentaje de descuento del cliente
    SELECT tc.porcentajeDescuento
    INTO v_porcentaje_descuento
    FROM Reserva r
    INNER JOIN Cliente c ON r.idCliente = c.idCliente
    INNER JOIN TipoCliente tc ON c.idTipoCliente = tc.idTipoCliente
    WHERE r.idReserva = p_id_reserva;
    
    -- Calcular subtotal de habitaciones
    SELECT COALESCE(SUM((numeroNoches * precioNoche) + (numeroDias * precioDia)), 0)
    INTO v_subtotal_hab
    FROM ReservaHabitacion
    WHERE idReserva = p_id_reserva AND activo = true;
    
    -- Calcular subtotal de áreas
    SELECT COALESCE(SUM(duracionHoras * precioHora), 0)
    INTO v_subtotal_areas
    FROM ReservaArea
    WHERE idReserva = p_id_reserva AND activo = true;
    
    -- Calcular descuento
    v_descuento := ((v_subtotal_hab + v_subtotal_areas) * v_porcentaje_descuento / 100);
    
    -- Calcular total final
    v_total := (v_subtotal_hab + v_subtotal_areas) - v_descuento;
    
    RETURN QUERY
    SELECT v_subtotal_hab, v_subtotal_areas, v_descuento, v_total;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 4. PROCEDIMIENTO: Cancelar Reserva
-- Justificación: Proceso complejo con múltiples pasos
-- Mejora: No sobrescribe estado de habitaciones en mantenimiento
-- ============================================

CREATE OR REPLACE PROCEDURE sp_cancelar_reserva(
    p_id_reserva INTEGER,
    p_motivo_cancelacion TEXT,
    p_id_usuario INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_cancelada INTEGER;
    v_estado_disponible INTEGER;
BEGIN
    -- Obtener IDs de estados
    SELECT idEstadoReserva INTO v_estado_cancelada
    FROM EstadoReserva WHERE codigo = 'CANC';
    
    SELECT idEstadoLugar INTO v_estado_disponible
    FROM EstadoLugar WHERE codigo = 'DISP';
    
    -- 1. Actualizar estado de la reserva
    UPDATE Reserva
    SET idEstadoReserva = v_estado_cancelada,
        motivoCancelacion = p_motivo_cancelacion,
        fechaCancelacion = CURRENT_TIMESTAMP
    WHERE idReserva = p_id_reserva;
    
    -- 2. Liberar habitaciones (CORREGIDO PARA POSTGRESQL)
    -- Usamos una subconsulta para encontrar las habitaciones que debemos liberar
    UPDATE Habitacion
    SET idEstadoLugar = v_estado_disponible
    WHERE idHabitacion IN (
        SELECT rh.idHabitacion 
        FROM ReservaHabitacion rh
        INNER JOIN Habitacion h2 ON rh.idHabitacion = h2.idHabitacion
        INNER JOIN EstadoLugar el ON h2.idEstadoLugar = el.idEstadoLugar
        WHERE rh.idReserva = p_id_reserva
        AND rh.activo = true
        AND el.codigo = 'OCUP'
    );
    
    -- 3. Liberar áreas (CORREGIDO PARA POSTGRESQL)
    UPDATE Area
    SET idEstadoLugar = v_estado_disponible
    WHERE idArea IN (
        SELECT ra.idArea 
        FROM ReservaArea ra
        INNER JOIN Area a2 ON ra.idArea = a2.idArea
        INNER JOIN EstadoLugar el ON a2.idEstadoLugar = el.idEstadoLugar
        WHERE ra.idReserva = p_id_reserva
        AND ra.activo = true
        AND el.codigo = 'OCUP'
    );
    
    -- 4. Registrar en auditoría
    INSERT INTO Auditoria (idUsuario, accion, modulo, tabla, idRegistro, descripcion)
    VALUES (p_id_usuario, 'UPDATE', 'Reservas', 'reserva', p_id_reserva, 
            'Reserva cancelada: ' || p_motivo_cancelacion);
    
    RAISE NOTICE 'Reserva % cancelada exitosamente', p_id_reserva;
END;
$$;

-- ============================================
-- 5. PROCEDIMIENTO: Completar Reserva (Check-out)
-- Justificación: Proceso de finalización con múltiples validaciones
-- ============================================

CREATE OR REPLACE PROCEDURE sp_completar_reserva(
    p_id_reserva INTEGER,
    p_id_usuario INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_completada INTEGER;
    v_estado_limpieza INTEGER;
    v_total_reserva NUMERIC(10,2);
    v_total_pagado NUMERIC(10,2);
    v_saldo NUMERIC(10,2);
BEGIN
    -- Obtener IDs de estados
    SELECT idEstadoReserva INTO v_estado_completada
    FROM EstadoReserva WHERE codigo = 'COMP';
    
    SELECT idEstadoLugar INTO v_estado_limpieza
    FROM EstadoLugar WHERE codigo = 'LIMP';
    
    -- Calcular totales
    SELECT totalFinal INTO v_total_reserva
    FROM fn_calcular_total_reserva(p_id_reserva);
    
    SELECT COALESCE(SUM(monto), 0) INTO v_total_pagado
    FROM Pago p
    INNER JOIN EstadoPago ep ON p.idEstadoPago = ep.idEstadoPago
    WHERE p.idReserva = p_id_reserva
    AND ep.codigo = 'APRO';
    
    v_saldo := v_total_reserva - v_total_pagado;
    
    -- Validar que no haya saldo pendiente
    IF v_saldo > 0 THEN
        RAISE EXCEPTION 'No se puede completar la reserva. Saldo pendiente: $%', v_saldo;
    END IF;
    
    -- Actualizar estado de la reserva
    UPDATE Reserva
    SET idEstadoReserva = v_estado_completada
    WHERE idReserva = p_id_reserva;
    
    -- Cambiar habitaciones a estado "Limpieza"
    UPDATE Habitacion h
    SET idEstadoLugar = v_estado_limpieza
    FROM ReservaHabitacion rh
    WHERE h.idHabitacion = rh.idHabitacion
    AND rh.idReserva = p_id_reserva
    AND rh.activo = true;
    
    -- Cambiar áreas a estado "Limpieza"
    UPDATE Area a
    SET idEstadoLugar = v_estado_limpieza
    FROM ReservaArea ra
    WHERE a.idArea = ra.idArea
    AND ra.idReserva = p_id_reserva
    AND ra.activo = true;
    
    -- Registrar en auditoría
    INSERT INTO Auditoria (idUsuario, accion, modulo, tabla, idRegistro, descripcion)
    VALUES (p_id_usuario, 'UPDATE', 'Reservas', 'reserva', p_id_reserva, 
            'Reserva completada - Check-out realizado');
    
    RAISE NOTICE 'Reserva % completada exitosamente. Check-out realizado.', p_id_reserva;
END;
$$;

-- ============================================
-- 6. FUNCIÓN: Obtener Reporte de Ocupación por Período
-- Justificación: Reporte gerencial crítico
-- ============================================
CREATE OR REPLACE FUNCTION fn_reporte_ocupacion(
    p_fecha_inicio DATE,
    p_fecha_fin DATE
)
RETURNS TABLE (
    out_fecha DATE,
    out_tipo VARCHAR,
    out_ocupadas BIGINT,
    out_ingreso NUMERIC(12,2)
) AS $$
BEGIN
    RETURN QUERY
    WITH habs AS (
        SELECT 
            f.fecha::DATE AS col_fecha,
            'Habitación'::VARCHAR AS col_tipo,
            COUNT(DISTINCT rh.idHabitacion) AS col_ocupadas,
            SUM(rh.precioNoche) AS col_ingreso
        FROM (SELECT generate_series(p_fecha_inicio, p_fecha_fin, '1 day'::interval)::DATE AS fecha) f
        INNER JOIN ReservaHabitacion rh ON f.fecha >= rh.fechaInicio AND f.fecha < rh.fechaFin
        INNER JOIN Reserva r ON rh.idReserva = r.idReserva
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE rh.activo = true AND er.codigo IN ('CONF', 'CURSO', 'COMP')
        GROUP BY f.fecha
    ),
    areas AS (
        SELECT 
            ra.fechaEvento AS col_fecha,
            'Área Social'::VARCHAR AS col_tipo,
            COUNT(DISTINCT ra.idArea) AS col_ocupadas,
            SUM(ra.precioHora * ra.duracionHoras) AS col_ingreso
        FROM ReservaArea ra
        INNER JOIN Reserva r ON ra.idReserva = r.idReserva
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE ra.activo = true AND er.codigo IN ('CONF', 'CURSO', 'COMP')
        AND ra.fechaEvento BETWEEN p_fecha_inicio AND p_fecha_fin
        GROUP BY ra.fechaEvento
    ),
    multas AS (
        -- NUEVO BLOQUE: Sumamos los ingresos por penalidades
        SELECT 
            p.fechaPago::DATE AS col_fecha,
            'Penalidad por Cancelación'::VARCHAR AS col_tipo,
            0::BIGINT AS col_ocupadas, -- Una multa no ocupa espacio físico
            SUM(p.monto) AS col_ingreso
        FROM Pago p
        WHERE p.codigoPago LIKE 'MUL-%' 
        AND p.idEstadoPago = 2 -- Solo multas aprobadas/cobradas
        AND p.fechaPago::DATE BETWEEN p_fecha_inicio AND p_fecha_fin
        GROUP BY p.fechaPago::DATE
    )
    -- Unimos los 3 bloques. Filtramos donde haya ingresos o habitaciones ocupadas.
    SELECT col_fecha, col_tipo, col_ocupadas, col_ingreso FROM habs WHERE col_ocupadas > 0 OR col_ingreso > 0
    UNION ALL
    SELECT col_fecha, col_tipo, col_ocupadas, col_ingreso FROM areas WHERE col_ocupadas > 0 OR col_ingreso > 0
    UNION ALL
    SELECT col_fecha, col_tipo, col_ocupadas, col_ingreso FROM multas WHERE col_ingreso > 0
    ORDER BY col_fecha ASC, col_tipo DESC;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 7. FUNCIÓN: Obtener Historial de Cliente
-- Justificación: Para atención personalizada y marketing
-- ============================================

CREATE OR REPLACE FUNCTION fn_historial_cliente(
    p_id_cliente INTEGER
)
RETURNS TABLE (
    totalReservas BIGINT,
    reservasCompletadas BIGINT,
    reservasCanceladas BIGINT,
    totalGastado NUMERIC(12,2),
    ultimaReserva DATE,
    habitacionFavorita VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    WITH stats AS (
        SELECT
            COUNT(*) AS total_reservas,
            COUNT(*) FILTER (WHERE er.codigo = 'COMP') AS completadas,
            COUNT(*) FILTER (WHERE er.codigo = 'CANC') AS canceladas,
            MAX(r.fechaReserva::DATE) AS ultima_reserva
        FROM Reserva r
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE r.idCliente = p_id_cliente
    ),
    gastos AS (
        -- CORRECCIÓN: Cambiamos SUM(total) por SUM(totalPagado)
        SELECT COALESCE(SUM(totalPagado), 0) AS total_gastado
        FROM v_estado_cuenta_reservas
        WHERE codigoCliente = (SELECT codigoCliente FROM Cliente WHERE idCliente = p_id_cliente)
    ),
    favorita AS (
        SELECT h.nombre
        FROM ReservaHabitacion rh
        INNER JOIN Habitacion h ON rh.idHabitacion = h.idHabitacion
        INNER JOIN Reserva r ON rh.idReserva = r.idReserva
        WHERE r.idCliente = p_id_cliente
        AND rh.activo = true
        GROUP BY h.nombre
        ORDER BY COUNT(*) DESC
        LIMIT 1
    )
    SELECT
        s.total_reservas,
        s.completadas,
        s.canceladas,
        g.total_gastado,
        s.ultima_reserva,
        f.nombre
    FROM stats s
    CROSS JOIN gastos g
    LEFT JOIN favorita f ON true;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 8. PROCEDIMIENTO: Generar Factura Automática
-- Justificación: Automatiza proceso de facturación
-- Mejora: IVA como constante, error de sintaxis corregido
-- ============================================
CREATE OR REPLACE PROCEDURE sp_generar_factura(
    p_id_reserva INTEGER,
    p_id_usuario INTEGER,
    OUT p_id_factura INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    c_factor_iva CONSTANT NUMERIC := 1.19; -- Factor para extraer el IVA incluido
    v_id_cliente INTEGER;
    v_id_factura_nuevo INTEGER;
BEGIN
    SELECT idCliente INTO v_id_cliente FROM Reserva WHERE idReserva = p_id_reserva;
    
    INSERT INTO Factura (idCliente, idReserva, estado, idUsuarioCreacion)
    VALUES (v_id_cliente, p_id_reserva, 'Pagada', p_id_usuario)
    RETURNING idFactura INTO v_id_factura_nuevo;
    
    -- Habitaciones (Extrayendo el IVA del precio final)
    INSERT INTO FacturaDetalle (idFactura, descripcion, cantidad, subtotal, IVA, total)
    SELECT 
        v_id_factura_nuevo,
        'Hospedaje ' || h.nombre || ' (' || rh.numeroNoches || ' noches)',
        rh.numeroNoches,
        ROUND(((rh.numeroNoches * rh.precioNoche) / c_factor_iva)::numeric, 2), -- Subtotal real
        ROUND(((rh.numeroNoches * rh.precioNoche) - ((rh.numeroNoches * rh.precioNoche) / c_factor_iva))::numeric, 2), -- IVA extraído
        (rh.numeroNoches * rh.precioNoche) -- Total final que pagó el cliente
    FROM ReservaHabitacion rh
    JOIN Habitacion h ON rh.idHabitacion = h.idHabitacion
    WHERE rh.idReserva = p_id_reserva AND rh.activo = true;

    -- Áreas Sociales (Extrayendo el IVA del precio final)
    INSERT INTO FacturaDetalle (idFactura, descripcion, cantidad, subtotal, IVA, total)
    SELECT 
        v_id_factura_nuevo,
        'Alquiler ' || a.nombre || ' (' || ra.duracionHoras || ' horas)',
        1,
        ROUND(((ra.duracionHoras * ra.precioHora) / c_factor_iva)::numeric, 2),
        ROUND(((ra.duracionHoras * ra.precioHora) - ((ra.duracionHoras * ra.precioHora) / c_factor_iva))::numeric, 2),
        (ra.duracionHoras * ra.precioHora)
    FROM ReservaArea ra
    JOIN Area a ON ra.idArea = a.idArea
    WHERE ra.idReserva = p_id_reserva AND ra.activo = true;
    
    p_id_factura := v_id_factura_nuevo;
END;
$$;


-- ============================================
-- 9. FUNCIÓN: Validar Disponibilidad de Habitación
-- Justificación: Validación rápida antes de reservar
-- ============================================

CREATE OR REPLACE FUNCTION fn_validar_disponibilidad_habitacion(
    p_id_habitacion INTEGER,
    p_fecha_inicio DATE,
    p_fecha_fin DATE
)
RETURNS BOOLEAN AS $$
DECLARE
    v_tiene_conflicto BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 
        FROM ReservaHabitacion rh
        INNER JOIN Reserva r ON rh.idReserva = r.idReserva
        INNER JOIN EstadoReserva er ON r.idEstadoReserva = er.idEstadoReserva
        WHERE rh.idHabitacion = p_id_habitacion
        AND rh.activo = true
        AND er.codigo IN ('CONF', 'CURSO', 'PEND')
        AND daterange(rh.fechaInicio, rh.fechaFin, '[]') && 
            daterange(p_fecha_inicio, p_fecha_fin, '[]')
    ) INTO v_tiene_conflicto;
    
    RETURN NOT v_tiene_conflicto;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 10. FUNCIÓN: Calcular Días para Check-in
-- Justificación: Notificaciones y recordatorios
-- ============================================

CREATE OR REPLACE FUNCTION fn_dias_para_checkin(
    p_id_reserva INTEGER
)
RETURNS INTEGER AS $$
DECLARE
    v_fecha_inicio DATE;
    v_dias INTEGER;
BEGIN
    SELECT MIN(fechaInicio) INTO v_fecha_inicio
    FROM ReservaHabitacion
    WHERE idReserva = p_id_reserva AND activo = true;
    
    IF v_fecha_inicio IS NULL THEN
        SELECT MIN(fechaEvento) INTO v_fecha_inicio
        FROM ReservaArea
        WHERE idReserva = p_id_reserva AND activo = true;
    END IF;
    
    v_dias := v_fecha_inicio - CURRENT_DATE;
    
    RETURN v_dias;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 11. PROCEDIMIENTO: Cierre Diario (Night Audit)
-- Justificación: CRÍTICO en hotelería - Marca No-Show automáticamente
-- Debe ejecutarse a medianoche (cron job o pg_cron)
-- ============================================

CREATE OR REPLACE PROCEDURE sp_cierre_diario_noshow()
LANGUAGE plpgsql
AS $$
DECLARE
    v_estado_cancelada INTEGER;
    v_reservas_canceladas INTEGER;
BEGIN
    -- Obtener ID del estado Cancelada
    SELECT idEstadoReserva INTO v_estado_cancelada
    FROM EstadoReserva WHERE codigo = 'CANC';
    
    -- Marcar como No-Show las reservas confirmadas que no hicieron check-in
    WITH reservas_no_show AS (
        UPDATE Reserva
        SET 
            idEstadoReserva = v_estado_cancelada,
            motivoCancelacion = 'No Show - Cancelación automática por sistema',
            fechaCancelacion = CURRENT_TIMESTAMP
        WHERE idEstadoReserva = (SELECT idEstadoReserva FROM EstadoReserva WHERE codigo = 'CONF')
        AND idReserva IN (
            SELECT DISTINCT idReserva 
            FROM ReservaHabitacion 
            WHERE fechaInicio < CURRENT_DATE -- Debió llegar ayer o antes
            AND activo = true
        )
        RETURNING idReserva
    )
    SELECT COUNT(*) INTO v_reservas_canceladas FROM reservas_no_show;
    
    -- Registrar en auditoría
    INSERT INTO Auditoria (
        idUsuario, 
        accion, 
        modulo, 
        tabla, 
        descripcion
    ) VALUES (
        NULL, -- Sistema automático
        'UPDATE', 
        'Reservas', 
        'reserva', 
        'Cierre diario: ' || v_reservas_canceladas || ' reservas marcadas como No-Show'
    );
    
    RAISE NOTICE 'Cierre diario completado. % reservas marcadas como No-Show', v_reservas_canceladas;
END;
$$;

-- ============================================
-- CONFIGURACIÓN RECOMENDADA: pg_cron
-- ============================================
-- Para ejecutar el cierre diario automáticamente:
-- 
-- 1. Instalar extensión pg_cron:
--    CREATE EXTENSION pg_cron;
--
-- 2. Programar ejecución a medianoche:
--    SELECT cron.schedule(
--        'night-audit',           -- Nombre del job
--        '0 0 * * *',            -- A las 00:00 todos los días
--        'CALL sp_cierre_diario_noshow();'
--    );
--
-- 3. Ver jobs programados:
--    SELECT * FROM cron.job;
-- ============================================

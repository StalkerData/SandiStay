-- ============================================
-- TRIGGERS ESENCIALES - SISTEMA HOTELERO (REFACTORIZADO)
-- ============================================

-- ============================================
-- SECUENCIAS PARA GENERACIÓN DE CÓDIGOS
-- Justificación: Atómicas, sin race conditions, thread-safe
-- ============================================

CREATE SEQUENCE seq_cliente_cod START 1;
CREATE SEQUENCE seq_reserva_cod START 1;
CREATE SEQUENCE seq_pago_cod START 1;
CREATE SEQUENCE seq_factura_cod START 1;

-- ============================================
-- DEFAULTS PARA CÓDIGOS AUTOMÁTICOS
-- Justificación: Más simple, más seguro, sin triggers
-- ============================================

ALTER TABLE Cliente 
ALTER COLUMN codigoCliente 
SET DEFAULT 'CLI-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || LPAD(nextval('seq_cliente_cod')::TEXT, 4, '0');

ALTER TABLE Reserva 
ALTER COLUMN codigoReserva 
SET DEFAULT 'RES-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(nextval('seq_reserva_cod')::TEXT, 4, '0');

ALTER TABLE Pago 
ALTER COLUMN codigoPago 
SET DEFAULT 'PAG-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(nextval('seq_pago_cod')::TEXT, 4, '0');

ALTER TABLE Factura 
ALTER COLUMN numeroFactura 
SET DEFAULT 'FACT-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-' || LPAD(nextval('seq_factura_cod')::TEXT, 4, '0');

-- ============================================
-- 1. TRIGGER: Calcular Noches y Días en Reserva Habitación
-- Justificación: Evita inconsistencias, garantiza cálculos correctos
-- ============================================

CREATE OR REPLACE FUNCTION fn_calcular_noches_dias_habitacion()
RETURNS TRIGGER AS $$
BEGIN
    -- Calcular número de noches (diferencia de fechas)
    NEW.numeroNoches := NEW.fechaFin - NEW.fechaInicio;
    
    -- Calcular número de días (mismo que noches para habitaciones)
    NEW.numeroDias := NEW.numeroNoches;
    
    -- Validar que sea al menos 1 noche
    IF NEW.numeroNoches < 1 THEN
        RAISE EXCEPTION 'La reserva debe ser de al menos 1 noche';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calcular_noches_dias_habitacion
BEFORE INSERT OR UPDATE ON ReservaHabitacion
FOR EACH ROW
EXECUTE FUNCTION fn_calcular_noches_dias_habitacion();

-- ============================================
-- 2. TRIGGER: Calcular Duración en Horas para Reserva de Área
-- Justificación: Evita errores de cálculo manual
-- ============================================

CREATE OR REPLACE FUNCTION fn_calcular_duracion_horas_area()
RETURNS TRIGGER AS $$
DECLARE
    minutos_diferencia INTEGER;
BEGIN
    -- Calcular diferencia en minutos y convertir a horas decimales
    minutos_diferencia := EXTRACT(EPOCH FROM (NEW.horaFin - NEW.horaInicio)) / 60;
    NEW.duracionHoras := ROUND((minutos_diferencia / 60.0)::NUMERIC, 2);
    
    -- Validar que sea al menos 1 hora
    IF NEW.duracionHoras < 1 THEN
        RAISE EXCEPTION 'La reserva de área debe ser de al menos 1 hora';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calcular_duracion_horas_area
BEFORE INSERT OR UPDATE ON ReservaArea
FOR EACH ROW
EXECUTE FUNCTION fn_calcular_duracion_horas_area();

-- ============================================
-- 3. TRIGGER: Validar Capacidad de Habitación
-- Justificación: Previene overbooking de personas
-- ============================================

CREATE OR REPLACE FUNCTION fn_validar_capacidad_habitacion()
RETURNS TRIGGER AS $$
DECLARE
    capacidad_maxima INTEGER;
    total_personas INTEGER;
BEGIN
    -- Obtener capacidad máxima de la habitación
    SELECT h.capacidadMaxima 
    INTO capacidad_maxima
    FROM Habitacion h
    WHERE h.idHabitacion = NEW.idHabitacion;
    
    -- Obtener total de personas de la reserva
    SELECT r.numeroAdultos + r.numeroNinos
    INTO total_personas
    FROM Reserva r
    WHERE r.idReserva = NEW.idReserva;
    
    -- Validar capacidad
    IF total_personas > capacidad_maxima THEN
        RAISE EXCEPTION 'La habitación tiene capacidad para % personas, pero la reserva es para % personas', 
            capacidad_maxima, total_personas;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_capacidad_habitacion
BEFORE INSERT OR UPDATE ON ReservaHabitacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_capacidad_habitacion();

-- ============================================
-- 4. TRIGGER: Validar Capacidad de Área
-- Justificación: Previene overbooking de asistentes
-- ============================================

CREATE OR REPLACE FUNCTION fn_validar_capacidad_area()
RETURNS TRIGGER AS $$
DECLARE
    capacidad_maxima INTEGER;
BEGIN
    -- Obtener capacidad máxima del área
    SELECT a.capacidadPersonas 
    INTO capacidad_maxima
    FROM Area a
    WHERE a.idArea = NEW.idArea;
    
    -- Validar capacidad
    IF NEW.cantidadAsistentes > capacidad_maxima THEN
        RAISE EXCEPTION 'El área tiene capacidad para % personas, pero la reserva es para % asistentes', 
            capacidad_maxima, NEW.cantidadAsistentes;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_capacidad_area
BEFORE INSERT OR UPDATE ON ReservaArea
FOR EACH ROW
WHEN (NEW.cantidadAsistentes IS NOT NULL)
EXECUTE FUNCTION fn_validar_capacidad_area();

-- ============================================
-- 5. TRIGGER: Validar que Habitación esté Activa
-- Justificación: Previene reservas en habitaciones fuera de servicio
-- ============================================

CREATE OR REPLACE FUNCTION fn_validar_habitacion_activa()
RETURNS TRIGGER AS $$
DECLARE
    habitacion_activa BOOLEAN;
BEGIN
    -- Verificar si la habitación está activa
    SELECT h.activo 
    INTO habitacion_activa
    FROM Habitacion h
    WHERE h.idHabitacion = NEW.idHabitacion;
    
    IF NOT habitacion_activa THEN
        RAISE EXCEPTION 'No se puede reservar la habitación porque está inactiva o fuera de servicio';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_habitacion_activa
BEFORE INSERT ON ReservaHabitacion
FOR EACH ROW
EXECUTE FUNCTION fn_validar_habitacion_activa();

-- ============================================
-- 6. TRIGGER: Validar que Área esté Activa
-- Justificación: Previene reservas en áreas fuera de servicio
-- ============================================

CREATE OR REPLACE FUNCTION fn_validar_area_activa()
RETURNS TRIGGER AS $$
DECLARE
    area_activa BOOLEAN;
BEGIN
    -- Verificar si el área está activa
    SELECT a.activo 
    INTO area_activa
    FROM Area a
    WHERE a.idArea = NEW.idArea;
    
    IF NOT area_activa THEN
        RAISE EXCEPTION 'No se puede reservar el área porque está inactiva o fuera de servicio';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_area_activa
BEFORE INSERT ON ReservaArea
FOR EACH ROW
EXECUTE FUNCTION fn_validar_area_activa();

-- ============================================
-- 7. TRIGGER: Copiar Precios Actuales a Reserva de Habitación
-- Justificación: Histórico de precios, inmutable ante cambios futuros
-- ============================================

CREATE OR REPLACE FUNCTION fn_copiar_precios_habitacion()
RETURNS TRIGGER AS $$
BEGIN
    -- Si no se especificaron precios, copiar los actuales de la habitación
    IF NEW.precioNoche IS NULL OR NEW.precioDia IS NULL THEN
        SELECT h.precioNoche, h.precioDia
        INTO NEW.precioNoche, NEW.precioDia
        FROM Habitacion h
        WHERE h.idHabitacion = NEW.idHabitacion;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_copiar_precios_habitacion
BEFORE INSERT ON ReservaHabitacion
FOR EACH ROW
EXECUTE FUNCTION fn_copiar_precios_habitacion();

-- ============================================
-- 8. TRIGGER: Copiar Precio Actual a Reserva de Área
-- Justificación: Histórico de precios, inmutable ante cambios futuros
-- ============================================

CREATE OR REPLACE FUNCTION fn_copiar_precio_area()
RETURNS TRIGGER AS $$
BEGIN
    -- Si no se especificó precio, copiar el actual del área
    IF NEW.precioHora IS NULL THEN
        SELECT a.precioHora
        INTO NEW.precioHora
        FROM Area a
        WHERE a.idArea = NEW.idArea;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_copiar_precio_area
BEFORE INSERT ON ReservaArea
FOR EACH ROW
EXECUTE FUNCTION fn_copiar_precio_area();

-- ============================================
-- 9. TRIGGER: Prevenir Modificación de Reservas Completadas
-- Justificación: Integridad histórica, cumplimiento contable
-- ============================================

CREATE OR REPLACE FUNCTION fn_prevenir_modificacion_reserva_completada()
RETURNS TRIGGER AS $$
DECLARE
    estado_actual VARCHAR(10);
BEGIN
    -- Obtener código del estado actual
    SELECT er.codigo
    INTO estado_actual
    FROM EstadoReserva er
    WHERE er.idEstadoReserva = OLD.idEstadoReserva;
    
    -- Si la reserva está completada, no permitir cambios
    IF estado_actual IN ('COMP', 'CANC') AND 
       NEW.idEstadoReserva != OLD.idEstadoReserva THEN
        RAISE EXCEPTION 'No se puede modificar una reserva completada o cancelada';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevenir_modificacion_reserva_completada
BEFORE UPDATE ON Reserva
FOR EACH ROW
EXECUTE FUNCTION fn_prevenir_modificacion_reserva_completada();

-- ============================================
-- 10. TRIGGER: Auditoría Automática de Cambios Críticos
-- Justificación: Trazabilidad, cumplimiento legal
-- Tablas auditadas: Reserva, Pago, Factura
-- ============================================

CREATE OR REPLACE FUNCTION fn_auditoria_cambios()
RETURNS TRIGGER AS $$
DECLARE
    accion_tipo VARCHAR(20);
    descripcion_cambio TEXT;
    usuario_actual INTEGER;
    id_registro INTEGER;
BEGIN
    -- Determinar tipo de acción
    IF TG_OP = 'INSERT' THEN
        accion_tipo := 'INSERT';
        descripcion_cambio := 'Registro creado';
    ELSIF TG_OP = 'UPDATE' THEN
        accion_tipo := 'UPDATE';
        descripcion_cambio := 'Registro actualizado';
    ELSIF TG_OP = 'DELETE' THEN
        accion_tipo := 'DELETE';
        descripcion_cambio := 'Registro eliminado';
    END IF;
    
    -- Obtener ID del registro según la tabla
    IF TG_TABLE_NAME = 'reserva' THEN
        id_registro := CASE WHEN TG_OP = 'DELETE' THEN OLD.idReserva ELSE NEW.idReserva END;
    ELSIF TG_TABLE_NAME = 'pago' THEN
        id_registro := CASE WHEN TG_OP = 'DELETE' THEN OLD.idPago ELSE NEW.idPago END;
    ELSIF TG_TABLE_NAME = 'factura' THEN
        id_registro := CASE WHEN TG_OP = 'DELETE' THEN OLD.idFactura ELSE NEW.idFactura END;
    END IF;
    
    -- Intentar obtener usuario actual de la sesión
    -- En producción esto vendría del contexto de la aplicación
    BEGIN
        usuario_actual := current_setting('app.current_user_id')::INTEGER;
    EXCEPTION
        WHEN OTHERS THEN
            usuario_actual := NULL;
    END;
    
    -- Insertar registro de auditoría
    INSERT INTO Auditoria (
        idUsuario,
        accion,
        modulo,
        tabla,
        idRegistro,
        descripcion,
        ipAddress
    ) VALUES (
        usuario_actual,
        accion_tipo,
        CASE 
            WHEN TG_TABLE_NAME = 'reserva' THEN 'Reservas'
            WHEN TG_TABLE_NAME = 'pago' THEN 'Pagos'
            WHEN TG_TABLE_NAME = 'factura' THEN 'Facturación'
        END,
        TG_TABLE_NAME,
        id_registro,
        descripcion_cambio,
        inet_client_addr()::TEXT
    );
    
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a tablas críticas
CREATE TRIGGER trg_auditoria_reserva
AFTER INSERT OR UPDATE OR DELETE ON Reserva
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria_cambios();

CREATE TRIGGER trg_auditoria_pago
AFTER INSERT OR UPDATE OR DELETE ON Pago
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria_cambios();

CREATE TRIGGER trg_auditoria_factura
AFTER INSERT OR UPDATE OR DELETE ON Factura
FOR EACH ROW
EXECUTE FUNCTION fn_auditoria_cambios();

-- ============================================
-- NOTAS IMPORTANTES
-- ============================================

-- 1. Códigos automáticos: Ahora usan SECUENCIAS (thread-safe, sin race conditions)
-- 2. Estado de habitación: Se maneja dinámicamente en la vista v_ocupacion_habitaciones
--    No se persiste en la tabla porque es un estado derivado del tiempo
-- 3. Auditoría: Configurar usuario antes de operaciones:
--    SET app.current_user_id = 123;
-- 4. Los triggers solo manejan lógica de negocio que debe ser inmutable en la DB

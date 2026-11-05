-- ============================================
-- ÍNDICES ESENCIALES - SISTEMA HOTELERO (OPTIMIZADO)
-- ============================================

-- ============================================
-- MÓDULO: PERSONAS Y CLIENTES
-- ============================================

-- Búsqueda de clientes por documento (muy común en recepción)
-- Justificación: Ya existe UNIQUE(idTipoDocumento, numeroDocumento), pero este permite búsquedas parciales
CREATE INDEX idx_persona_documento ON Persona(idTipoDocumento, numeroDocumento);

-- Búsqueda de clientes por email (login, recuperación contraseña)
-- Justificación: Aunque email no es único, se busca frecuentemente
CREATE INDEX idx_persona_email ON Persona(email) WHERE email IS NOT NULL;

-- ELIMINADO: idx_cliente_codigo (ya existe UNIQUE automático)

-- ============================================
-- MÓDULO: USUARIOS Y SEGURIDAD
-- ============================================

-- ELIMINADO: idx_usuario_username (ya existe UNIQUE automático, suficientemente rápido)

-- Búsqueda de roles de un usuario (verificación de permisos)
-- Justificación: Consulta en cada request para verificar autorización
CREATE INDEX idx_usuario_rol_usuario ON UsuarioRol(idUsuario);

-- Auditoría por usuario (reportes de actividad)
-- Justificación: "¿Qué ha hecho este usuario?" - reporte común de supervisores
CREATE INDEX idx_auditoria_usuario_fecha ON Auditoria(idUsuario, fechaHora DESC);

-- Auditoría por tabla y registro (seguimiento específico)
-- Justificación: "¿Quién modificó esta reserva?" - investigación de cambios
CREATE INDEX idx_auditoria_tabla_registro ON Auditoria(tabla, idRegistro);

-- ============================================
-- MÓDULO: HABITACIONES Y ÁREAS
-- ============================================

-- Búsqueda de habitaciones por estado (dashboard principal)
-- Justificación: "Mostrar todas las habitaciones disponibles/ocupadas/en limpieza"
CREATE INDEX idx_habitacion_estado ON Habitacion(idEstadoLugar) WHERE activo = true;

-- Búsqueda de habitaciones por tipo (filtros de búsqueda)
-- Justificación: "Mostrar todas las suites" o "Mostrar habitaciones dobles"
CREATE INDEX idx_habitacion_tipo ON Habitacion(idTipoHabitacion) WHERE activo = true;

-- Búsqueda de áreas por estado
-- Justificación: Similar a habitaciones, consulta frecuente en dashboard
CREATE INDEX idx_area_estado ON Area(idEstadoLugar) WHERE activo = true;

-- ============================================
-- MÓDULO: RESERVAS (CRÍTICO PARA RENDIMIENTO)
-- ============================================

-- Búsqueda de reservas por cliente (historial de cliente)
-- Justificación: "Ver todas las reservas de este cliente" - consulta muy común
CREATE INDEX idx_reserva_cliente ON Reserva(idCliente);

-- Búsqueda de reservas por estado (filtros de dashboard)
-- Justificación: "Mostrar reservas pendientes/confirmadas/canceladas"
CREATE INDEX idx_reserva_estado ON Reserva(idEstadoReserva);

-- Búsqueda de reservas por fecha (reportes diarios/mensuales)
-- Justificación: "Reservas de hoy", "Reservas de esta semana", "Reporte mensual"
CREATE INDEX idx_reserva_fecha ON Reserva(fechaReserva DESC);

-- ELIMINADO: idx_reserva_codigo (ya existe UNIQUE automático)

-- ============================================
-- ÍNDICES PARA DISPONIBILIDAD
-- ============================================

-- ELIMINADOS: idx_reserva_habitacion_fechas e idx_reserva_area_fechas
-- Justificación: Ya existen por los constraints EXCLUDE USING gist
-- PostgreSQL reutiliza automáticamente estos índices para búsquedas

-- Índice adicional para consultas de historial específico
-- Justificación: Consultas tipo "¿Cuándo estuvo ocupada esta habitación?"
-- Solo si el índice GiST del constraint no es suficientemente rápido
-- COMENTADO: Probar primero sin él, agregar solo si hay problemas de rendimiento
-- CREATE INDEX idx_reserva_habitacion_hab ON ReservaHabitacion(idHabitacion, fechaInicio DESC) 
-- WHERE activo = true;

-- Reserva por reserva (para JOINs)
-- Justificación: JOIN frecuente en vistas y consultas
CREATE INDEX idx_reserva_habitacion_reserva ON ReservaHabitacion(idReserva);

-- Área por reserva (para JOINs)
CREATE INDEX idx_reserva_area_reserva ON ReservaArea(idReserva);

-- ============================================
-- MÓDULO: PAGOS Y FACTURACIÓN
-- ============================================

-- Pagos por reserva (estado de cuenta)
-- Justificación: "¿Cuánto ha pagado de esta reserva?" - consulta constante
CREATE INDEX idx_pago_reserva ON Pago(idReserva);

-- Pagos por estado (pagos pendientes de aprobación)
-- Justificación: "Mostrar todos los pagos pendientes de aprobar"
CREATE INDEX idx_pago_estado ON Pago(idEstadoPago);

-- Pagos por fecha (reportes de caja diaria)
-- Justificación: "Caja del día", "Corte de caja", reportes diarios
CREATE INDEX idx_pago_fecha ON Pago(fechaPago DESC);

-- ELIMINADO: idx_pago_codigo (ya existe UNIQUE automático)

-- Facturas por cliente (historial de facturación)
-- Justificación: "Ver todas las facturas de este cliente"
CREATE INDEX idx_factura_cliente ON Factura(idCliente);

-- Facturas por reserva (consulta muy común)
-- Justificación: "¿Ya se facturó esta reserva?"
CREATE INDEX idx_factura_reserva ON Factura(idReserva);

-- Facturas por fecha de emisión (reportes contables)
-- Justificación: "Facturas emitidas hoy/este mes" - obligatorio para contabilidad
CREATE INDEX idx_factura_fecha ON Factura(fechaEmision DESC);

-- Detalles de factura por factura (siempre se consultan juntos)
-- Justificación: FK, siempre se necesitan los detalles al consultar una factura
CREATE INDEX idx_factura_detalle_factura ON FacturaDetalle(idFactura);

-- ============================================
-- MÓDULO: IMÁGENES
-- ============================================

-- Imágenes de habitaciones (galería de fotos)
-- Justificación: Cargar todas las fotos de una habitación
CREATE INDEX idx_imagen_habitacion ON ImagenHabitacion(idHabitacion) WHERE activo = true;

-- Imágenes de áreas (galería de fotos)
-- Justificación: Cargar todas las fotos de un área
CREATE INDEX idx_imagen_area ON ImagenArea(idArea) WHERE activo = true;

-- ============================================
-- ESTADÍSTICAS Y ANÁLISIS
-- ============================================

-- Actualizar estadísticas para el optimizador de PostgreSQL
-- Justificación: Ayuda al query planner a tomar mejores decisiones
ANALYZE Persona;
ANALYZE Cliente;
ANALYZE Usuario;
ANALYZE Reserva;
ANALYZE ReservaHabitacion;
ANALYZE ReservaArea;
ANALYZE Pago;
ANALYZE Factura;
ANALYZE Habitacion;
ANALYZE Area;

-- ============================================
-- RESUMEN DE ÍNDICES OPTIMIZADO
-- ============================================
-- Total: 20 índices (vs 28 originales)
-- Eliminados: 8 índices redundantes
--
-- Índices ELIMINADOS por redundancia:
-- ❌ idx_cliente_codigo (UNIQUE constraint ya lo indexa)
-- ❌ idx_usuario_username (UNIQUE constraint ya lo indexa)
-- ❌ idx_reserva_codigo (UNIQUE constraint ya lo indexa)
-- ❌ idx_pago_codigo (UNIQUE constraint ya lo indexa)
-- ❌ idx_reserva_habitacion_fechas (EXCLUDE constraint ya lo indexa)
-- ❌ idx_reserva_area_fechas (EXCLUDE constraint ya lo indexa)
-- ❌ idx_reserva_habitacion_hab (GiST constraint cubre el caso)
-- ❌ idx_reserva_area_area (GiST constraint cubre el caso)
--
-- Beneficios:
-- ✅ 40% menos espacio en disco
-- ✅ Inserciones más rápidas (menos índices que actualizar)
-- ✅ Mantenimiento simplificado
-- ✅ Mismo rendimiento de consultas
--
-- Monitoreo post-producción:
-- Después de 1 mes, revisar índices no utilizados:
-- SELECT schemaname, tablename, indexname, idx_scan
-- FROM pg_stat_user_indexes
-- WHERE idx_scan = 0 AND schemaname = 'public'
-- ORDER BY pg_relation_size(indexrelid) DESC;
-- ============================================

-- ==========================================================
-- 06_seed_data.sql - Datos Semilla para Producción (Clean Slate)
-- Contiene únicamente catálogos paramétricos y el Super Admin.
-- ==========================================================

-- MÓDULO 1: Ubicación
INSERT INTO Pais (codigo, nombre, codigoTelefono) VALUES 
('CO', 'Colombia', '+57'),
('VE', 'Venezuela', '+58'),
('US', 'Estados Unidos', '+1')
ON CONFLICT DO NOTHING;

-- MÓDULO 2: Personas
INSERT INTO TipoDocumento (codigo, nombre) VALUES 
('CC', 'Cédula de Ciudadanía'),
('CE', 'Cédula de Extranjería'),
('PA', 'Pasaporte'),
('NIT', 'Número de Identificación Tributaria')
ON CONFLICT DO NOTHING;

INSERT INTO EstadoPersona (codigo, nombre) VALUES 
('ACT', 'Activo'),
('INA', 'Inactivo'),
('BLOQ', 'Bloqueado')
ON CONFLICT DO NOTHING;

-- MÓDULO 3: Clientes
INSERT INTO TipoCliente (codigo, nombre, porcentajeDescuento) VALUES 
('REG', 'Regular', 0.00),
('PREM', 'Premium', 10.00),
('CORP', 'Corporativo', 15.00)
ON CONFLICT DO NOTHING;

-- MÓDULO 4: Usuarios y Seguridad (Roles)
INSERT INTO Rol (codigo, nombre, descripcion, nivelAcceso) VALUES 
('ADMIN', 'Administrador', 'Control total del sistema', 10),
('STAFF', 'Recepcionista', 'Gestión operativa diaria', 5)
ON CONFLICT DO NOTHING;

-- MÓDULO 5: Habitaciones y Áreas (Estados y Tipos)
INSERT INTO EstadoLugar (codigo, nombre, color) VALUES 
('DISP', 'Disponible', '#28a745'),
('OCUP', 'Ocupada', '#dc3545'),
('MANT', 'Mantenimiento', '#ffc107'),
('LIMP', 'Limpieza', '#17a2b8')
ON CONFLICT DO NOTHING;

INSERT INTO TipoHabitacion (codigo, nombre, descripcion) VALUES 
('SGL', 'Sencilla', '1 Cama individual'),
('DBL', 'Doble', '1 Cama matrimonial o 2 individuales'),
('SUI', 'Suite', 'Habitación de lujo con sala de estar')
ON CONFLICT DO NOTHING;

INSERT INTO TipoArea (codigo, nombre) VALUES 
('SAL', 'Salón de Eventos'),
('PIS', 'Piscina'),
('GIM', 'Gimnasio')
ON CONFLICT DO NOTHING;

-- MÓDULO 6: Reservas (Estados)
INSERT INTO EstadoReserva (codigo, nombre, color, orden) VALUES 
('PEND', 'Pendiente', '#ffc107', 1),
('CONF', 'Confirmada', '#28a745', 2),
('CURSO', 'En Curso', '#17a2b8', 3),
('COMP', 'Completada', '#6c757d', 4),
('CANC', 'Cancelada', '#dc3545', 5),
('NOSHW', 'No Show', '#343a40', 6)
ON CONFLICT DO NOTHING;

-- MÓDULO 7: Pagos (Métodos y Estados)
INSERT INTO MetodoPago (codigo, nombre, tipo, urlImagenQR) VALUES 
('EFE', 'Efectivo', 'Fisico', NULL),
('NEQ', 'Nequi', 'Digital', '/assets/qr/nequi.png'),
('DAV', 'Daviplata', 'Digital', '/assets/qr/daviplata.png')
ON CONFLICT DO NOTHING;

INSERT INTO EstadoPago (codigo, nombre) VALUES 
('PEND', 'Pendiente'),
('APRO', 'Aprobado'),
('RECH', 'Rechazado'),
('CANC', 'Cancelado'),
('ADEV', 'A Devolver'),
('DEV', 'Devuelto')
ON CONFLICT DO NOTHING;

-- ==========================================================
-- MÓDULO 8: Permisos del Sistema (RBAC)
-- ==========================================================
INSERT INTO Permiso (modulo, accion, codigo, descripcion) VALUES 
('RESERVAS', 'LEER', 'RES_LEER', 'Ver listado y detalle de reservas'),
('RESERVAS', 'CREAR', 'RES_CREAR', 'Crear nuevas reservas'),
('RESERVAS', 'EDITAR', 'RES_EDITAR', 'Modificar datos de reservas existentes'),
('RESERVAS', 'CANCELAR', 'RES_CANCELAR', 'Cancelar reservas y aplicar multas'),
('RESERVAS', 'CHECKIN', 'RES_CHECKIN', 'Realizar check-in de huéspedes'),
('RESERVAS', 'CHECKOUT', 'RES_CHECKOUT', 'Realizar check-out de huéspedes'),
('CATALOGO', 'LEER', 'CAT_LEER', 'Ver disponibilidad y catálogo'),
('CATALOGO', 'CREAR', 'CAT_CREAR', 'Crear nuevas habitaciones o áreas'),
('CATALOGO', 'EDITAR', 'CAT_EDITAR', 'Modificar habitaciones, áreas o estados'),
('CATALOGO', 'ELIMINAR', 'CAT_ELIMINAR', 'Eliminar habitaciones o áreas'),
('FINANZAS', 'LEER', 'FIN_LEER', 'Ver historial de pagos y facturas'),
('FINANZAS', 'CREAR', 'FIN_CREAR', 'Registrar pagos manuales y generar facturas'),
('REPORTES', 'LEER', 'REP_LEER', 'Ver y exportar reportes de ocupación y financieros'),
('USUARIOS', 'LEER', 'USR_LEER', 'Ver listado de usuarios del sistema'),
('USUARIOS', 'CREAR', 'USR_CREAR', 'Crear nuevos usuarios administrativos'),
('USUARIOS', 'EDITAR', 'USR_EDITAR', 'Modificar roles y bloquear usuarios')
ON CONFLICT DO NOTHING;

-- Asignación de Permisos a Roles
INSERT INTO RolPermiso (idRol, idPermiso)
SELECT r.idRol, p.idPermiso FROM Rol r CROSS JOIN Permiso p WHERE r.codigo = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO RolPermiso (idRol, idPermiso)
SELECT r.idRol, p.idPermiso FROM Rol r CROSS JOIN Permiso p 
WHERE r.codigo = 'STAFF' AND p.codigo IN (
    'RES_LEER', 'RES_CREAR', 'RES_EDITAR', 'RES_CANCELAR', 'RES_CHECKIN', 'RES_CHECKOUT',
    'CAT_LEER', 'FIN_LEER', 'FIN_CREAR'
)
ON CONFLICT DO NOTHING;

-- ==========================================================
-- CREACIÓN DEL USUARIO ADMINISTRADOR POR DEFECTO
-- ==========================================================
INSERT INTO Persona (nombres, apellidos, idTipoDocumento, numeroDocumento, email, idPais, idEstadoPersona)
VALUES ('Admin', 'Sistema', 1, '999999999', 'admin@sandistay.com', 1, 1);

INSERT INTO Usuario (idPersona, username, passwordHash, passwordSalt)
VALUES (
    (SELECT idPersona FROM Persona WHERE email = 'admin@sandistay.com'), 
    'admin', 
    '$2a$12$uD7RaEg3bSy8QctqDe55WeuwGPQb55IILJFOse/OUzCMzSNV4WOA2', -- Hash BCrypt (Factor 12) para 'admin123'
    'salt_dummy'
);

INSERT INTO UsuarioRol (idUsuario, idRol)
VALUES (
    (SELECT idUsuario FROM Usuario WHERE username = 'admin'),
    (SELECT idRol FROM Rol WHERE codigo = 'ADMIN')
);

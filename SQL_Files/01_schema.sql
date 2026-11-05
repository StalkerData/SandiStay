-- ============================================
-- EXTENSIONES NECESARIAS
-- ============================================
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- ============================================
-- MÓDULO 1: UBICACIÓN
-- ============================================

CREATE TABLE Pais (
    idPais INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(2) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    codigoTelefono VARCHAR(5) NOT NULL
);

-- ============================================
-- MÓDULO 2: PERSONAS (SIMPLIFICADO)
-- ============================================

CREATE TABLE TipoDocumento (
    idTipoDocumento INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE EstadoPersona (
    idEstado INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE Persona (
    idPersona INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    fechaNacimiento DATE,
    genero VARCHAR(10),
    idTipoDocumento INTEGER NOT NULL,
    numeroDocumento VARCHAR(50) NOT NULL,
    email VARCHAR(150),
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    ciudad VARCHAR(100),
    departamento VARCHAR(100),
    idPais INTEGER NOT NULL,
    idEstadoPersona INTEGER NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_persona_tipo_documento FOREIGN KEY (idTipoDocumento) REFERENCES TipoDocumento(idTipoDocumento),
    CONSTRAINT fk_persona_pais FOREIGN KEY (idPais) REFERENCES Pais(idPais),
    CONSTRAINT fk_persona_estado FOREIGN KEY (idEstadoPersona) REFERENCES EstadoPersona(idEstado),
    CONSTRAINT uq_persona_documento UNIQUE (idTipoDocumento, numeroDocumento)
);

CREATE TABLE ContactoPersona (
    idContacto INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idPersona INTEGER NOT NULL,
    nombreCompleto VARCHAR(200) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    relacion VARCHAR(50),
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_contacto_persona FOREIGN KEY (idPersona) REFERENCES Persona(idPersona) ON DELETE CASCADE
);

-- ============================================
-- MÓDULO 3: CLIENTES
-- ============================================

CREATE TABLE TipoCliente (
    idTipoCliente INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    porcentajeDescuento DECIMAL(5,2) DEFAULT 0.00,
    CONSTRAINT chk_porcentaje_descuento CHECK (porcentajeDescuento >= 0 AND porcentajeDescuento <= 100)
);

CREATE TABLE Cliente (
    idCliente INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idPersona INTEGER UNIQUE NOT NULL,
    codigoCliente VARCHAR(50) UNIQUE NOT NULL,
    idTipoCliente INTEGER NOT NULL,
    fechaRegistro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cliente_persona FOREIGN KEY (idPersona) REFERENCES Persona(idPersona),
    CONSTRAINT fk_cliente_tipo FOREIGN KEY (idTipoCliente) REFERENCES TipoCliente(idTipoCliente)
);

-- ============================================
-- MÓDULO 4: USUARIOS Y SEGURIDAD
-- ============================================

CREATE TABLE Usuario (
    idUsuario INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idPersona INTEGER UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    passwordHash VARCHAR(255) NOT NULL,
    passwordSalt VARCHAR(255) NOT NULL,
    intentosFallidos INTEGER DEFAULT 0,
    bloqueado BOOLEAN DEFAULT FALSE,
    ultimoLogin TIMESTAMP,
    requiereCambioPassword BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_persona FOREIGN KEY (idPersona) REFERENCES Persona(idPersona),
    CONSTRAINT chk_intentos_fallidos CHECK (intentosFallidos >= 0)
);

CREATE TABLE Rol (
    idRol INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    nivelAcceso INTEGER CHECK (nivelAcceso BETWEEN 1 AND 10),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE UsuarioRol (
    idUsuarioRol INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idUsuario INTEGER NOT NULL,
    idRol INTEGER NOT NULL,
    fechaAsignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idUsuarioAsigno INTEGER,
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (idRol) REFERENCES Rol(idRol),
    CONSTRAINT fk_usuario_rol_asigno FOREIGN KEY (idUsuarioAsigno) REFERENCES Usuario(idUsuario)
);

CREATE TABLE Permiso (
    idPermiso INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    codigo VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT
);

CREATE TABLE RolPermiso (
    idRol INTEGER NOT NULL,
    idPermiso INTEGER NOT NULL,
    PRIMARY KEY (idRol, idPermiso),
    CONSTRAINT fk_rol_permiso_rol FOREIGN KEY (idRol) REFERENCES Rol(idRol) ON DELETE CASCADE,
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (idPermiso) REFERENCES Permiso(idPermiso) ON DELETE CASCADE
);

CREATE TABLE Auditoria (
    idAuditoria INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idUsuario INTEGER,
    fechaHora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(20) NOT NULL,
    modulo VARCHAR(50),
    tabla VARCHAR(50),
    idRegistro INTEGER,
    descripcion TEXT,
    ipAddress VARCHAR(45),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario) ON DELETE SET NULL
);

-- ============================================
-- MÓDULO 5: HABITACIONES Y ÁREAS
-- ============================================

CREATE TABLE TipoHabitacion (
    idTipoHabitacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE EstadoLugar (
    idEstadoLugar INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    color VARCHAR(7)
);

CREATE TABLE Habitacion (
    idHabitacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    numero VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    idTipoHabitacion INTEGER NOT NULL,
    capacidadAdultos INTEGER NOT NULL,
    capacidadNinos INTEGER DEFAULT 0,
    capacidadMaxima INTEGER NOT NULL,
    idEstadoLugar INTEGER NOT NULL,
    precioNoche DECIMAL(10,2) NOT NULL,
    precioDia DECIMAL(10,2) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_habitacion_tipo FOREIGN KEY (idTipoHabitacion) REFERENCES TipoHabitacion(idTipoHabitacion),
    CONSTRAINT fk_habitacion_estado FOREIGN KEY (idEstadoLugar) REFERENCES EstadoLugar(idEstadoLugar),
    CONSTRAINT chk_capacidad_adultos CHECK (capacidadAdultos > 0),
    CONSTRAINT chk_capacidad_ninos CHECK (capacidadNinos >= 0),
    CONSTRAINT chk_capacidad_maxima CHECK (capacidadMaxima >= capacidadAdultos),
    CONSTRAINT chk_precio_noche CHECK (precioNoche >= 0),
    CONSTRAINT chk_precio_dia CHECK (precioDia >= 0)
);

CREATE TABLE TipoArea (
    idTipoArea INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE Area (
    idArea INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    idTipoArea INTEGER NOT NULL,
    capacidadPersonas INTEGER NOT NULL,
    idEstadoLugar INTEGER NOT NULL,
    precioHora DECIMAL(10,2) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_area_tipo FOREIGN KEY (idTipoArea) REFERENCES TipoArea(idTipoArea),
    CONSTRAINT fk_area_estado FOREIGN KEY (idEstadoLugar) REFERENCES EstadoLugar(idEstadoLugar),
    CONSTRAINT chk_capacidad_personas CHECK (capacidadPersonas > 0),
    CONSTRAINT chk_precio_hora CHECK (precioHora >= 0)
);

CREATE TABLE ImagenArea (
    idImagen INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idArea INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_imagen_area FOREIGN KEY (idArea) REFERENCES Area(idArea) ON DELETE CASCADE
);

CREATE TABLE ImagenHabitacion (
    idImagen INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idHabitacion INTEGER NOT NULL,
    url VARCHAR(500) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_imagen_habitacion FOREIGN KEY (idHabitacion) REFERENCES Habitacion(idHabitacion) ON DELETE CASCADE
);

-- ============================================
-- MÓDULO 6: RESERVAS
-- ============================================

CREATE TABLE EstadoReserva (
    idEstadoReserva INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    color VARCHAR(7),
    orden INTEGER
);

CREATE TABLE Reserva (
    idReserva INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigoReserva VARCHAR(50) UNIQUE NOT NULL,
    tipoReserva VARCHAR(20) NOT NULL CHECK (tipoReserva IN ('Habitacion', 'Area')),
    idCliente INTEGER NOT NULL,
    nombreHuesped VARCHAR(200) NOT NULL,
    documentoHuesped VARCHAR(50),
    telefonoHuesped VARCHAR(20),
    fechaReserva TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    numeroAdultos INTEGER DEFAULT 0,
    numeroNinos INTEGER DEFAULT 0,
    idEstadoReserva INTEGER NOT NULL,
    motivoCancelacion TEXT,
    fechaCancelacion TIMESTAMP,
    observaciones TEXT,
    idUsuarioCreacion INTEGER NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente),
    CONSTRAINT fk_reserva_estado FOREIGN KEY (idEstadoReserva) REFERENCES EstadoReserva(idEstadoReserva),
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (idUsuarioCreacion) REFERENCES Usuario(idUsuario),
    CONSTRAINT chk_numero_adultos CHECK (numeroAdultos >= 0),
    CONSTRAINT chk_numero_ninos CHECK (numeroNinos >= 0)
);

CREATE TABLE ReservaHabitacion (
    idReservaHabitacion INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idReserva INTEGER NOT NULL,
    idHabitacion INTEGER NOT NULL,
    fechaInicio DATE NOT NULL,
    fechaFin DATE NOT NULL,
    numeroNoches INTEGER NOT NULL,
    numeroDias INTEGER NOT NULL,
    precioNoche DECIMAL(10,2) NOT NULL,
    precioDia DECIMAL(10,2) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_reserva_habitacion_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_habitacion_habitacion FOREIGN KEY (idHabitacion) REFERENCES Habitacion(idHabitacion),
    CONSTRAINT chk_fechas_habitacion CHECK (fechaFin > fechaInicio),
    CONSTRAINT chk_numero_noches CHECK (numeroNoches > 0),
    CONSTRAINT chk_numero_dias CHECK (numeroDias > 0),
    CONSTRAINT chk_precio_noche_reserva CHECK (precioNoche >= 0),
    CONSTRAINT chk_precio_dia_reserva CHECK (precioDia >= 0),
    -- Prevención de solapamiento de reservas (double booking)
    EXCLUDE USING gist (
        idHabitacion WITH =,
        daterange(fechaInicio, fechaFin, '[]') WITH &&
    ) WHERE (activo = true)
);

CREATE TABLE ReservaArea (
    idReservaArea INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idReserva INTEGER NOT NULL,
    idArea INTEGER NOT NULL,
    fechaEvento DATE NOT NULL,
    horaInicio TIME NOT NULL,
    horaFin TIME NOT NULL,
    duracionHoras DECIMAL(5,2) NOT NULL,
    precioHora DECIMAL(10,2) NOT NULL,
    cantidadAsistentes INTEGER,
    requiereAnticipo BOOLEAN DEFAULT FALSE,
    porcentajeAnticipo DECIMAL(5,2) DEFAULT 0.00,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_reserva_area_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva) ON DELETE CASCADE,
    CONSTRAINT fk_reserva_area_area FOREIGN KEY (idArea) REFERENCES Area(idArea),
    CONSTRAINT chk_horas_area CHECK (horaFin > horaInicio),
    CONSTRAINT chk_duracion_horas CHECK (duracionHoras > 0),
    CONSTRAINT chk_precio_hora_reserva CHECK (precioHora >= 0),
    CONSTRAINT chk_cantidad_asistentes CHECK (cantidadAsistentes >= 0),
    CONSTRAINT chk_porcentaje_anticipo CHECK (porcentajeAnticipo >= 0 AND porcentajeAnticipo <= 100),
    -- Prevención de solapamiento de reservas de áreas
    EXCLUDE USING gist (
        idArea WITH =,
        fechaEvento WITH =,
        tsrange(
            (fechaEvento + horaInicio)::timestamp,
            (fechaEvento + horaFin)::timestamp,
            '[]'
        ) WITH &&
    ) WHERE (activo = true)
);

-- ============================================
-- MÓDULO 7: PAGOS
-- ============================================

CREATE TABLE MetodoPago (
    idMetodoPago INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    urlImagenQR VARCHAR(500),
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE EstadoPago (
    idEstadoPago INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo VARCHAR(10) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE Pago (
    idPago INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigoPago VARCHAR(50) UNIQUE NOT NULL,
    idReserva INTEGER NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    idMetodoPago INTEGER NOT NULL,
    numeroReferencia VARCHAR(100),
    idEstadoPago INTEGER NOT NULL,
    fechaPago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    idUsuarioRegistro INTEGER NOT NULL,
    observaciones TEXT,
    CONSTRAINT fk_pago_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva),
    CONSTRAINT fk_pago_metodo FOREIGN KEY (idMetodoPago) REFERENCES MetodoPago(idMetodoPago),
    CONSTRAINT fk_pago_estado FOREIGN KEY (idEstadoPago) REFERENCES EstadoPago(idEstadoPago),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (idUsuarioRegistro) REFERENCES Usuario(idUsuario),
    CONSTRAINT chk_monto_pago CHECK (monto > 0)
);

-- ============================================
-- MÓDULO 8: FACTURACIÓN
-- ============================================

CREATE TABLE Factura (
    idFactura INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numeroFactura VARCHAR(50) UNIQUE NOT NULL,
    idCliente INTEGER NOT NULL,
    idReserva INTEGER NOT NULL,
    fechaEmision TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Pagada', 'Pendiente', 'Anulada')),
    observaciones TEXT,
    idUsuarioCreacion INTEGER NOT NULL,
    fechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_factura_cliente FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente),
    CONSTRAINT fk_factura_reserva FOREIGN KEY (idReserva) REFERENCES Reserva(idReserva),
    CONSTRAINT fk_factura_usuario FOREIGN KEY (idUsuarioCreacion) REFERENCES Usuario(idUsuario)
);

CREATE TABLE FacturaDetalle (
    idFacturaDetalle INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    idFactura INTEGER NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad INTEGER NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    IVA DECIMAL(10,2) DEFAULT 0.00,
    total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_factura_detalle FOREIGN KEY (idFactura) REFERENCES Factura(idFactura) ON DELETE CASCADE,
    CONSTRAINT chk_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_iva CHECK (IVA >= 0),
    CONSTRAINT chk_total CHECK (total >= 0)
);

-- ============================================
-- MÓDULO 0: NEGOCIO 
-- ============================================
CREATE TABLE Negocio (
    nit VARCHAR(20) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    barrio VARCHAR(50) NOT NULL,
    ciudad VARCHAR(50) NOT NULL,
    país VARCHAR(50) NOT NULL
);


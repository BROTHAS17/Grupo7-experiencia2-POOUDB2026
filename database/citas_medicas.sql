-- =====================================================================
--  Base de datos: citas_medicas
--  Proyecto: Experiencia 2 POO UDB 2026 - Grupo 7
--  Entidades: pacientes, doctores, citas_medicas
--
--  Ejecutar completo en MySQL Workbench o desde consola:
--      mysql -u root -p < database/citas_medicas.sql
--
--  OJO: elimina y vuelve a crear las tablas (se pierden los datos).
-- =====================================================================

CREATE DATABASE IF NOT EXISTS citas_medicas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE citas_medicas;

DROP TABLE IF EXISTS citas_medicas;
DROP TABLE IF EXISTS doctores;
DROP TABLE IF EXISTS pacientes;

-- ---------------------------------------------------------------------
--  Tabla: pacientes
-- ---------------------------------------------------------------------
CREATE TABLE pacientes (
    id_paciente      INT           NOT NULL AUTO_INCREMENT,
    nombres          VARCHAR(100)  NOT NULL,
    apellidos        VARCHAR(100)  NOT NULL,
    dui              VARCHAR(10)   NULL,         -- formato 00000000-0 (menores pueden no tener)
    fecha_nacimiento DATE          NOT NULL,
    genero           ENUM('M', 'F') NOT NULL,
    telefono         VARCHAR(15)   NULL,
    correo           VARCHAR(100)  NULL,
    direccion        VARCHAR(255)  NULL,
    fecha_registro   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_pacientes PRIMARY KEY (id_paciente),
    CONSTRAINT uq_pacientes_dui UNIQUE (dui)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: doctores
-- ---------------------------------------------------------------------
CREATE TABLE doctores (
    id_doctor        INT           NOT NULL AUTO_INCREMENT,
    nombres          VARCHAR(100)  NOT NULL,
    apellidos        VARCHAR(100)  NOT NULL,
    especialidad     VARCHAR(100)  NOT NULL,
    numero_jvpm      VARCHAR(10)   NOT NULL,     -- número de la Junta de Vigilancia de la Profesión Médica
    telefono         VARCHAR(15)   NULL,
    correo           VARCHAR(100)  NULL,
    CONSTRAINT pk_doctores PRIMARY KEY (id_doctor),
    CONSTRAINT uq_doctores_jvpm UNIQUE (numero_jvpm)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
--  Tabla: citas_medicas  (relaciona un paciente con un doctor)
-- ---------------------------------------------------------------------
CREATE TABLE citas_medicas (
    id_cita          INT           NOT NULL AUTO_INCREMENT,
    id_paciente      INT           NOT NULL,
    id_doctor        INT           NOT NULL,
    fecha            DATE          NOT NULL,
    hora             TIME          NOT NULL,
    motivo           VARCHAR(255)  NOT NULL,
    estado           ENUM('PROGRAMADA', 'ATENDIDA', 'CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',
    observaciones    TEXT          NULL,
    CONSTRAINT pk_citas_medicas PRIMARY KEY (id_cita),
    CONSTRAINT fk_citas_paciente FOREIGN KEY (id_paciente)
        REFERENCES pacientes (id_paciente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_citas_doctor FOREIGN KEY (id_doctor)
        REFERENCES doctores (id_doctor)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_citas_doctor_fecha (id_doctor, fecha)
) ENGINE = InnoDB;

-- =====================================================================
--  Datos de prueba
-- =====================================================================
INSERT INTO pacientes (nombres, apellidos, dui, fecha_nacimiento, genero, telefono, correo, direccion) VALUES
    ('Carlos Alberto', 'Martínez López',  '04567891-2', '1990-03-15', 'M', '7012-3456', 'carlos.martinez@correo.com', 'Col. Escalón, San Salvador'),
    ('María José',     'Hernández Rivas', '05678912-3', '1985-07-22', 'F', '7123-4567', 'maria.hernandez@correo.com', 'Santa Tecla, La Libertad'),
    ('José Antonio',   'Ramírez Flores',  '03456789-1', '1978-11-02', 'M', '7234-5678', NULL,                         'Soyapango, San Salvador'),
    ('Ana Sofía',      'Guzmán Pérez',    NULL,         '2012-05-30', 'F', '7345-6789', NULL,                         'Mejicanos, San Salvador'),
    ('Luis Fernando',  'Castillo Mejía',  '06789123-4', '2000-01-18', 'M', '7456-7890', 'luis.castillo@correo.com',   'Antiguo Cuscatlán, La Libertad');

INSERT INTO doctores (nombres, apellidos, especialidad, numero_jvpm, telefono, correo) VALUES
    ('Roberto Ernesto', 'Aguilar Sánchez', 'Medicina General', '12345', '2222-1111', 'raguilar@clinica.com'),
    ('Claudia Patricia', 'Morales Díaz',   'Pediatría',        '23456', '2222-2222', 'cmorales@clinica.com'),
    ('Fernando José',   'Portillo Cruz',   'Cardiología',      '34567', '2222-3333', 'fportillo@clinica.com'),
    ('Gabriela María',  'Navarro Orellana','Ginecología',      '45678', '2222-4444', 'gnavarro@clinica.com');

INSERT INTO citas_medicas (id_paciente, id_doctor, fecha, hora, motivo, estado, observaciones) VALUES
    (1, 1, '2026-10-12', '08:00:00', 'Chequeo general',                 'PROGRAMADA', NULL),
    (2, 4, '2026-10-12', '09:30:00', 'Control ginecológico anual',      'PROGRAMADA', NULL),
    (3, 3, '2026-10-05', '10:00:00', 'Dolor en el pecho',               'ATENDIDA',   'Se indicó electrocardiograma.'),
    (4, 2, '2026-10-13', '14:00:00', 'Control de crecimiento',          'PROGRAMADA', NULL),
    (5, 1, '2026-10-06', '11:00:00', 'Fiebre y dolor de garganta',      'CANCELADA',  'El paciente canceló por teléfono.'),
    (1, 3, '2026-10-20', '15:30:00', 'Evaluación de presión arterial',  'PROGRAMADA', NULL);

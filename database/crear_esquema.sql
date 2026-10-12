-- =====================================================================
--  Base de datos: citas_medicas
--  Proyecto: Experiencia 2 POO UDB 2026 - Grupo 7
--  Script NO destructivo para crear las tablas que todavía no existen.
--  No elimina tablas ni inserta datos de prueba.
--
--  Ejecutar en MySQL Workbench o desde consola:
--      mysql -u root -p < database/crear_esquema.sql
--  CREATE TABLE IF NOT EXISTS conserva las tablas que ya existen; no actualiza
--  automáticamente una estructura antigua.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS citas_medicas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE citas_medicas;

-- ---------------------------------------------------------------------
--  Tabla: pacientes
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS pacientes (
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
CREATE TABLE IF NOT EXISTS doctores (
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
CREATE TABLE IF NOT EXISTS citas_medicas (
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


-- =====================================================================
--  Sentencias SQL básicas (inserción, actualización y consulta)
--  Base de datos: citas_medicas
--
--  Son las mismas sentencias que usan las clases DAO en Java
--  (sv.edu.udb.clinica.dao). En Java los valores se reemplazan por "?"
--  y se asignan con PreparedStatement para evitar inyección SQL.
-- =====================================================================

USE citas_medicas;

-- ---------------------------------------------------------------------
--  PACIENTES
-- ---------------------------------------------------------------------

-- Inserción
INSERT INTO pacientes (nombres, apellidos, dui, fecha_nacimiento, genero, telefono, correo, direccion)
VALUES ('Karla Beatriz', 'Romero Alvarado', '07891234-5', '1995-09-10', 'F', '7567-8901', 'karla.romero@correo.com', 'San Miguel');

-- Actualización
UPDATE pacientes
SET telefono = '7999-0000',
    correo   = 'karla.romero@nuevo.com',
    direccion = 'Usulután'
WHERE id_paciente = 6;

-- Consulta: todos los pacientes
SELECT id_paciente, nombres, apellidos, dui, fecha_nacimiento, genero, telefono, correo, direccion, fecha_registro
FROM pacientes
ORDER BY apellidos, nombres;

-- Consulta: un paciente por id
SELECT * FROM pacientes WHERE id_paciente = 1;

-- Consulta: buscar por DUI
SELECT * FROM pacientes WHERE dui = '04567891-2';

-- ---------------------------------------------------------------------
--  DOCTORES
-- ---------------------------------------------------------------------

-- Inserción
INSERT INTO doctores (nombres, apellidos, especialidad, numero_jvpm, telefono, correo)
VALUES ('Mario Alfredo', 'Quintanilla Ruiz', 'Dermatología', '56789', '2222-5555', 'mquintanilla@clinica.com');

-- Actualización
UPDATE doctores
SET especialidad = 'Dermatología Pediátrica',
    telefono     = '2222-5556'
WHERE id_doctor = 5;

-- Consulta: todos los doctores
SELECT id_doctor, nombres, apellidos, especialidad, numero_jvpm, telefono, correo
FROM doctores
ORDER BY apellidos, nombres;

-- Consulta: doctores por especialidad
SELECT * FROM doctores WHERE especialidad = 'Medicina General';

-- ---------------------------------------------------------------------
--  CITAS MÉDICAS
-- ---------------------------------------------------------------------

-- Inserción
INSERT INTO citas_medicas (id_paciente, id_doctor, fecha, hora, motivo)
VALUES (6, 5, '2026-10-21', '10:30:00', 'Revisión de lunares');

-- Actualización: reprogramar una cita
UPDATE citas_medicas
SET fecha = '2026-10-22',
    hora  = '09:00:00'
WHERE id_cita = 7;

-- Actualización: marcar una cita como atendida
UPDATE citas_medicas
SET estado = 'ATENDIDA',
    observaciones = 'Sin hallazgos de riesgo.'
WHERE id_cita = 7;

-- Consulta: todas las citas con el nombre del paciente y del doctor
SELECT c.id_cita,
       c.fecha,
       c.hora,
       CONCAT(p.nombres, ' ', p.apellidos) AS paciente,
       CONCAT(d.nombres, ' ', d.apellidos) AS doctor,
       d.especialidad,
       c.motivo,
       c.estado
FROM citas_medicas c
INNER JOIN pacientes p ON p.id_paciente = c.id_paciente
INNER JOIN doctores  d ON d.id_doctor   = c.id_doctor
ORDER BY c.fecha, c.hora;

-- Consulta: citas de un paciente
SELECT * FROM citas_medicas WHERE id_paciente = 1 ORDER BY fecha, hora;

-- Consulta: agenda de un doctor en una fecha
SELECT * FROM citas_medicas
WHERE id_doctor = 1 AND fecha = '2026-10-12'
ORDER BY hora;

-- Consulta: verificar si el doctor ya tiene una cita activa en ese horario
SELECT COUNT(*) AS citas_en_horario
FROM citas_medicas
WHERE id_doctor = 1
  AND fecha = '2026-10-12'
  AND hora = '08:00:00'
  AND estado <> 'CANCELADA';

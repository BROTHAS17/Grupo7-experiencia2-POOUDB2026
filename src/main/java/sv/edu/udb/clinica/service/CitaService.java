package sv.edu.udb.clinica.service;

import java.sql.SQLException;
import java.util.List;
import sv.edu.udb.clinica.dao.CitaMedicaDAO;
import sv.edu.udb.clinica.excepciones.CitaDuplicadaException;
import sv.edu.udb.clinica.modelo.CitaMedica;

public class CitaService {
    private CitaMedicaDAO citaDAO = new CitaMedicaDAO();

    /**
     * Registra una nueva cita médica aplicando validaciones y control de duplicidad de horarios.
     */
    public int registrarCita(CitaMedica cita) throws CitaDuplicadaException, SQLException, IllegalArgumentException {
        // Validación de campos obligatorios
        if (cita.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria.");
        }
        if (cita.getHora() == null) {
            throw new IllegalArgumentException("La hora de la cita es obligatoria.");
        }
        if (cita.getMotivo() == null || cita.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la consulta no puede estar vacío.");
        }

        // Regla de negocio: Verificar si el doctor ya tiene cita en ese horario y no está cancelada
        if (citaDAO.existeCitaEnHorario(cita)) {
            throw new CitaDuplicadaException("El doctor seleccionado ya tiene una cita activa asignada en esta misma fecha y hora.");
        }

        // Sitodo es correcto, procede a insertar mediante el DAO
        return citaDAO.insertar(cita);
    }

    /**
     * Actualizar los datos de una cita existente
     */
    public boolean actualizarCita(CitaMedica cita) throws CitaDuplicadaException, SQLException, IllegalArgumentException {
        if (cita.getFecha() == null || cita.getHora() == null) {
            throw new IllegalArgumentException("La fecha y la hora son obligatorias para la actualización.");
        }

        if (citaDAO.existeCitaEnHorario(cita)) {
            throw new CitaDuplicadaException("No se puede reprogramar: el doctor ya tiene otra cita activa en ese horario.");
        }

        return citaDAO.actualizar(cita);
    }

    public boolean cambiarEstadoCita(int idCita, CitaMedica.Estado nuevoEstado) throws SQLException {
        return citaDAO.actualizarEstado(idCita, nuevoEstado);
    }

    public List<CitaMedica> listarCitas() throws SQLException {
        return citaDAO.listar();
    }
}
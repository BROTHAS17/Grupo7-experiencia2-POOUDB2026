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
        validarCita(cita);
        return citaDAO.insertarSiDisponible(cita);
    }

    /** Actualiza una cita sin bloquearse a sí misma y valida el nuevo horario. */
    public boolean actualizarCita(CitaMedica cita) throws CitaDuplicadaException, SQLException, IllegalArgumentException {
        validarCita(cita);
        if (cita.getIdCita() <= 0) {
            throw new IllegalArgumentException("El identificador de la cita es obligatorio para actualizar.");
        }
        return citaDAO.actualizarSiDisponible(cita);
    }

    private void validarCita(CitaMedica cita) {
        if (cita == null) {
            throw new IllegalArgumentException("Los datos de la cita son obligatorios.");
        }
        if (cita.getIdPaciente() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un paciente válido.");
        }
        if (cita.getIdDoctor() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un doctor válido.");
        }
        if (cita.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la cita es obligatoria.");
        }
        if (cita.getHora() == null) {
            throw new IllegalArgumentException("La hora de la cita es obligatoria.");
        }
        if (cita.getMotivo() == null || cita.getMotivo().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la consulta no puede estar vacío.");
        }
        if (cita.getEstado() == null) {
            throw new IllegalArgumentException("El estado de la cita es obligatorio.");
        }
    }

    public boolean cambiarEstadoCita(int idCita, CitaMedica.Estado nuevoEstado) throws SQLException {
        return citaDAO.actualizarEstado(idCita, nuevoEstado);
    }

    public List<CitaMedica> listarCitas() throws SQLException {
        return citaDAO.listar();
    }
}
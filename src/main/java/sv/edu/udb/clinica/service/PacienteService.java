package sv.edu.udb.clinica.service;

import java.sql.SQLException;
import java.util.List;
import sv.edu.udb.clinica.dao.PacienteDAO;
import sv.edu.udb.clinica.modelo.Paciente;

public class PacienteService {
    private PacienteDAO pacienteDAO = new PacienteDAO();

    public int registrarPaciente(Paciente paciente) throws SQLException, IllegalArgumentException {
        // Validaciones de negocio básicas
        if (paciente.getNombres() == null || paciente.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio.");
        }
        if (paciente.getApellidos() == null || paciente.getApellidos().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del paciente es obligatorio.");
        }
        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }

        // Verificar si el DUI ya se encuentra registrado (evitar duplicados si trae DUI)
        if (paciente.getDui() != null && !paciente.getDui().trim().isEmpty()) {
            Paciente existente = pacienteDAO.buscarPorDui(paciente.getDui());
            if (existente != null) {
                throw new IllegalArgumentException("Ya existe un paciente registrado con el número de DUI ingresado.");
            }
        }

        return pacienteDAO.insertar(paciente);
    }

    public boolean actualizarPaciente(Paciente paciente) throws SQLException, IllegalArgumentException {
        if (paciente.getNombres() == null || paciente.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente no puede estar vacío.");
        }
        return pacienteDAO.actualizar(paciente);
    }

    public List<Paciente> listarPacientes() throws SQLException {
        return pacienteDAO.listar();
    }
}
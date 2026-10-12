package sv.edu.udb.clinica.service;

import java.time.LocalDate;
import java.util.List;
import java.sql.SQLException;
import sv.edu.udb.clinica.dao.PacienteDAO;
import sv.edu.udb.clinica.modelo.Paciente;

public class PacienteService {
    private final PacienteDAO pacienteDAO = new PacienteDAO();

    public int registrarPaciente(Paciente paciente) throws SQLException, IllegalArgumentException {
        validarPaciente(paciente);
        validarDuiUnico(paciente);
        return pacienteDAO.insertar(paciente);
    }

    public boolean actualizarPaciente(Paciente paciente) throws SQLException, IllegalArgumentException {
        validarPaciente(paciente);
        if (paciente.getIdPaciente() <= 0) {
            throw new IllegalArgumentException("El identificador del paciente es obligatorio para actualizar.");
        }
        validarDuiUnico(paciente);
        return pacienteDAO.actualizar(paciente);
    }

    private void validarPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("Los datos del paciente son obligatorios.");
        }
        validarTextoObligatorio(paciente.getNombres(), "El nombre del paciente es obligatorio.", 100);
        validarTextoObligatorio(paciente.getApellidos(), "El apellido del paciente es obligatorio.", 100);
        if (paciente.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede estar en el futuro.");
        }
        if (paciente.getGenero() == null || !(paciente.getGenero().equals("M") || paciente.getGenero().equals("F"))) {
            throw new IllegalArgumentException("Seleccione un género válido (M o F).");
        }
        paciente.setNombres(paciente.getNombres().trim());
        paciente.setApellidos(paciente.getApellidos().trim());
        String dui = paciente.getDui();
        if (dui != null) {
            dui = dui.trim();
            paciente.setDui(dui.isEmpty() ? null : dui);
        }
        validarLongitudOpcional(paciente.getDui(), "El DUI", 10);
        validarLongitudOpcional(paciente.getTelefono(), "El teléfono", 15);
        validarLongitudOpcional(paciente.getCorreo(), "El correo electrónico", 100);
        validarLongitudOpcional(paciente.getDireccion(), "La dirección", 255);
    }

    private void validarDuiUnico(Paciente paciente) throws SQLException {
        if (paciente.getDui() == null) {
            return;
        }
        Paciente existente = pacienteDAO.buscarPorDui(paciente.getDui());
        if (existente != null && existente.getIdPaciente() != paciente.getIdPaciente()) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con el número de DUI ingresado.");
        }
    }

    private void validarTextoObligatorio(String valor, String mensaje, int maximo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
        if (valor.trim().length() > maximo) {
            throw new IllegalArgumentException("El texto no puede superar " + maximo + " caracteres.");
        }
    }

    private void validarLongitudOpcional(String valor, String campo, int maximo) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(campo + " no puede superar " + maximo + " caracteres.");
        }
    }

    public List<Paciente> listarPacientes() throws SQLException {
        return pacienteDAO.listar();
    }
}

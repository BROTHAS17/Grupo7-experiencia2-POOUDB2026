package sv.edu.udb.clinica.service;

import java.sql.SQLException;
import java.util.List;
import sv.edu.udb.clinica.dao.DoctorDAO;
import sv.edu.udb.clinica.modelo.Doctor;

public class DoctorService {
    private final DoctorDAO doctorDAO = new DoctorDAO();

    public int registrarDoctor(Doctor doctor) throws SQLException, IllegalArgumentException {
        validarDoctor(doctor);
        validarJvpmUnico(doctor);
        return doctorDAO.insertar(doctor);
    }

    public boolean actualizarDoctor(Doctor doctor) throws SQLException, IllegalArgumentException {
        validarDoctor(doctor);
        if (doctor.getIdDoctor() <= 0) {
            throw new IllegalArgumentException("El identificador del doctor es obligatorio para actualizar.");
        }
        validarJvpmUnico(doctor);
        return doctorDAO.actualizar(doctor);
    }

    private void validarDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Los datos del doctor son obligatorios.");
        }
        validarTextoObligatorio(doctor.getNombres(), "El nombre del doctor es obligatorio.", 100);
        validarTextoObligatorio(doctor.getApellidos(), "El apellido del doctor es obligatorio.", 100);
        validarTextoObligatorio(doctor.getEspecialidad(), "La especialidad médica es obligatoria.", 100);
        validarTextoObligatorio(doctor.getNumeroJvpm(), "El número de JVPM es obligatorio.", 10);
        doctor.setNombres(doctor.getNombres().trim());
        doctor.setApellidos(doctor.getApellidos().trim());
        doctor.setEspecialidad(doctor.getEspecialidad().trim());
        doctor.setNumeroJvpm(doctor.getNumeroJvpm().trim());
        validarLongitudOpcional(doctor.getTelefono(), "El teléfono", 15);
        validarLongitudOpcional(doctor.getCorreo(), "El correo electrónico", 100);
    }

    private void validarJvpmUnico(Doctor doctor) throws SQLException {
        Doctor existente = doctorDAO.buscarPorNumeroJvpm(doctor.getNumeroJvpm());
        if (existente != null && existente.getIdDoctor() != doctor.getIdDoctor()) {
            throw new IllegalArgumentException("Ya existe un doctor registrado con el número de JVPM ingresado.");
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

    public List<Doctor> listarDoctores() throws SQLException {
        return doctorDAO.listar();
    }
}

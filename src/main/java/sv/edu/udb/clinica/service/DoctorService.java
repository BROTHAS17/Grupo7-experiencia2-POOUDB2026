package sv.edu.udb.clinica.service;

import java.sql.SQLException;
import java.util.List;
import sv.edu.udb.clinica.dao.DoctorDAO;
import sv.edu.udb.clinica.modelo.Doctor;

public class DoctorService {
    private DoctorDAO doctorDAO = new DoctorDAO();

    public int registrarDoctor(Doctor doctor) throws SQLException, IllegalArgumentException {
        if (doctor.getNombres() == null || doctor.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del doctor es obligatorio.");
        }
        if (doctor.getEspecialidad() == null || doctor.getEspecialidad().trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad médica es obligatoria.");
        }
        if (doctor.getNumeroJvpm() == null || doctor.getNumeroJvpm().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de JVPM es obligatorio.");
        }

        return doctorDAO.insertar(doctor);
    }

    public boolean actualizarDoctor(Doctor doctor) throws SQLException, IllegalArgumentException {
        if (doctor.getNombres() == null || doctor.getNombres().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del doctor no puede estar vacío.");
        }
        return doctorDAO.actualizar(doctor);
    }

    public List<Doctor> listarDoctores() throws SQLException {
        return doctorDAO.listar();
    }
}
package sv.edu.udb.clinica.ui;

import java.sql.SQLException;
import java.util.List;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.modelo.Doctor;
import sv.edu.udb.clinica.modelo.Paciente;
import sv.edu.udb.clinica.service.CitaService;
import sv.edu.udb.clinica.service.DoctorService;
import sv.edu.udb.clinica.service.PacienteService;

// Agrupa los servicios del backend que usan las pantallas

public class Servicios {

    public record Datos(List<Paciente> pacientes, List<Doctor> doctores, List<CitaMedica> citas) {
    }

    private final PacienteService pacientes = new PacienteService();
    private final DoctorService doctores = new DoctorService();
    private final CitaService citas = new CitaService();

    public PacienteService pacientes() {
        return pacientes;
    }

    public DoctorService doctores() {
        return doctores;
    }

    public CitaService citas() {
        return citas;
    }

    public Datos cargarTodo() throws SQLException {
        return new Datos(pacientes.listarPacientes(), doctores.listarDoctores(), citas.listarCitas());
    }
}

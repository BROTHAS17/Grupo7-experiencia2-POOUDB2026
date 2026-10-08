package sv.edu.udb.clinica.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un registro de la tabla pacientes.
 * Hereda de la clase abstracta Persona.
 */
public class Paciente extends Persona {
    private int idPaciente;
    private String dui;
    private LocalDate fechaNacimiento;
    private String genero; // "M" o "F"
    private String direccion;
    private LocalDateTime fechaRegistro;

    public Paciente() {
        super();
    }

    public Paciente(String nombres, String apellidos, String dui, LocalDate fechaNacimiento,
                    String genero, String telefono, String correo, String direccion) {
        super(nombres, apellidos, telefono, correo);
        this.dui = dui;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.direccion = direccion;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return idPaciente + " - " + getNombreCompleto();
    }
}
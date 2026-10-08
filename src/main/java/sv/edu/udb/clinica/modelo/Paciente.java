package sv.edu.udb.clinica.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa un registro de la tabla pacientes.
 */
public class Paciente {

    private int idPaciente;
    private String nombres;
    private String apellidos;
    private String dui;
    private LocalDate fechaNacimiento;
    private String genero; // "M" o "F"
    private String telefono;
    private String correo;
    private String direccion;
    private LocalDateTime fechaRegistro;

    public Paciente() {
    }

    public Paciente(String nombres, String apellidos, String dui, LocalDate fechaNacimiento,
            String genero, String telefono, String correo, String direccion) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.dui = dui;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return idPaciente + " - " + getNombreCompleto();
    }
}

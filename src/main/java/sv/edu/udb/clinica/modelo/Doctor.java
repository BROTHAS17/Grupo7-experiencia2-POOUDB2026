package sv.edu.udb.clinica.modelo;

/**
 * Representa un registro de la tabla doctores.
 */
public class Doctor {

    private int idDoctor;
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String numeroJvpm;
    private String telefono;
    private String correo;

    public Doctor() {
    }

    public Doctor(String nombres, String apellidos, String especialidad, String numeroJvpm,
            String telefono, String correo) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.numeroJvpm = numeroJvpm;
        this.telefono = telefono;
        this.correo = correo;
    }

    public int getIdDoctor() {
        return idDoctor;
    }

    public void setIdDoctor(int idDoctor) {
        this.idDoctor = idDoctor;
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

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getNumeroJvpm() {
        return numeroJvpm;
    }

    public void setNumeroJvpm(String numeroJvpm) {
        this.numeroJvpm = numeroJvpm;
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

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Dr(a). " + getNombreCompleto() + " - " + especialidad;
    }
}

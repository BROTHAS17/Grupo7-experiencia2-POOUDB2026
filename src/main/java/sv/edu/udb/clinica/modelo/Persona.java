package sv.edu.udb.clinica.modelo;

/**
 * Clase abstracta para los datos comunes de cualquier persona en la clínica.
 */
public abstract class Persona {
    private String nombres;
    private String apellidos;
    private String telefono;
    private String correo;

    public Persona() {
    }

    public Persona(String nombres, String apellidos, String telefono, String correo) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
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
}
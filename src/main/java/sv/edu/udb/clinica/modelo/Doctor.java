package sv.edu.udb.clinica.modelo;

/**
 * Representa un registro de la tabla doctores.
 * Hereda de la clase abstracta Persona.
 */
public class Doctor extends Persona {
    private int idDoctor;
    private String especialidad;
    private String numeroJvpm;

    public Doctor() {
        super();
    }

    public Doctor(String nombres, String apellidos, String especialidad, String numeroJvpm,
                  String telefono, String correo) {
        super(nombres, apellidos, telefono, correo);
        this.especialidad = especialidad;
        this.numeroJvpm = numeroJvpm;
    }

    public int getIdDoctor() {
        return idDoctor;
    }

    public void setIdDoctor(int idDoctor) {
        this.idDoctor = idDoctor;
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

    @Override
    public String toString() {
        return "Dr(a). " + getNombreCompleto() + " - " + especialidad;
    }
}
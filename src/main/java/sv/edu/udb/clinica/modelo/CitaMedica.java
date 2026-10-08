package sv.edu.udb.clinica.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa un registro de la tabla citas_medicas.
 */
public class CitaMedica {

    /** Valores permitidos para la columna estado. */
    public enum Estado {
        PROGRAMADA, ATENDIDA, CANCELADA
    }

    private int idCita;
    private int idPaciente;
    private int idDoctor;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Estado estado = Estado.PROGRAMADA;
    private String observaciones;

    // Solo lectura: se llenan al consultar (JOIN), no se guardan en citas_medicas.
    private String nombrePaciente;
    private String nombreDoctor;

    public CitaMedica() {
    }

    public CitaMedica(int idPaciente, int idDoctor, LocalDate fecha, LocalTime hora, String motivo) {
        this.idPaciente = idPaciente;
        this.idDoctor = idDoctor;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public int getIdDoctor() {
        return idDoctor;
    }

    public void setIdDoctor(int idDoctor) {
        this.idDoctor = idDoctor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getNombreDoctor() {
        return nombreDoctor;
    }

    public void setNombreDoctor(String nombreDoctor) {
        this.nombreDoctor = nombreDoctor;
    }

    @Override
    public String toString() {
        return "Cita " + idCita + ": " + fecha + " " + hora + " - " + motivo + " (" + estado + ")";
    }
}

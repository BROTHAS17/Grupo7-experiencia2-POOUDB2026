package sv.edu.udb.clinica.excepciones;

/**
 * Excepción personalizada se ocupara cuando se intenta programar una cita
 * en un horario en el que el doctor ya se tenga otra cita.
 */
public class CitaDuplicadaException extends Exception {
    public CitaDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
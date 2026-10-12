package sv.edu.udb.clinica.ui.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import sv.edu.udb.clinica.modelo.CitaMedica;

// Textos y formatos que se muestran al usuario (fechas, horas, estados y género)

public final class Etiquetas {

    public static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final Locale ES = Locale.forLanguageTag("es-SV");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES);

    private Etiquetas() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String hora(LocalTime hora) {
        return hora == null ? "" : HORA.format(hora);
    }

    // Por ejemplo "Domingo 11 de octubre de 2026"
    public static String fechaLarga(LocalDate fecha) {
        String s = FECHA_LARGA.format(fecha);
        return s.substring(0, 1).toUpperCase(ES) + s.substring(1);
    }

    public static String estado(CitaMedica.Estado estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case PROGRAMADA -> "Programada";
            case ATENDIDA -> "Atendida";
            case CANCELADA -> "Cancelada";
        };
    }

    // La base de datos guarda "M" o "F"
    public static String genero(String codigo) {
        if ("M".equals(codigo)) {
            return "Masculino";
        }
        if ("F".equals(codigo)) {
            return "Femenino";
        }
        return codigo == null ? "" : codigo;
    }

    // Texto para mostrar en una tabla, una raya cuando el dato no existe
    public static String vacio(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    public static String plural(long cantidad, String singular, String plural) {
        return cantidad + " " + (cantidad == 1 ? singular : plural);
    }
}

package sv.edu.udb.clinica.ui.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;
import sv.edu.udb.clinica.ui.componentes.CampoFecha;

// Validaciones de los formularios
// Cada método devuelve un mensaje de error si el dato es incorrecto o null cuando es correcto

public final class Validador {

    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}[\\p{L} .'\\-]*$");
    private static final Pattern DUI = Pattern.compile("^\\d{8}-\\d$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{4}-\\d{4}$");
    private static final Pattern JVPM = Pattern.compile("^\\d{3,10}$");
    private static final Pattern CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.[A-Za-z]{2,}$");

    private static final int EDAD_MAXIMA = 120;

    private Validador() {
    }

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // Texto sin espacios sobrantes, o null si quedó vacío (así la base de datos guarda NULL)
    public static String nulo(String valor) {
        String v = texto(valor);
        return v.isEmpty() ? null : v;
    }

    // Nombres y apellidos, obligatorios, solo letras y mínimo dos caracteres
    public static String nombre(String valor, String descripcion) {
        String v = texto(valor);
        if (v.isEmpty()) {
            return "Ingrese " + descripcion + ".";
        }
        if (v.length() < 2 || !NOMBRE.matcher(v).matches()) {
            return "Use solo letras (mínimo 2).";
        }
        return null;
    }

    // El DUI es opcional (los menores pueden no tenerlo) pero si se escribe debe estar completo
    public static String dui(String valor) {
        String v = texto(valor);
        if (v.isEmpty()) {
            return null;
        }
        return DUI.matcher(v).matches() ? null : "Formato: 00000000-0";
    }

    public static String telefono(String valor) {
        String v = texto(valor);
        if (v.isEmpty()) {
            return "Ingrese el teléfono.";
        }
        return TELEFONO.matcher(v).matches() ? null : "Formato: 0000-0000";
    }

    public static String correo(String valor) {
        String v = texto(valor);
        if (v.isEmpty()) {
            return null;
        }
        return CORREO.matcher(v).matches() ? null : "Correo no válido (ej. nombre@correo.com).";
    }

    public static String jvpm(String valor) {
        String v = texto(valor);
        if (v.isEmpty()) {
            return "Ingrese el número de JVPM.";
        }
        return JVPM.matcher(v).matches() ? null : "Solo dígitos (de 3 a 10).";
    }

    public static String fechaNacimiento(CampoFecha campo) {
        if (campo.estaVacio()) {
            return "Ingrese la fecha.";
        }
        LocalDate fecha = campo.getFecha();
        if (fecha == null) {
            return "Fecha no válida (dd/mm/aaaa).";
        }
        if (fecha.isAfter(LocalDate.now())) {
            return "No puede ser una fecha futura.";
        }
        if (edad(fecha) > EDAD_MAXIMA) {
            return "Revise el año de nacimiento.";
        }
        return null;
    }

    public static int edad(LocalDate nacimiento) {
        return Period.between(nacimiento, LocalDate.now()).getYears();
    }
}

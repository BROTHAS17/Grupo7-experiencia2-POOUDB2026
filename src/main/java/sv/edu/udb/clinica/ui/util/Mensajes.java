package sv.edu.udb.clinica.ui.util;

import java.awt.Component;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.swing.JOptionPane;
import sv.edu.udb.clinica.excepciones.CitaDuplicadaException;
import sv.edu.udb.clinica.util.LogErrores;
import sv.edu.udb.clinica.util.LogErrores.Nivel;

// Convierte las excepciones técnicas en mensajes claros para el usuario y las deja registradas
// en el log. Los textos de las excepciones del backend (IllegalArgumentException y
// CitaDuplicadaException) ya están escritos para el usuario y se muestran tal cual

public final class Mensajes {

    // Quien muestra los avisos breves (la barra de estado de la ventana principal)
    public interface Notificador {
        void exito(String mensaje);

        void error(String mensaje);

        void info(String mensaje);
    }

    public record Traduccion(Nivel nivel, String titulo, String mensaje) {
    }

    private static Notificador notificador;
    private static String ultimoError = "";
    private static long ultimoErrorHora;

    private Mensajes() {
    }

    public static void setNotificador(Notificador n) {
        notificador = n;
    }

    // Avisos breves

    public static void exito(String mensaje) {
        LogErrores.getInstancia().info("Interfaz", mensaje);
        if (notificador != null) {
            notificador.exito(mensaje);
        }
    }

    public static void info(String mensaje) {
        if (notificador != null) {
            notificador.info(mensaje);
        }
    }

    // Aviso de formulario incompleto, solo va a la barra de estado porque los campos ya están en rojo
    public static void validacion(String mensaje) {
        if (notificador != null) {
            notificador.error(mensaje);
        }
    }

    // Errores

    public static void error(Component padre, String accion, Throwable causa) {
        Traduccion t = traducir(causa);
        registrar(accion, t, causa);
        if (notificador != null) {
            notificador.error(t.mensaje());
        }
        boolean tecnico = t.nivel() == Nivel.ERROR;
        String html = "<html><body style='width:340px'>" + escapar(t.mensaje())
                + (tecnico ? "<br><br><span style='color:gray'>El detalle técnico quedó guardado en el "
                        + "Registro de errores.</span>" : "")
                + "</body></html>";
        JOptionPane.showMessageDialog(padre, html, t.titulo(),
                tecnico ? JOptionPane.ERROR_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    // Error de una carga automática, no abre ventana y no repite el mismo aviso seguido
    public static void errorSilencioso(String accion, Throwable causa) {
        Traduccion t = traducir(causa);
        long ahora = System.currentTimeMillis();
        boolean repetido = t.mensaje().equals(ultimoError) && ahora - ultimoErrorHora < 4000;
        ultimoError = t.mensaje();
        ultimoErrorHora = ahora;
        if (repetido) {
            return;
        }
        registrar(accion, t, causa);
        if (notificador != null) {
            notificador.error(t.mensaje());
        }
    }

    public static boolean confirmar(Component padre, String titulo, String mensaje, String textoAceptar) {
        Object[] opciones = {textoAceptar, "Volver"};
        int r = JOptionPane.showOptionDialog(padre,
                "<html><body style='width:320px'>" + escapar(mensaje) + "</body></html>", titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]);
        return r == 0;
    }

    private static void registrar(String accion, Traduccion t, Throwable causa) {
        // Los errores de validación no necesitan la traza completa, los técnicos sí
        LogErrores.getInstancia().registrar(t.nivel(), accion, t.mensaje(),
                t.nivel() == Nivel.ERROR ? causa : null);
    }

    // Traducción de excepciones

    public static Traduccion traducir(Throwable causa) {
        // Busca en la cadena de causas la excepción que sabemos explicar
        for (Throwable t = causa; t != null; t = t.getCause()) {
            if (t instanceof CitaDuplicadaException) {
                return aviso("Horario no disponible", t.getMessage());
            }
            if (t instanceof IllegalArgumentException) {
                return aviso("Revise los datos", t.getMessage());
            }
            if (t instanceof SQLException sql) {
                return traducirSql(sql);
            }
        }
        return fallo("Error inesperado", "Ocurrió un error inesperado en la aplicación. Intente de nuevo; "
                + "si continúa, avise al equipo con el detalle del Registro de errores.");
    }

    private static Traduccion traducirSql(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        int codigo = e.getErrorCode();
        String estado = e.getSQLState() == null ? "" : e.getSQLState();

        if (e instanceof SQLIntegrityConstraintViolationException || codigo == 1062) {
            if (msg.contains("uq_pacientes_dui")) {
                return aviso("Dato duplicado", "Ya existe un paciente registrado con ese DUI.");
            }
            if (msg.contains("uq_doctores_jvpm")) {
                return aviso("Dato duplicado", "Ya existe un doctor registrado con ese número de JVPM.");
            }
            if (codigo == 1451 || msg.contains("foreign key")) {
                return aviso("Registro en uso",
                        "No se puede completar la acción porque el registro tiene datos relacionados.");
            }
            return aviso("Dato duplicado", "Ya existe un registro con esos datos.");
        }
        if (codigo == 1406 || msg.contains("Data too long")) {
            return aviso("Dato demasiado largo", "Alguno de los datos excede la longitud permitida.");
        }
        if (codigo == 1045 || estado.startsWith("28")) {
            return fallo("No se pudo conectar", "MySQL rechazó el usuario o la contraseña. Revise "
                    + "db.usuario y db.password en src/main/resources/db.properties.");
        }
        if (codigo == 1049) {
            return fallo("Base de datos no encontrada", "La base de datos citas_medicas no existe. "
                    + "Ejecute primero el script database/citas_medicas.sql en MySQL.");
        }
        if (codigo == 1146) {
            return fallo("Tablas no encontradas",
                    "Faltan las tablas de la base de datos. Ejecute el script database/citas_medicas.sql.");
        }
        if (estado.startsWith("08") || msg.contains("Communications link failure")
                || msg.contains("Connection refused") || msg.contains("No se encontró el archivo")) {
            return fallo("Sin conexión con la base de datos", "No se pudo conectar con MySQL. Verifique que "
                    + "el servicio esté en ejecución y que db.properties tenga la dirección correcta.");
        }
        return fallo("Error en la base de datos", "La base de datos devolvió un error al procesar la solicitud.");
    }

    // Problema que el usuario puede corregir
    private static Traduccion aviso(String titulo, String mensaje) {
        return new Traduccion(Nivel.ADVERTENCIA, titulo, mensaje);
    }

    // Problema técnico, el detalle completo queda en el registro
    private static Traduccion fallo(String titulo, String mensaje) {
        return new Traduccion(Nivel.ERROR, titulo, mensaje);
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

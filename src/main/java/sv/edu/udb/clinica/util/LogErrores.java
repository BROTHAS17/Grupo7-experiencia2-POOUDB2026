package sv.edu.udb.clinica.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Registro de errores de la aplicación (Unidad 4, clase 16).
 *
 * Cada evento se guarda en dos lugares:
 *  - el archivo logs/errores.log (queda como evidencia aunque se cierre el programa), y
 *  - una lista en memoria que la pantalla "Registro de errores" muestra al usuario.
 */
public final class LogErrores {

    public enum Nivel {
        INFO("Info"), ADVERTENCIA("Advertencia"), ERROR("Error");

        private final String etiqueta;

        Nivel(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }

    /** Un evento del registro. */
    public record Entrada(LocalDateTime fecha, Nivel nivel, String origen, String mensaje, String detalle) {
    }

    private static final LogErrores INSTANCIA = new LogErrores();
    private static final Path ARCHIVO = Paths.get("logs", "errores.log");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAXIMO_EN_MEMORIA = 500;

    private final List<Entrada> entradas = new ArrayList<>();
    private final List<Consumer<Entrada>> oyentes = new CopyOnWriteArrayList<>();

    private LogErrores() {
    }

    public static LogErrores getInstancia() {
        return INSTANCIA;
    }

    public Path getArchivo() {
        return ARCHIVO.toAbsolutePath();
    }

    public void info(String origen, String mensaje) {
        registrar(Nivel.INFO, origen, mensaje, null);
    }

    public void advertencia(String origen, String mensaje, Throwable causa) {
        registrar(Nivel.ADVERTENCIA, origen, mensaje, causa);
    }

    public void error(String origen, String mensaje, Throwable causa) {
        registrar(Nivel.ERROR, origen, mensaje, causa);
    }

    public void registrar(Nivel nivel, String origen, String mensaje, Throwable causa) {
        Entrada entrada = new Entrada(LocalDateTime.now(), nivel, origen, mensaje, describir(causa));
        synchronized (this) {
            entradas.add(entrada);
            if (entradas.size() > MAXIMO_EN_MEMORIA) {
                entradas.remove(0);
            }
            escribirEnArchivo(entrada);
        }
        // Las pantallas se actualizan siempre en el hilo de Swing.
        SwingUtilities.invokeLater(() -> oyentes.forEach(o -> o.accept(entrada)));
    }

    /** Copia de los eventos que hay en memoria, del más antiguo al más reciente. */
    public synchronized List<Entrada> getEntradas() {
        return new ArrayList<>(entradas);
    }

    public synchronized long contar(Nivel nivel) {
        return entradas.stream().filter(e -> e.nivel() == nivel).count();
    }

    /** Vacía solo la lista en pantalla; el archivo conserva el historial completo. */
    public synchronized void limpiarVista() {
        entradas.clear();
    }

    public void agregarOyente(Consumer<Entrada> oyente) {
        oyentes.add(oyente);
    }

    private static String describir(Throwable causa) {
        if (causa == null) {
            return "";
        }
        StringWriter texto = new StringWriter();
        causa.printStackTrace(new PrintWriter(texto));
        return texto.toString().trim();
    }

    private void escribirEnArchivo(Entrada e) {
        StringBuilder linea = new StringBuilder();
        linea.append(FORMATO.format(e.fecha())).append(" [").append(e.nivel().name()).append("] ")
                .append(e.origen()).append(" - ").append(e.mensaje()).append(System.lineSeparator());
        if (!e.detalle().isEmpty()) {
            for (String l : e.detalle().split("\\R")) {
                linea.append("    ").append(l).append(System.lineSeparator());
            }
        }
        try {
            Files.createDirectories(ARCHIVO.getParent());
            Files.writeString(ARCHIVO, linea.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            // Si el disco falla no se debe caer la aplicación: se avisa por consola y se sigue.
            System.err.println("No se pudo escribir en el archivo de log: " + ex.getMessage());
        }
    }
}

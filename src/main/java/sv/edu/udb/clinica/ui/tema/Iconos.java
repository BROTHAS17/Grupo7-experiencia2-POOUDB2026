package sv.edu.udb.clinica.ui.tema;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import java.awt.Color;
import java.awt.Image;
import java.net.URL;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;

// Acceso a los iconos svg de src/main/resources/icons
// Los iconos están dibujados en negro, FlatLaf los pinta en gris oscuro en el tema claro
// y en gris claro en el tema oscuro

public final class Iconos {

    private static final String CARPETA = "icons/";

    private Iconos() {
    }

    // Debe llamarse una sola vez, antes de crear la primera ventana
    public static void registrarFiltroDeColor() {
        FlatSVGIcon.ColorFilter.getInstance().add(Color.BLACK, Color.decode("#475569"), Color.decode("#CBD5E1"));
    }

    // Icono del color normal del tema (gris)
    public static Icon get(String nombre, int tamano) {
        return new FlatSVGIcon(CARPETA + nombre + ".svg", tamano, tamano);
    }

    public static Icon get(String nombre) {
        return get(nombre, 18);
    }

    // Icono con un color fijo, por ejemplo blanco sobre un botón de color
    public static Icon color(String nombre, int tamano, Color color) {
        FlatSVGIcon icono = new FlatSVGIcon(CARPETA + nombre + ".svg", tamano, tamano);
        icono.setColorFilter(new FlatSVGIcon.ColorFilter().add(Color.BLACK, color));
        return icono;
    }

    public static Icon blanco(String nombre) {
        return color(nombre, 18, Color.WHITE);
    }

    // Imágenes a color (logotipo e ilustración), no se les aplica el filtro del tema
    public static Icon imagen(String nombre, int ancho, int alto) {
        return new FlatSVGIcon(CARPETA + nombre + ".svg", ancho, alto);
    }

    // Icono de la ventana (barra de tareas y esquina de la ventana)
    public static List<Image> iconosVentana() {
        URL url = Iconos.class.getResource("/" + CARPETA + "app.png");
        return url == null ? List.of() : List.of(new ImageIcon(url).getImage());
    }
}

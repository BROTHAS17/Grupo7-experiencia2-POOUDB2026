package sv.edu.udb.clinica.ui.componentes;

import javax.swing.JButton;
import sv.edu.udb.clinica.ui.tema.Iconos;

// Fábrica de botones con los estilos de la aplicación

public final class Botones {

    private Botones() {
    }

    public static JButton primario(String texto, String icono) {
        return coloreado(texto, icono, "$Component.accentColor", "lighten($Component.accentColor,6%)",
                "darken($Component.accentColor,6%)");
    }

    public static JButton secundario(String texto, String icono) {
        return new JButton(texto, Iconos.get(icono));
    }

    // Botón rojo
    public static JButton peligro(String texto, String icono) {
        return coloreado(texto, icono, "#DC2626", "#B91C1C", "#991B1B");
    }

    // Botón verde
    public static JButton exito(String texto, String icono) {
        return coloreado(texto, icono, "#15803D", "#166534", "#14532D");
    }

    private static JButton coloreado(String texto, String icono, String fondo, String encima, String presionado) {
        JButton b = new JButton(texto, Iconos.blanco(icono));
        b.putClientProperty("FlatLaf.style", "background: " + fondo + "; foreground: #FFFFFF; "
                + "hoverBackground: " + encima + "; pressedBackground: " + presionado + "; borderWidth: 0");
        return b;
    }
}

package sv.edu.udb.clinica.ui.tema;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import java.awt.Color;
import java.util.prefs.Preferences;

// Tema visual de la aplicación (claro u oscuro) y colores que usan las pantallas
// Los colores se piden cada vez que se necesitan, así cambian al alternar el tema

public final class Tema {

    private static final String PAQUETE_ESTILOS = "sv.edu.udb.clinica.ui.tema";
    private static final String PREFERENCIA = "temaOscuro";

    private static boolean oscuro;

    private Tema() {
    }

    // Instala FlatLaf con el último tema usado, se llama una sola vez al iniciar
    public static void instalar() {
        oscuro = Preferences.userNodeForPackage(Tema.class).getBoolean(PREFERENCIA, false);
        FlatLaf.registerCustomDefaultsSource(PAQUETE_ESTILOS);
        aplicar();
    }

    // Cambia entre tema claro y oscuro
    public static void alternar() {
        FlatAnimatedLafChange.showSnapshot();
        oscuro = !oscuro;
        Preferences.userNodeForPackage(Tema.class).putBoolean(PREFERENCIA, oscuro);
        aplicar();
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private static void aplicar() {
        if (oscuro) {
            FlatDarkLaf.setup();
        } else {
            FlatLightLaf.setup();
        }
    }

    public static boolean esOscuro() {
        return oscuro;
    }

    private static Color c(String claro, String oscuroHex) {
        return Color.decode(oscuro ? oscuroHex : claro);
    }

    // Colores base

    public static Color primario() {
        return c("#0F766E", "#14B8A6");
    }

    // Fondo de las tarjetas (formularios, tablas y paneles de datos)
    public static Color superficie() {
        return c("#FFFFFF", "#1B2430");
    }

    public static Color borde() {
        return c("#E2E8F0", "#2A3441");
    }

    public static Color textoSecundario() {
        return c("#64748B", "#94A3B8");
    }

    public static Color textoFuerte() {
        return c("#0F172A", "#F1F5F9");
    }

    // Colores de estado

    public static Color exito() {
        return c("#15803D", "#4ADE80");
    }

    public static Color exitoFondo() {
        return c("#DCFCE7", "#14391F");
    }

    public static Color peligro() {
        return c("#DC2626", "#F87171");
    }

    public static Color peligroFondo() {
        return c("#FEE2E2", "#45191C");
    }

    public static Color advertencia() {
        return c("#B45309", "#FBBF24");
    }

    public static Color advertenciaFondo() {
        return c("#FEF3C7", "#42300F");
    }

    public static Color info() {
        return c("#1D4ED8", "#60A5FA");
    }

    public static Color infoFondo() {
        return c("#DBEAFE", "#172B4D");
    }

    // Barra lateral (siempre oscura)

    public static Color barraLateral() {
        return c("#0B3B3C", "#0C2A2D");
    }

    public static Color barraLateralTexto() {
        return Color.decode("#CFE8E6");
    }
}

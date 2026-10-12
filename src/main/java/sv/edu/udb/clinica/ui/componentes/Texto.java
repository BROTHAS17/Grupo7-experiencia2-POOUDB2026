package sv.edu.udb.clinica.ui.componentes;

import java.awt.Color;
import java.awt.Font;
import java.util.function.Supplier;
import javax.swing.JLabel;
import sv.edu.udb.clinica.ui.tema.Tema;

// Etiqueta cuyo color se calcula otra vez al cambiar de tema claro a oscuro
// (el color fijo de un JLabel normal se quedaría con el tema anterior)

public class Texto extends JLabel {

    private Supplier<Color> color;

    public Texto(String texto, Supplier<Color> color) {
        super(texto);
        setColor(color);
    }

    // Texto con un color que no depende del tema
    public Texto(String texto, Color fijo) {
        this(texto, () -> fijo);
    }

    public static Texto secundario(String texto) {
        return new Texto(texto, Tema::textoSecundario);
    }

    public static Texto fuerte(String texto) {
        return new Texto(texto, Tema::textoFuerte);
    }

    public static Texto peligro(String texto) {
        return new Texto(texto, Tema::peligro);
    }

    public Texto negrita() {
        setFont(getFont().deriveFont(Font.BOLD));
        return this;
    }

    // Cambia el tamaño de la letra respecto al tamaño base (por ejemplo +6 o -1)
    public Texto tamano(float delta) {
        setFont(getFont().deriveFont(getFont().getSize2D() + delta));
        return this;
    }

    public void setColor(Supplier<Color> nuevo) {
        color = nuevo;
        setForeground(nuevo.get());
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (color != null) {
            setForeground(color.get());
        }
    }
}

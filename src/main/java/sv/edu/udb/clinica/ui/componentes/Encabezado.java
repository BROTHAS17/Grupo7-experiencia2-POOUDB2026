package sv.edu.udb.clinica.ui.componentes;

import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// Título de una pantalla con una línea de descripción debajo

public final class Encabezado {

    private Encabezado() {
    }

    public static JPanel crear(String titulo, String descripcion) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 18, 0));
        panel.add(Texto.fuerte(titulo).negrita().tamano(9));
        panel.add(Texto.secundario(descripcion));
        return panel;
    }
}

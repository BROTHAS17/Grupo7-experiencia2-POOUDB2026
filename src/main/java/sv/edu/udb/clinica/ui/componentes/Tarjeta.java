package sv.edu.udb.clinica.ui.componentes;

import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.ui.tema.Tema;

// Panel con esquinas redondeadas y un borde fino, la "tarjeta" de la interfaz
// FlatLaf dibuja las esquinas con la propiedad arc

public class Tarjeta extends JPanel {

    private static final int ARCO = 16;

    private final int margen;

    public Tarjeta(LayoutManager layout, int margen) {
        super(layout);
        this.margen = margen;
        putClientProperty("FlatLaf.style", "arc: " + ARCO);
        poner();
    }

    public Tarjeta(int margen) {
        this(new BorderLayout(), margen);
    }

    // Se llama también al cambiar de tema para tomar los colores nuevos
    @Override
    public void updateUI() {
        super.updateUI();
        poner();
    }

    private void poner() {
        setBackground(Tema.superficie());
        setBorder(BorderFactory.createCompoundBorder(
                new FlatLineBorder(new Insets(0, 0, 0, 0), Tema.borde(), 1, ARCO),
                new EmptyBorder(margen, margen, margen, margen)));
    }
}

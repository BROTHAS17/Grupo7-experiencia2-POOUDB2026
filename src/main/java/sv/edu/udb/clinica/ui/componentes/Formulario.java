package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

// Formulario hecho de filas de campos, una debajo de otra

public class Formulario extends JPanel {

    private static final int ESPACIO_ENTRE_COLUMNAS = 14;
    private static final int ESPACIO_ENTRE_FILAS = 6;

    public Formulario() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    // Fila con columnas del mismo ancho
    public Formulario fila(JComponent... campos) {
        JPanel fila = crearFila(new GridLayout(1, campos.length, ESPACIO_ENTRE_COLUMNAS, 0));
        for (JComponent c : campos) {
            fila.add(c);
        }
        add(fila);
        return this;
    }

    // Fila donde cada columna ocupa una parte distinta del ancho
    public Formulario filaPesos(double[] pesos, JComponent... campos) {
        JPanel fila = crearFila(new GridBagLayout());
        for (int i = 0; i < campos.length; i++) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i;
            gbc.weightx = pesos[i];
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets = new Insets(0, 0, 0, i < campos.length - 1 ? ESPACIO_ENTRE_COLUMNAS : 0);
            fila.add(campos[i], gbc);
        }
        add(fila);
        return this;
    }

    // Pone el formulario dentro de una tarjeta, con el título arriba y los botones abajo
    public Tarjeta enTarjeta(Texto titulo, JButton guardar, JButton limpiar, int ancho) {
        PanelAnchoAjustado contenido = new PanelAnchoAjustado();
        contenido.add(this, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(contenido, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setViewportBorder(null);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botones.setOpaque(false);
        botones.add(guardar);
        botones.add(limpiar);

        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 14), 20);
        tarjeta.setPreferredSize(new Dimension(ancho, 100));
        tarjeta.add(titulo, BorderLayout.NORTH);
        tarjeta.add(scroll, BorderLayout.CENTER);
        tarjeta.add(botones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearFila(LayoutManager layout) {
        JPanel fila = new JPanel(layout) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(0, 0, ESPACIO_ENTRE_FILAS, 0));
        fila.setAlignmentX(LEFT_ALIGNMENT);
        return fila;
    }
}

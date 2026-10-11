package sv.edu.udb.clinica.ui.componentes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;

// Botón del menú lateral, resalta al pasar el mouse y cuando es la pantalla elegida

public class BotonNavegacion extends JToggleButton {

    public BotonNavegacion(String texto, String icono) {
        super(texto);
        cambiarIcono(icono);
        setHorizontalAlignment(SwingConstants.LEFT);
        setIconTextGap(14);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(new EmptyBorder(11, 18, 11, 14));
        setFont(getFont().deriveFont(Font.PLAIN, getFont().getSize2D() + 1f));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(LEFT_ALIGNMENT);
    }

    // Cambia el icono al alternar el tema
    public final void cambiarIcono(String icono) {
        setIcon(Iconos.color(icono, 20, Tema.barraLateralTexto()));
        setSelectedIcon(Iconos.color(icono, 20, Color.WHITE));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (isSelected()) {
            g.setColor(new Color(255, 255, 255, 38));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            // Barra de color al borde izquierdo
            g.setColor(Color.decode("#5EEAD4"));
            g.fillRoundRect(0, 9, 4, getHeight() - 18, 4, 4);
        } else if (getModel().isRollover()) {
            g.setColor(new Color(255, 255, 255, 18));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        }
        setForeground(isSelected() ? Color.WHITE : Tema.barraLateralTexto());
        super.paintComponent(g);
    }
}

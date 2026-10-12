package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

// Panel para poner dentro de un JScrollPane, se ajusta siempre al ancho visible (sin barra
// horizontal) y solo se desplaza hacia abajo si el contenido no cabe en alto

public class PanelAnchoAjustado extends JPanel implements Scrollable {

    public PanelAnchoAjustado() {
        super(new BorderLayout());
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
        return visible.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}

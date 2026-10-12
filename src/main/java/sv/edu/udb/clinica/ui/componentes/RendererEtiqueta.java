package sv.edu.udb.clinica.ui.componentes;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.util.LogErrores.Nivel;

// Dibuja el estado de una cita (o el nivel de un evento del log) como una etiqueta redondeada de color

public class RendererEtiqueta implements TableCellRenderer {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JLabel etiqueta = new JLabel();

    public RendererEtiqueta() {
        etiqueta.setOpaque(true);
        etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
        etiqueta.setBorder(new EmptyBorder(2, 10, 2, 10));
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, etiqueta.getFont().getSize2D() - 1f));
        // Un arco muy grande deja los extremos completamente redondos
        etiqueta.putClientProperty("FlatLaf.style", "arc: 999");

        // Sin relleno, el panel deja la etiqueta centrada en alto y pegada a la izquierda
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.anchor = GridBagConstraints.WEST;
        posicion.weightx = 1;
        posicion.insets = new Insets(0, 4, 0, 0);
        panel.add(etiqueta, posicion);
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
            boolean foco, int fila, int columna) {
        if (valor instanceof CitaMedica.Estado estado) {
            etiqueta.setText(Etiquetas.estado(estado));
            if (estado == CitaMedica.Estado.ATENDIDA) {
                colores(Tema.exito(), Tema.exitoFondo());
            } else if (estado == CitaMedica.Estado.CANCELADA) {
                colores(Tema.peligro(), Tema.peligroFondo());
            } else {
                colores(Tema.info(), Tema.infoFondo());
            }
        } else if (valor instanceof Nivel nivel) {
            etiqueta.setText(nivel.getEtiqueta());
            if (nivel == Nivel.ERROR) {
                colores(Tema.peligro(), Tema.peligroFondo());
            } else if (nivel == Nivel.ADVERTENCIA) {
                colores(Tema.advertencia(), Tema.advertenciaFondo());
            } else {
                colores(Tema.info(), Tema.infoFondo());
            }
        }

        Color alterno = UIManager.getColor("Table.alternateRowColor");
        if (seleccionada) {
            panel.setBackground(tabla.getSelectionBackground());
        } else if (fila % 2 == 1 && alterno != null) {
            panel.setBackground(alterno);
        } else {
            panel.setBackground(tabla.getBackground());
        }
        return panel;
    }

    private void colores(Color letra, Color fondo) {
        etiqueta.setForeground(letra);
        etiqueta.setBackground(fondo);
    }
}

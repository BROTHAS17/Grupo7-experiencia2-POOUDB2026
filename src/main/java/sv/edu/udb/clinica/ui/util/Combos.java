package sv.edu.udb.clinica.ui.util;

import java.awt.Component;
import java.util.Collection;
import java.util.function.Function;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import sv.edu.udb.clinica.ui.tema.Tema;

// Listas desplegables con un primer elemento vacío ("Seleccione...") en gris
// Elegir de una lista cerrada evita que el usuario escriba valores inválidos

public final class Combos {

    private static final String CON_MARCADOR = "combos.conMarcador";

    private Combos() {
    }

    public static <T> JComboBox<T> crear(String marcador, Function<T, String> texto) {
        return crear(marcador, texto, texto);
    }

    // textoElegido es cómo se muestra el elemento ya elegido (puede ser más corto que en la lista)
    public static <T> JComboBox<T> crear(String marcador, Function<T, String> texto,
            Function<T, String> textoElegido) {
        JComboBox<T> combo = new JComboBox<>(new DefaultComboBoxModel<>());
        if (marcador != null) {
            combo.putClientProperty(CON_MARCADOR, Boolean.TRUE);
            ((DefaultComboBoxModel<T>) combo.getModel()).addElement(null);
        }
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {
                String s = valor == null ? (marcador == null ? "" : marcador)
                        : (indice < 0 ? textoElegido : texto).apply((T) valor);
                super.getListCellRendererComponent(lista, s, indice, seleccionado, foco);
                if (valor == null && !seleccionado) {
                    setForeground(Tema.textoSecundario());
                }
                return this;
            }
        });
        return combo;
    }

    // Cambia los elementos conservando la selección si el elemento sigue existiendo (según equals)
    public static <T> void reemplazar(JComboBox<T> combo, Collection<? extends T> elementos) {
        DefaultComboBoxModel<T> modelo = (DefaultComboBoxModel<T>) combo.getModel();
        boolean conMarcador = Boolean.TRUE.equals(combo.getClientProperty(CON_MARCADOR));
        Object previo = combo.getSelectedItem();
        modelo.removeAllElements();
        if (conMarcador) {
            modelo.addElement(null);
        }
        for (T e : elementos) {
            modelo.addElement(e);
        }
        if (previo != null && modelo.getIndexOf(previo) >= 0) {
            combo.setSelectedItem(previo);
        } else if (conMarcador || modelo.getSize() == 0) {
            combo.setSelectedItem(null);
        } else {
            combo.setSelectedIndex(0);
        }
    }

    // Selecciona el valor, si no está en la lista lo agrega (por ejemplo un dato antiguo)
    public static <T> void seleccionarOAgregar(JComboBox<T> combo, T valor) {
        if (valor == null) {
            combo.setSelectedItem(null);
            return;
        }
        DefaultComboBoxModel<T> modelo = (DefaultComboBoxModel<T>) combo.getModel();
        if (modelo.getIndexOf(valor) < 0) {
            modelo.addElement(valor);
        }
        combo.setSelectedItem(valor);
    }
}

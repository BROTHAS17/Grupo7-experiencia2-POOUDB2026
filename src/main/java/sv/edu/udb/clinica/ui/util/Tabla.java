package sv.edu.udb.clinica.ui.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.ui.componentes.RendererEtiqueta;
import sv.edu.udb.clinica.ui.componentes.Tarjeta;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.util.LogErrores;

// Configuración común de las tablas de la aplicación

public final class Tabla {

    private Tabla() {
    }

    public static JTable crear(TableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowSorter(new TableRowSorter<TableModel>(modelo));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getTableHeader().setFont(tabla.getTableHeader().getFont().deriveFont(Font.BOLD));

        // Fechas y horas se muestran con formato propio, aunque el modelo guarde objetos
        // así la tabla también las ordena por su valor real
        DefaultTableCellRenderer formato = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object valor) {
                setText(textoDe(valor));
            }
        };
        tabla.setDefaultRenderer(LocalDate.class, formato);
        tabla.setDefaultRenderer(LocalTime.class, formato);

        RendererEtiqueta etiqueta = new RendererEtiqueta();
        tabla.setDefaultRenderer(CitaMedica.Estado.class, etiqueta);
        tabla.setDefaultRenderer(LogErrores.Nivel.class, etiqueta);
        return tabla;
    }

    // Tabla dentro de un panel con desplazamiento, sin el borde que Swing agrega por defecto
    public static JScrollPane envolver(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    public static void anchos(JTable tabla, int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    // Evita que el ajuste automático de columnas deje una columna demasiado angosta
    public static void anchoMinimo(JTable tabla, int columna, int ancho) {
        tabla.getColumnModel().getColumn(columna).setMinWidth(ancho);
    }

    // Devuelve el objeto de la fila seleccionada, o null si no hay selección
    public static <T> T seleccionado(JTable tabla, ModeloTabla<T> modelo) {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.get(tabla.convertRowIndexToModel(fila));
    }

    // Selecciona una fila del modelo si está visible con los filtros actuales y la muestra
    public static void seleccionarFilaDelModelo(JTable tabla, int filaModelo) {
        if (filaModelo < 0) {
            return;
        }
        int vista = tabla.convertRowIndexToView(filaModelo);
        if (vista >= 0) {
            tabla.setRowSelectionInterval(vista, vista);
            tabla.scrollRectToVisible(tabla.getCellRect(vista, 0, true));
        }
    }

    // Campo de búsqueda con una lupa y un botón para borrar el texto
    public static JTextField campoBusqueda(String ayuda) {
        JTextField campo = new JTextField();
        campo.putClientProperty("JTextField.placeholderText", ayuda);
        campo.putClientProperty("JTextField.showClearButton", true);
        campo.putClientProperty("JTextField.leadingIcon", Iconos.get("buscar"));
        return campo;
    }

    // Tarjeta con la barra de búsqueda arriba (y algo a su derecha, como el total) y la tabla debajo
    public static Tarjeta tarjeta(JTextField busqueda, JComponent derecha, JTable tabla) {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(0, 0, 14, 0));
        barra.add(busqueda, BorderLayout.CENTER);
        barra.add(derecha, BorderLayout.EAST);

        Tarjeta tarjeta = new Tarjeta(20);
        tarjeta.add(barra, BorderLayout.NORTH);
        tarjeta.add(envolver(tabla), BorderLayout.CENTER);
        return tarjeta;
    }

    // Muestra solo las filas que contienen el texto buscado en alguna columna
    public static void filtrar(JTable tabla, String buscado) {
        filtrar(tabla, buscado, -1, null);
    }

    // Igual que el anterior, pero si valor no es null la columna indicada debe tener exactamente ese valor
    public static void filtrar(JTable tabla, String buscado, int columna, Object valor) {
        String buscar = normalizar(buscado);
        RowFilter<TableModel, Integer> filtro = new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> fila) {
                if (valor != null && !valor.equals(fila.getValue(columna))) {
                    return false;
                }
                for (int i = 0; i < fila.getValueCount(); i++) {
                    if (normalizar(textoDe(fila.getValue(i))).contains(buscar)) {
                        return true;
                    }
                }
                return buscar.isEmpty();
            }
        };
        ((TableRowSorter<?>) tabla.getRowSorter()).setRowFilter(filtro);
    }

    // Texto que ve el usuario en una celda
    private static String textoDe(Object valor) {
        if (valor instanceof LocalDate fecha) {
            return Etiquetas.fecha(fecha);
        }
        if (valor instanceof LocalTime hora) {
            return Etiquetas.hora(hora);
        }
        if (valor instanceof CitaMedica.Estado estado) {
            return Etiquetas.estado(estado);
        }
        return valor == null ? "" : valor.toString();
    }

    // Pasa a minúsculas y quita las tildes para buscar sin distinguirlas
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }
}

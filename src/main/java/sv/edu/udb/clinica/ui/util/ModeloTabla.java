package sv.edu.udb.clinica.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

// Modelo de tabla genérico, cada columna se define con un título, un tipo y una función
// que obtiene el valor desde el objeto (por ejemplo el método getTelefono de Paciente)

public class ModeloTabla<T> extends AbstractTableModel {

    private record Columna<T>(String titulo, Class<?> tipo, Function<T, Object> valor) {
    }

    private final List<Columna<T>> columnas = new ArrayList<>();
    private List<T> datos = new ArrayList<>();

    public ModeloTabla<T> columna(String titulo, Class<?> tipo, Function<T, Object> valor) {
        columnas.add(new Columna<>(titulo, tipo, valor));
        return this;
    }

    public void setDatos(List<T> nuevos) {
        datos = new ArrayList<>(nuevos);
        fireTableDataChanged();
    }

    public T get(int fila) {
        return datos.get(fila);
    }

    @Override
    public int getRowCount() {
        return datos.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna).titulo();
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        return columnas.get(columna).tipo();
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        return columnas.get(columna).valor().apply(datos.get(fila));
    }
}

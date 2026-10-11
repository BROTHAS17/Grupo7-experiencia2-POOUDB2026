package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.ui.componentes.Botones;
import sv.edu.udb.clinica.ui.componentes.Encabezado;
import sv.edu.udb.clinica.ui.componentes.Tarjeta;
import sv.edu.udb.clinica.ui.componentes.Texto;
import sv.edu.udb.clinica.ui.util.Combos;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.ui.util.ModeloTabla;
import sv.edu.udb.clinica.ui.util.Tabla;
import sv.edu.udb.clinica.util.LogErrores;
import sv.edu.udb.clinica.util.LogErrores.Entrada;
import sv.edu.udb.clinica.util.LogErrores.Nivel;

// Registro de errores visible para el usuario, con la lista de eventos de la sesión
// su nivel, el origen y el detalle técnico, y acceso al archivo logs/errores.log

public class PanelLogs extends JPanel implements Refrescable {

    private static final String SIN_SELECCION = "Seleccione un evento para ver su detalle técnico.";
    private static final int COL_NIVEL = 1;

    private final LogErrores log = LogErrores.getInstancia();

    private final ModeloTabla<Entrada> modelo = new ModeloTabla<Entrada>()
            .columna("Fecha y hora", String.class, e -> Etiquetas.FECHA_HORA.format(e.fecha()))
            .columna("Nivel", Nivel.class, Entrada::nivel)
            .columna("Origen", String.class, Entrada::origen)
            .columna("Mensaje", String.class, Entrada::mensaje);
    private final JTable tabla = Tabla.crear(modelo);
    private final JComboBox<Nivel> cmbNivel = Combos.crear("Todos los niveles", Nivel::getEtiqueta);
    private final JTextArea detalle = new JTextArea(SIN_SELECCION);
    private final Texto lblTotal = Texto.secundario(" ");

    public PanelLogs() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 16, 28));

        add(Encabezado.crear("Registro de errores",
                "Avisos y errores de esta sesión. Todo queda guardado también en el archivo de log."),
                BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);

        // Cada evento nuevo aparece en la tabla
        log.agregarOyente(e -> refrescar());
    }

    private Tarjeta crearContenido() {
        Combos.reemplazar(cmbNivel, List.of(Nivel.values()));
        cmbNivel.setPreferredSize(new Dimension(190, cmbNivel.getPreferredSize().height));
        cmbNivel.addActionListener(e -> filtrar());

        JButton actualizar = Botones.secundario("Actualizar", "actualizar");
        actualizar.addActionListener(e -> refrescar());
        JButton limpiar = Botones.secundario("Limpiar vista", "papelera");
        limpiar.setToolTipText("Vacía la lista en pantalla; el archivo conserva el historial.");
        limpiar.addActionListener(e -> {
            log.limpiarVista();
            refrescar();
        });
        JButton abrir = Botones.secundario("Abrir archivo de log", "carpeta");
        abrir.addActionListener(e -> abrirArchivo());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.setBorder(new EmptyBorder(0, 0, 14, 0));
        barra.add(fila(FlowLayout.LEFT, cmbNivel, lblTotal), BorderLayout.WEST);
        barra.add(fila(FlowLayout.RIGHT, actualizar, limpiar, abrir), BorderLayout.EAST);

        Tabla.anchos(tabla, 150, 110, 190, 480);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarDetalle(Tabla.seleccionado(tabla, modelo));
            }
        });

        detalle.setEditable(false);
        detalle.setLineWrap(true);
        detalle.setWrapStyleWord(true);
        detalle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detalle.setBorder(new EmptyBorder(10, 12, 10, 12));
        JScrollPane scrollDetalle = new JScrollPane(detalle);
        scrollDetalle.setMinimumSize(new Dimension(100, 90));

        JSplitPane division = new JSplitPane(JSplitPane.VERTICAL_SPLIT, Tabla.envolver(tabla), scrollDetalle);
        division.setResizeWeight(0.62);
        division.setBorder(BorderFactory.createEmptyBorder());
        division.setOpaque(false);

        Texto pie = Texto.secundario("Archivo: " + log.getArchivo());
        pie.setBorder(new EmptyBorder(12, 0, 0, 0));

        Tarjeta tarjeta = new Tarjeta(20);
        tarjeta.add(barra, BorderLayout.NORTH);
        tarjeta.add(division, BorderLayout.CENTER);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    // Panel transparente con los componentes en una fila
    private JPanel fila(int alineacion, JComponent... componentes) {
        JPanel panel = new JPanel(new FlowLayout(alineacion, 8, 0));
        panel.setOpaque(false);
        for (JComponent c : componentes) {
            panel.add(c);
        }
        return panel;
    }

    @Override
    public void refrescar() {
        List<Entrada> lista = new ArrayList<>(log.getEntradas());
        // Lo más reciente primero
        Collections.reverse(lista);
        modelo.setDatos(lista);
        actualizarTotal();
    }

    private void filtrar() {
        Tabla.filtrar(tabla, "", COL_NIVEL, cmbNivel.getSelectedItem());
        actualizarTotal();
    }

    private void actualizarTotal() {
        lblTotal.setText(modelo.getRowCount() == 0 ? "Sin eventos en esta sesión"
                : Etiquetas.plural(tabla.getRowCount(), "evento", "eventos"));
    }

    private void mostrarDetalle(Entrada e) {
        if (e == null) {
            detalle.setText(SIN_SELECCION);
            return;
        }
        String texto = Etiquetas.FECHA_HORA.format(e.fecha()) + "  ·  " + e.nivel().getEtiqueta()
                + "  ·  " + e.origen() + "\n\n" + e.mensaje();
        if (!e.detalle().isEmpty()) {
            texto += "\n\n" + e.detalle();
        }
        detalle.setText(texto);
        detalle.setCaretPosition(0);
    }

    private void abrirArchivo() {
        File archivo = log.getArchivo().toFile();
        try {
            if (!archivo.exists()) {
                Mensajes.info("Todavía no se ha registrado ningún evento en el archivo.");
            } else if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Mensajes.info("Este equipo no permite abrir el archivo desde la aplicación: " + archivo);
            } else {
                Desktop.getDesktop().open(archivo);
            }
        } catch (IOException | RuntimeException ex) {
            Mensajes.error(this, "Abrir archivo de log", ex);
        }
    }
}

package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.Paciente;
import sv.edu.udb.clinica.ui.util.Escucha;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.ui.util.ModeloTabla;
import sv.edu.udb.clinica.ui.util.Tabla;

// Ventana con una lista de pacientes y un buscador para elegir uno

public final class DialogoSeleccionPaciente extends JDialog {

    private final ModeloTabla<Paciente> modelo = new ModeloTabla<Paciente>()
            .columna("Código", Integer.class, Paciente::getIdPaciente)
            .columna("Nombre completo", String.class, Paciente::getNombreCompleto)
            .columna("DUI", String.class, p -> Etiquetas.vacio(p.getDui()))
            .columna("Teléfono", String.class, p -> Etiquetas.vacio(p.getTelefono()));
    private final JTable tabla = Tabla.crear(modelo);
    private final JButton seleccionar = Botones.primario("Seleccionar", "atendida");
    private Paciente elegido;

    private DialogoSeleccionPaciente(Window padre, List<Paciente> pacientes) {
        super(padre, "Seleccionar paciente", ModalityType.APPLICATION_MODAL);
        modelo.setDatos(pacientes);

        JTextField buscar = Tabla.campoBusqueda("Buscar por nombre, DUI o teléfono...");
        Escucha.alEscribir(buscar, () -> Tabla.filtrar(tabla, buscar.getText()));

        Tabla.anchos(tabla, 70, 280, 110, 110);
        tabla.getSelectionModel().addListSelectionListener(e -> seleccionar.setEnabled(tabla.getSelectedRow() >= 0));
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) {
                    confirmar();
                }
            }
        });

        seleccionar.setEnabled(false);
        seleccionar.addActionListener(e -> confirmar());
        JButton cancelar = Botones.secundario("Cancelar", "cancelar");
        cancelar.addActionListener(e -> dispose());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(seleccionar);

        JPanel superior = new JPanel(new BorderLayout(0, 8));
        superior.setOpaque(false);
        superior.add(buscar, BorderLayout.NORTH);
        if (pacientes.isEmpty()) {
            superior.add(Texto.secundario("No hay pacientes registrados. Regístrelo primero en la sección Pacientes."),
                    BorderLayout.CENTER);
        }

        JPanel contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBorder(new EmptyBorder(16, 16, 16, 16));
        contenido.add(superior, BorderLayout.NORTH);
        contenido.add(Tabla.envolver(tabla), BorderLayout.CENTER);
        contenido.add(botones, BorderLayout.SOUTH);
        setContentPane(contenido);

        // La tecla Escape cierra la ventana
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(640, 460);
        setLocationRelativeTo(padre);
    }

    private void confirmar() {
        elegido = Tabla.seleccionado(tabla, modelo);
        dispose();
    }

    // Abre la ventana y espera, devuelve el paciente elegido o null si se canceló
    public static Paciente mostrar(Window padre, List<Paciente> pacientes) {
        DialogoSeleccionPaciente d = new DialogoSeleccionPaciente(padre, pacientes);
        d.setVisible(true);
        return d.elegido;
    }
}

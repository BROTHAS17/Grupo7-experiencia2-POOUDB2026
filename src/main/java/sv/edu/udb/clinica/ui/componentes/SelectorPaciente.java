package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import sv.edu.udb.clinica.modelo.Paciente;
import sv.edu.udb.clinica.ui.tema.Iconos;

// Campo para elegir el paciente de una cita, muestra el nombre y abre una ventana de búsqueda
// Así nunca se escribe un código de paciente a mano (fuente de errores en el registro manual)

public class SelectorPaciente extends JPanel implements CampoValidable {

    private final JTextField campo = new JTextField();
    private final JButton boton = new JButton("Buscar", Iconos.get("buscar"));
    private final List<Runnable> oyentes = new ArrayList<>();
    private List<Paciente> pacientes = new ArrayList<>();
    private Paciente seleccionado;

    public SelectorPaciente() {
        super(new BorderLayout(6, 0));
        setOpaque(false);
        campo.setEditable(false);
        campo.putClientProperty("JTextField.placeholderText", "Ningún paciente seleccionado");
        campo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        campo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrir();
            }
        });
        boton.setFocusable(false);
        boton.addActionListener(e -> abrir());
        add(campo, BorderLayout.CENTER);
        add(boton, BorderLayout.EAST);
    }

    public void setPacientes(List<Paciente> pacientes) {
        this.pacientes = pacientes;
        // Si el paciente elegido sigue existiendo se actualiza su objeto, si no se limpia
        if (seleccionado != null) {
            seleccionarPorId(seleccionado.getIdPaciente());
        }
    }

    public Paciente getPaciente() {
        return seleccionado;
    }

    public void setPaciente(Paciente paciente) {
        seleccionado = paciente;
        if (paciente == null) {
            campo.setText("");
        } else {
            String dui = paciente.getDui() == null ? "" : "   ·   DUI " + paciente.getDui();
            campo.setText(paciente.getNombreCompleto() + dui);
        }
        for (Runnable oyente : oyentes) {
            oyente.run();
        }
    }

    // Elige el paciente con ese código de la lista cargada (o deja vacío si no existe)
    public void seleccionarPorId(int idPaciente) {
        Paciente encontrado = null;
        for (Paciente p : pacientes) {
            if (p.getIdPaciente() == idPaciente) {
                encontrado = p;
            }
        }
        setPaciente(encontrado);
    }

    private void abrir() {
        if (isEnabled()) {
            Paciente elegido = DialogoSeleccionPaciente.mostrar(SwingUtilities.getWindowAncestor(this), pacientes);
            if (elegido != null) {
                setPaciente(elegido);
            }
        }
    }

    @Override
    public void marcarError(boolean error) {
        campo.putClientProperty("JComponent.outline", error ? "error" : null);
    }

    @Override
    public void alCambiar(Runnable accion) {
        oyentes.add(accion);
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        campo.setEnabled(habilitado);
        boton.setEnabled(habilitado);
    }
}

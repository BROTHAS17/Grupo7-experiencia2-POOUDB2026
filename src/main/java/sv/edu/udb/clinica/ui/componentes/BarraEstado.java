package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.util.LogErrores;
import sv.edu.udb.clinica.util.LogErrores.Nivel;

// Barra inferior de la ventana, muestra avisos breves y el acceso al registro de errores

public class BarraEstado extends JPanel implements Mensajes.Notificador {

    private static final int SEGUNDOS_VISIBLE = 8;

    private final JLabel mensaje = new JLabel(" ");
    private final JButton botonRegistro = new JButton();
    private final Timer temporizador = new Timer(SEGUNDOS_VISIBLE * 1000, e -> limpiar());

    public BarraEstado(Runnable abrirRegistro) {
        super(new BorderLayout(12, 0));
        setBorder(new EmptyBorder(6, 20, 6, 14));
        setOpaque(false);
        temporizador.setRepeats(false);

        botonRegistro.putClientProperty("JButton.buttonType", "toolBarButton");
        botonRegistro.setFocusable(false);
        botonRegistro.setToolTipText("Abrir el Registro de errores");
        botonRegistro.addActionListener(e -> abrirRegistro.run());

        add(mensaje, BorderLayout.CENTER);
        add(botonRegistro, BorderLayout.EAST);

        actualizarContador();
        LogErrores.getInstancia().agregarOyente(e -> actualizarContador());
    }

    private void actualizarContador() {
        LogErrores log = LogErrores.getInstancia();
        long problemas = log.contar(Nivel.ERROR) + log.contar(Nivel.ADVERTENCIA);
        botonRegistro.setIcon(Iconos.get("registro", 16));
        botonRegistro.setText(problemas == 0 ? "Registro de errores" : "Registro de errores (" + problemas + ")");
    }

    @Override
    public void exito(String texto) {
        mostrar("atendida", Tema.exito(), texto);
    }

    @Override
    public void error(String texto) {
        mostrar("advertencia", Tema.peligro(), texto);
    }

    @Override
    public void info(String texto) {
        mostrar("info", Tema.info(), texto);
    }

    private void mostrar(String icono, Color color, String texto) {
        mensaje.setIcon(Iconos.color(icono, 16, color));
        mensaje.setText(texto);
        mensaje.setForeground(color);
        temporizador.restart();
    }

    private void limpiar() {
        mensaje.setIcon(null);
        mensaje.setText(" ");
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(Tema.superficie());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Tema.borde());
        g.drawLine(0, 0, getWidth(), 0);
        super.paintComponent(g);
    }
}

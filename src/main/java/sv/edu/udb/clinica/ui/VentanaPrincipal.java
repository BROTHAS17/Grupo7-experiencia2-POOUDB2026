package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.conexion.ConexionBD;
import sv.edu.udb.clinica.ui.componentes.BarraEstado;
import sv.edu.udb.clinica.ui.componentes.BotonNavegacion;
import sv.edu.udb.clinica.ui.componentes.Texto;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.ui.util.Tarea;

// Ventana principal con menú lateral, pantallas intercambiables y barra de estado

public class VentanaPrincipal extends JFrame {

    private static final String INICIO = "inicio";
    private static final String PACIENTES = "pacientes";
    private static final String DOCTORES = "doctores";
    private static final String CITAS = "citas";
    private static final String REGISTRO = "registro";

    private final Servicios servicios = new Servicios();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);
    private final Map<String, JPanel> pantallas = new HashMap<>();
    private final Map<String, BotonNavegacion> botones = new HashMap<>();
    private final ButtonGroup grupo = new ButtonGroup();
    private final JLabel textoConexion = new JLabel();
    private final BarraEstado barraEstado = new BarraEstado(() -> mostrar(REGISTRO));
    private BotonNavegacion botonTema;
    private String pantallaActual = INICIO;

    // El menú lateral cambia de color con el tema
    private final JPanel barraLateral = new JPanel() {
        @Override
        public void updateUI() {
            super.updateUI();
            setBackground(Tema.barraLateral());
        }
    };

    public VentanaPrincipal() {
        super("Clínica Vida UDB · Gestión de citas médicas");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImages(Iconos.iconosVentana());
        setMinimumSize(new Dimension(1180, 700));
        // Tamaño cómodo, pero sin pasar del tamaño de la pantalla
        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(Math.min(1440, pantalla.width), Math.min(860, pantalla.height));
        setLocationRelativeTo(null);

        Mensajes.setNotificador(barraEstado);

        registrarPantalla(INICIO, new PanelInicio(servicios, this::mostrar));
        registrarPantalla(PACIENTES, new PanelPacientes(servicios.pacientes()));
        registrarPantalla(DOCTORES, new PanelDoctores(servicios.doctores()));
        registrarPantalla(CITAS, new PanelCitas(servicios));
        registrarPantalla(REGISTRO, new PanelLogs());

        contenedor.setOpaque(false);
        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setOpaque(false);
        derecha.add(contenedor, BorderLayout.CENTER);
        derecha.add(barraEstado, BorderLayout.SOUTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearBarraLateral(), BorderLayout.WEST);
        raiz.add(derecha, BorderLayout.CENTER);
        setContentPane(raiz);

        mostrar(INICIO);
        verificarConexion(true);
    }

    // Navegación

    private void registrarPantalla(String id, JPanel pantalla) {
        pantallas.put(id, pantalla);
        contenedor.add(pantalla, id);
    }

    // Muestra la pantalla y vuelve a leer sus datos de la base de datos
    private void mostrar(String id) {
        pantallaActual = id;
        tarjetas.show(contenedor, id);
        BotonNavegacion boton = botones.get(id);
        if (boton != null) {
            boton.setSelected(true);
        }
        if (pantallas.get(id) instanceof Refrescable pantalla) {
            pantalla.refrescar();
        }
    }

    private JPanel crearBarraLateral() {
        barraLateral.setLayout(new BoxLayout(barraLateral, BoxLayout.Y_AXIS));
        barraLateral.setBorder(new EmptyBorder(24, 16, 20, 16));
        barraLateral.setPreferredSize(new Dimension(250, 100));

        // Marca
        JPanel nombres = new JPanel(new GridLayout(2, 1));
        nombres.setOpaque(false);
        nombres.add(new Texto("Vida UDB", Color.WHITE).negrita().tamano(4));
        nombres.add(new Texto("Clínica universitaria", Tema.barraLateralTexto()).tamano(-1));
        JPanel marca = new JPanel(new BorderLayout(12, 0));
        marca.setOpaque(false);
        marca.setAlignmentX(LEFT_ALIGNMENT);
        marca.setBorder(new EmptyBorder(0, 6, 0, 0));
        marca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        marca.add(new JLabel(Iconos.imagen("logo", 42, 42)), BorderLayout.WEST);
        marca.add(nombres, BorderLayout.CENTER);
        barraLateral.add(marca);

        barraLateral.add(Box.createVerticalStrut(30));
        Texto seccion = new Texto("MENÚ", new Color(0x7FB8B3));
        seccion.setBorder(new EmptyBorder(0, 8, 8, 0));
        seccion.setAlignmentX(LEFT_ALIGNMENT);
        barraLateral.add(seccion);

        agregarBoton(INICIO, "Inicio", "inicio");
        agregarBoton(PACIENTES, "Pacientes", "pacientes");
        agregarBoton(DOCTORES, "Doctores", "doctores");
        agregarBoton(CITAS, "Citas médicas", "citas");
        agregarBoton(REGISTRO, "Registro de errores", "registro");

        barraLateral.add(Box.createVerticalGlue());
        barraLateral.add(crearIndicadorConexion());
        barraLateral.add(Box.createVerticalStrut(10));

        botonTema = new BotonNavegacion("", "luna");
        botonTema.addActionListener(e -> {
            botonTema.setSelected(false);
            Tema.alternar();
            actualizarBotonTema();
        });
        actualizarBotonTema();
        barraLateral.add(botonTema);
        return barraLateral;
    }

    private void agregarBoton(String id, String texto, String icono) {
        BotonNavegacion b = new BotonNavegacion(texto, icono);
        b.addActionListener(e -> mostrar(id));
        grupo.add(b);
        botones.put(id, b);
        barraLateral.add(b);
        barraLateral.add(Box.createVerticalStrut(4));
    }

    private void actualizarBotonTema() {
        botonTema.setText(Tema.esOscuro() ? "Cambiar a tema claro" : "Cambiar a tema oscuro");
        botonTema.cambiarIcono(Tema.esOscuro() ? "sol" : "luna");
    }

    // Conexión a la base de datos

    // Cuadro del menú que muestra si MySQL responde, al hacer clic vuelve a probar
    private JPanel crearIndicadorConexion() {
        textoConexion.setForeground(Tema.barraLateralTexto());
        textoConexion.setIconTextGap(12);
        mostrarConexion(null);

        JPanel indicador = new JPanel(new BorderLayout());
        indicador.putClientProperty("FlatLaf.style", "arc: 12; background: #FFFFFF14");
        indicador.setBorder(new EmptyBorder(10, 14, 10, 10));
        indicador.setAlignmentX(LEFT_ALIGNMENT);
        indicador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        indicador.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        indicador.setToolTipText("Clic para probar la conexión otra vez");
        indicador.add(textoConexion, BorderLayout.CENTER);
        indicador.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                verificarConexion(false);
            }
        });
        return indicador;
    }

    // Recibe null mientras prueba, true si hay conexión y false si no la hay
    private void mostrarConexion(Boolean conectado) {
        if (conectado == null) {
            textoConexion.setText("Probando conexión...");
            textoConexion.setIcon(Iconos.color("punto", 10, Color.decode("#FBBF24")));
        } else if (conectado) {
            textoConexion.setText("MySQL conectado");
            textoConexion.setIcon(Iconos.color("punto", 10, Color.decode("#4ADE80")));
        } else {
            textoConexion.setText("Sin conexión a MySQL");
            textoConexion.setIcon(Iconos.color("punto", 10, Color.decode("#F87171")));
        }
    }

    private void verificarConexion(boolean alIniciar) {
        mostrarConexion(null);
        Tarea.ejecutar(() -> {
            try (Connection c = ConexionBD.obtenerConexion()) {
                return c.isValid(3);
            }
        }, ok -> {
            mostrarConexion(ok);
            if (!alIniciar && ok) {
                Mensajes.exito("Conexión con la base de datos establecida.");
                mostrar(pantallaActual);
            }
        }, error -> {
            mostrarConexion(false);
            if (alIniciar) {
                Mensajes.error(this, "Conexión a MySQL", error);
            } else {
                Mensajes.errorSilencioso("Conexión a MySQL", error);
            }
        });
    }
}

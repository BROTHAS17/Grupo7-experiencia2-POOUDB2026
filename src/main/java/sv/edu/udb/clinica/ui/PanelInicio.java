package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.modelo.CitaMedica.Estado;
import sv.edu.udb.clinica.modelo.Doctor;
import sv.edu.udb.clinica.ui.componentes.Tarjeta;
import sv.edu.udb.clinica.ui.componentes.Texto;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.ui.util.ModeloTabla;
import sv.edu.udb.clinica.ui.util.Tabla;
import sv.edu.udb.clinica.ui.util.Tarea;

// Pantalla de inicio con un resumen de la clínica y reportes

public class PanelInicio extends JPanel implements Refrescable {

    private static final int MAXIMO_PROXIMAS = 8;

    private final Servicios servicios;
    private final Consumer<String> navegar;

    private final TarjetaDato datoPacientes = new TarjetaDato("Pacientes registrados", "pacientes", Tema::info);
    private final TarjetaDato datoDoctores = new TarjetaDato("Doctores", "doctores", Tema::primario);
    private final TarjetaDato datoProgramadas = new TarjetaDato("Citas programadas", "citas",
            () -> Color.decode("#D97706"));
    private final TarjetaDato datoAtendidas = new TarjetaDato("Citas atendidas", "atendida",
            () -> Color.decode("#15803D"));

    private final ModeloTabla<CitaMedica> modeloProximas = new ModeloTabla<CitaMedica>()
            .columna("Fecha", LocalDate.class, CitaMedica::getFecha)
            .columna("Hora", LocalTime.class, CitaMedica::getHora)
            .columna("Paciente", String.class, CitaMedica::getNombrePaciente)
            .columna("Doctor", String.class, CitaMedica::getNombreDoctor)
            .columna("Motivo", String.class, CitaMedica::getMotivo);
    private final JTable tablaProximas = Tabla.crear(modeloProximas);
    private final Texto lblSinProximas = Texto.secundario("No hay citas programadas próximamente.");
    private final GraficoBarras barrasEstado = new GraficoBarras();
    private final GraficoBarras barrasEspecialidad = new GraficoBarras();

    public PanelInicio(Servicios servicios, Consumer<String> navegar) {
        super(new BorderLayout(0, 18));
        this.servicios = servicios;
        this.navegar = navegar;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 20, 28));

        JPanel datos = new JPanel(new GridLayout(1, 4, 16, 0));
        datos.setOpaque(false);
        datos.add(datoPacientes);
        datos.add(datoDoctores);
        datos.add(datoProgramadas);
        datos.add(datoAtendidas);

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);
        centro.add(datos, BorderLayout.NORTH);
        centro.add(crearReportes(), BorderLayout.CENTER);

        add(crearBanner(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    // Construcción de la pantalla

    // Banner de bienvenida con los accesos rápidos
    private JPanel crearBanner() {
        JPanel texto = new JPanel();
        texto.setOpaque(false);
        texto.setLayout(new BoxLayout(texto, BoxLayout.Y_AXIS));
        texto.add(etiqueta("Clínica Universitaria Vida UDB", "#99F6E4", 0, false));
        texto.add(Box.createVerticalStrut(4));
        texto.add(etiqueta("Gestión de citas médicas", "#FFFFFF", 12, true));
        texto.add(Box.createVerticalStrut(4));
        texto.add(etiqueta(Etiquetas.fechaLarga(LocalDate.now()), "#CCFBF1", 1, false));
        texto.add(Box.createVerticalStrut(16));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        botones.add(botonBanner("Programar cita", "citas", "citas", true));
        botones.add(Box.createHorizontalStrut(10));
        botones.add(botonBanner("Registrar paciente", "paciente-nuevo", "pacientes", false));
        botones.add(Box.createHorizontalStrut(10));
        botones.add(botonBanner("Registrar doctor", "doctores", "doctores", false));
        texto.add(botones);

        JPanel banner = new JPanel(new BorderLayout(20, 0));
        banner.putClientProperty("FlatLaf.style", "arc: 24; background: #0D6D66");
        banner.setBorder(new EmptyBorder(18, 30, 18, 22));
        banner.add(texto, BorderLayout.CENTER);
        banner.add(new JLabel(Iconos.imagen("ilustracion-inicio", 230, 149)), BorderLayout.EAST);
        return banner;
    }

    private JLabel etiqueta(String texto, String color, float tamano, boolean negrita) {
        Texto etiqueta = new Texto(texto, Color.decode(color));
        etiqueta.setAlignmentX(LEFT_ALIGNMENT);
        if (negrita) {
            etiqueta.negrita();
        }
        return etiqueta.tamano(tamano);
    }

    private JButton botonBanner(String texto, String icono, String destino, boolean principal) {
        JButton b;
        if (principal) {
            b = new JButton(texto, Iconos.color(icono, 18, Color.decode("#0F766E")));
            b.putClientProperty("FlatLaf.style", "background: #FFFFFF; foreground: #0F766E; "
                    + "hoverBackground: #F0FDFA; pressedBackground: #CCFBF1; borderWidth: 0");
        } else {
            b = new JButton(texto, Iconos.blanco(icono));
            b.putClientProperty("FlatLaf.style", "background: #1A8F85; foreground: #FFFFFF; "
                    + "hoverBackground: #22A094; pressedBackground: #167B72; borderWidth: 0");
        }
        b.setFocusable(false);
        b.addActionListener(e -> navegar.accept(destino));
        return b;
    }

    private JComponent crearReportes() {
        tablaProximas.setRowSelectionAllowed(false);
        tablaProximas.setFocusable(false);
        Tabla.anchos(tablaProximas, 90, 60, 170, 170, 150);

        JPanel contenidoTabla = new JPanel(new BorderLayout());
        contenidoTabla.setOpaque(false);
        contenidoTabla.add(Tabla.envolver(tablaProximas), BorderLayout.CENTER);
        lblSinProximas.setBorder(new EmptyBorder(8, 0, 0, 0));
        contenidoTabla.add(lblSinProximas, BorderLayout.SOUTH);

        Tarjeta proximas = new Tarjeta(new BorderLayout(0, 12), 20);
        proximas.add(Texto.fuerte("Próximas citas").negrita().tamano(2), BorderLayout.NORTH);
        proximas.add(contenidoTabla, BorderLayout.CENTER);

        JPanel derecha = new JPanel(new GridLayout(2, 1, 0, 18));
        derecha.setOpaque(false);
        derecha.add(tarjetaGrafico("Citas por estado", barrasEstado));
        derecha.add(tarjetaGrafico("Citas por especialidad", barrasEspecialidad));

        // La tabla ocupa el 62% del ancho y los gráficos el resto
        JPanel reportes = new JPanel(new GridBagLayout());
        reportes.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 0.62;
        gbc.insets = new Insets(0, 0, 0, 18);
        reportes.add(proximas, gbc);
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 0, 0, 0);
        reportes.add(derecha, gbc);
        return reportes;
    }

    private Tarjeta tarjetaGrafico(String titulo, GraficoBarras grafico) {
        Tarjeta tarjeta = new Tarjeta(new BorderLayout(0, 8), 20);
        tarjeta.add(Texto.fuerte(titulo).negrita().tamano(2), BorderLayout.NORTH);
        tarjeta.add(grafico, BorderLayout.CENTER);
        return tarjeta;
    }

    // Datos

    @Override
    public void refrescar() {
        Tarea.ejecutar(servicios::cargarTodo, this::mostrar,
                error -> Mensajes.errorSilencioso("Cargar resumen de inicio", error));
    }

    private void mostrar(Servicios.Datos d) {
        LocalDate hoy = LocalDate.now();
        int programadas = 0;
        int atendidas = 0;
        int canceladas = 0;
        int paraHoy = 0;
        List<CitaMedica> proximas = new ArrayList<>();
        for (CitaMedica c : d.citas()) {
            if (c.getEstado() == Estado.PROGRAMADA) {
                programadas++;
                if (hoy.equals(c.getFecha())) {
                    paraHoy++;
                }
                if (!c.getFecha().isBefore(hoy)) {
                    proximas.add(c);
                }
            } else if (c.getEstado() == Estado.ATENDIDA) {
                atendidas++;
            } else if (c.getEstado() == Estado.CANCELADA) {
                canceladas++;
            }
        }

        Set<String> especialidades = new HashSet<>();
        Map<Integer, String> especialidadDe = new HashMap<>();
        for (Doctor doc : d.doctores()) {
            especialidades.add(doc.getEspecialidad());
            especialidadDe.put(doc.getIdDoctor(), doc.getEspecialidad());
        }

        datoPacientes.mostrar(String.valueOf(d.pacientes().size()), "En el sistema");
        datoDoctores.mostrar(String.valueOf(d.doctores().size()),
                Etiquetas.plural(especialidades.size(), "especialidad", "especialidades"));
        datoProgramadas.mostrar(String.valueOf(programadas), paraHoy == 0 ? "Ninguna para hoy" : paraHoy + " para hoy");
        datoAtendidas.mostrar(String.valueOf(atendidas), Etiquetas.plural(canceladas, "cancelada", "canceladas"));

        // Las próximas citas se ordenan por fecha y hora, y solo se muestran las primeras
        proximas.sort(Comparator.comparing(CitaMedica::getFecha).thenComparing(CitaMedica::getHora));
        if (proximas.size() > MAXIMO_PROXIMAS) {
            proximas = proximas.subList(0, MAXIMO_PROXIMAS);
        }
        modeloProximas.setDatos(proximas);
        lblSinProximas.setVisible(proximas.isEmpty());

        List<Barra> porEstado = new ArrayList<>();
        porEstado.add(new Barra("Programadas", programadas, Tema::info));
        porEstado.add(new Barra("Atendidas", atendidas, Tema::exito));
        porEstado.add(new Barra("Canceladas", canceladas, Tema::peligro));
        barrasEstado.setBarras(porEstado);

        // Cantidad de citas de cada especialidad, se muestran las 4 con más citas
        Map<String, Integer> citasPorEspecialidad = new HashMap<>();
        for (CitaMedica c : d.citas()) {
            String especialidad = especialidadDe.getOrDefault(c.getIdDoctor(), "Sin especialidad");
            citasPorEspecialidad.put(especialidad, citasPorEspecialidad.getOrDefault(especialidad, 0) + 1);
        }
        List<Map.Entry<String, Integer>> ordenadas = new ArrayList<>(citasPorEspecialidad.entrySet());
        ordenadas.sort((a, b) -> b.getValue() - a.getValue());
        List<Barra> porEspecialidad = new ArrayList<>();
        for (int i = 0; i < Math.min(4, ordenadas.size()); i++) {
            porEspecialidad.add(new Barra(ordenadas.get(i).getKey(), ordenadas.get(i).getValue(), Tema::primario));
        }
        barrasEspecialidad.setBarras(porEspecialidad);
    }

    // Componentes internos

    // Una barra del gráfico, el color se pide cada vez para seguir el tema
    private record Barra(String etiqueta, int valor, Supplier<Color> color) {
    }

    // Tarjeta con un número grande, su descripción y un icono dentro de un cuadro de color
    private static final class TarjetaDato extends Tarjeta {

        private final Texto valor = Texto.fuerte("–").negrita().tamano(14);
        private final Texto detalle = Texto.secundario(" ");

        TarjetaDato(String etiqueta, String icono, Supplier<Color> color) {
            super(new BorderLayout(16, 0), 18);
            JPanel texto = new JPanel(new GridLayout(3, 1));
            texto.setOpaque(false);
            texto.add(Texto.secundario(etiqueta));
            texto.add(valor);
            texto.add(detalle);
            // Con GridBagLayout el cuadro conserva su tamaño y queda centrado en vertical
            JPanel lado = new JPanel(new GridBagLayout());
            lado.setOpaque(false);
            lado.add(new Medallon(icono, color));
            add(lado, BorderLayout.WEST);
            add(texto, BorderLayout.CENTER);
        }

        void mostrar(String numero, String descripcion) {
            valor.setText(numero);
            detalle.setText(descripcion);
        }
    }

    // Cuadro redondeado de color con un icono blanco
    private static final class Medallon extends JLabel {

        private final Supplier<Color> color;

        Medallon(String icono, Supplier<Color> color) {
            super(Iconos.color(icono, 24, Color.WHITE), SwingConstants.CENTER);
            this.color = color;
            setOpaque(true);
            setBackground(color.get());
            setPreferredSize(new Dimension(52, 52));
            putClientProperty("FlatLaf.style", "arc: 18");
        }

        // Se llama también al cambiar de tema para tomar el color nuevo
        @Override
        public void updateUI() {
            super.updateUI();
            if (color != null) {
                setBackground(color.get());
            }
        }
    }

    // Barras horizontales para los reportes, cada fila tiene el nombre, una barra y el valor
    private static final class GraficoBarras extends JPanel {

        private List<Barra> barras;

        GraficoBarras() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
        }

        void setBarras(List<Barra> nuevas) {
            barras = nuevas;
            dibujar();
        }

        // Se llama también al cambiar de tema para tomar los colores nuevos
        @Override
        public void updateUI() {
            super.updateUI();
            if (barras != null) {
                dibujar();
            }
        }

        private void dibujar() {
            removeAll();
            // El valor más alto llena la barra completa, el nombre más largo define el ancho de los nombres
            int maximo = 0;
            int anchoNombre = 0;
            for (Barra b : barras) {
                maximo = Math.max(maximo, b.valor());
                anchoNombre = Math.max(anchoNombre, getFontMetrics(getFont()).stringWidth(b.etiqueta()));
            }
            anchoNombre = Math.min(150, anchoNombre) + 14;

            if (maximo == 0) {
                Texto vacio = Texto.secundario("Sin datos todavía.");
                vacio.setAlignmentX(LEFT_ALIGNMENT);
                vacio.setBorder(new EmptyBorder(6, 0, 0, 0));
                add(vacio);
            } else {
                for (Barra b : barras) {
                    add(crearFila(b, maximo, anchoNombre));
                }
            }
            revalidate();
            repaint();
        }

        private JPanel crearFila(Barra b, int maximo, int anchoNombre) {
            JLabel nombre = new JLabel(b.etiqueta());
            nombre.setPreferredSize(new Dimension(anchoNombre, 20));
            JLabel valor = new JLabel(String.valueOf(b.valor()), SwingConstants.RIGHT);
            valor.setPreferredSize(new Dimension(34, 20));

            JProgressBar barra = new JProgressBar(0, maximo);
            barra.setValue(b.valor());
            barra.setForeground(b.color().get());
            barra.setBackground(Tema.borde());
            barra.setPreferredSize(new Dimension(100, 12));
            barra.putClientProperty("FlatLaf.style", "arc: 12");

            // Cada fila mide entre 20 y 34 de alto según el espacio que haya
            JPanel fila = new JPanel(new BorderLayout());
            fila.setOpaque(false);
            fila.setAlignmentX(LEFT_ALIGNMENT);
            fila.setPreferredSize(new Dimension(200, 34));
            fila.setMinimumSize(new Dimension(0, 20));
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            fila.add(nombre, BorderLayout.WEST);
            fila.add(centrarEnAlto(barra), BorderLayout.CENTER);
            fila.add(valor, BorderLayout.EAST);
            return fila;
        }

        // Pone un componente en el centro vertical de su espacio sin estirarlo
        private JPanel centrarEnAlto(JComponent componente) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setOpaque(false);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(componente, gbc);
            return panel;
        }
    }
}

package sv.edu.udb.clinica.ui.componentes;

import com.formdev.flatlaf.ui.FlatLineBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Escucha;
import sv.edu.udb.clinica.ui.util.FiltroDocumento;

// Campo para escribir una fecha (dd/mm/aaaa) con un botón que abre un calendario

public class CampoFecha extends JPanel implements CampoValidable {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final String[] MESES = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
        "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    private static final String[] DIAS = {"L", "M", "M", "J", "V", "S", "D"};

    private final JTextField texto = new JTextField();
    private final JButton boton = new JButton(Iconos.get("citas"));
    private final JLabel tituloMes = new JLabel("", SwingConstants.CENTER);
    private final JPanel cuadricula = new JPanel(new GridLayout(0, 7, 2, 2));
    private LocalDate minimo;
    private LocalDate maximo;
    private YearMonth mes;
    private JPopupMenu popup;

    public CampoFecha() {
        super(new BorderLayout(6, 0));
        setOpaque(false);

        texto.putClientProperty("JTextField.placeholderText", "dd/mm/aaaa");
        FiltroDocumento.mascara(texto, "##/##/####");

        boton.setFocusable(false);
        boton.setToolTipText("Abrir calendario");
        boton.addActionListener(e -> mostrarCalendario());

        add(texto, BorderLayout.CENTER);
        add(boton, BorderLayout.EAST);
    }

    // Fecha escrita, o null si está vacía o incompleta
    public LocalDate getFecha() {
        try {
            return LocalDate.parse(texto.getText(), FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public void setFecha(LocalDate fecha) {
        texto.setText(fecha == null ? "" : FORMATO.format(fecha));
    }

    public boolean estaVacio() {
        return texto.getText().isEmpty();
    }

    // Días que se pueden elegir en el calendario (null deja ese lado sin límite)
    public void setRango(LocalDate minimo, LocalDate maximo) {
        this.minimo = minimo;
        this.maximo = maximo;
    }

    @Override
    public void marcarError(boolean error) {
        texto.putClientProperty("JComponent.outline", error ? "error" : null);
    }

    @Override
    public void alCambiar(Runnable accion) {
        Escucha.alEscribir(texto, accion);
    }

    @Override
    public void setEnabled(boolean habilitado) {
        super.setEnabled(habilitado);
        texto.setEnabled(habilitado);
        boton.setEnabled(habilitado);
    }

    @Override
    public boolean requestFocusInWindow() {
        return texto.requestFocusInWindow();
    }

    // Calendario

    private void mostrarCalendario() {
        LocalDate inicial = getFecha();
        mes = YearMonth.from(inicial != null ? inicial : LocalDate.now());

        tituloMes.setFont(tituloMes.getFont().deriveFont(Font.BOLD));
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.add(botonMes("anterior", -1), BorderLayout.WEST);
        encabezado.add(tituloMes, BorderLayout.CENTER);
        encabezado.add(botonMes("siguiente", 1), BorderLayout.EAST);

        cuadricula.setOpaque(false);
        JPanel calendario = new JPanel(new BorderLayout(0, 8));
        calendario.setBorder(new EmptyBorder(12, 12, 12, 12));
        calendario.setPreferredSize(new Dimension(7 * 36 + 24, 7 * 32 + 60));
        calendario.add(encabezado, BorderLayout.NORTH);
        calendario.add(cuadricula, BorderLayout.CENTER);
        llenarCuadricula();

        popup = new JPopupMenu();
        popup.add(calendario);
        popup.show(texto, 0, texto.getHeight() + 4);
    }

    private JButton botonMes(String icono, int cambio) {
        JButton b = new JButton(Iconos.get(icono, 16));
        b.putClientProperty("JButton.buttonType", "toolBarButton");
        b.setFocusable(false);
        b.addActionListener(e -> {
            mes = mes.plusMonths(cambio);
            llenarCuadricula();
        });
        return b;
    }

    private void llenarCuadricula() {
        tituloMes.setText(MESES[mes.getMonthValue() - 1] + " " + mes.getYear());
        cuadricula.removeAll();
        for (String dia : DIAS) {
            Texto cabecera = Texto.secundario(dia);
            cabecera.setHorizontalAlignment(SwingConstants.CENTER);
            cuadricula.add(cabecera);
        }
        // La semana empieza en lunes, los huecos dejan el día 1 en su columna
        int huecos = mes.atDay(1).getDayOfWeek().getValue() - 1;
        for (int i = 0; i < huecos; i++) {
            cuadricula.add(new JLabel());
        }
        for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
            cuadricula.add(botonDia(mes.atDay(dia)));
        }
        // Siempre seis semanas para que el calendario no cambie de alto al navegar
        for (int celdas = huecos + mes.lengthOfMonth(); celdas < 42; celdas++) {
            cuadricula.add(new JLabel());
        }
        cuadricula.revalidate();
        cuadricula.repaint();
    }

    private JButton botonDia(LocalDate dia) {
        JButton b = new JButton(String.valueOf(dia.getDayOfMonth()));
        b.setFocusable(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setEnabled((minimo == null || !dia.isBefore(minimo)) && (maximo == null || !dia.isAfter(maximo)));
        b.addActionListener(e -> {
            setFecha(dia);
            popup.setVisible(false);
        });

        if (dia.equals(getFecha())) {
            // Día elegido, relleno de color
            b.putClientProperty("FlatLaf.style", "background: $Component.accentColor; foreground: #FFFFFF; "
                    + "hoverBackground: lighten($Component.accentColor,6%); borderWidth: 0");
        } else {
            b.putClientProperty("JButton.buttonType", "toolBarButton");
            b.putClientProperty("FlatLaf.style", "toolbar.hoverBackground: fade($Component.accentColor,16%)");
            if (dia.equals(LocalDate.now())) {
                // El día de hoy lleva un contorno de color
                b.setBorder(new FlatLineBorder(new Insets(1, 1, 1, 1), Tema.primario(), 1.5f, 10));
            }
        }
        return b;
    }
}

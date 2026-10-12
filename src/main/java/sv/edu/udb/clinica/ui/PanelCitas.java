package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.modelo.CitaMedica.Estado;
import sv.edu.udb.clinica.modelo.Doctor;
import sv.edu.udb.clinica.ui.componentes.Botones;
import sv.edu.udb.clinica.ui.componentes.CampoFecha;
import sv.edu.udb.clinica.ui.componentes.CampoForm;
import sv.edu.udb.clinica.ui.componentes.Encabezado;
import sv.edu.udb.clinica.ui.componentes.Formulario;
import sv.edu.udb.clinica.ui.componentes.SelectorPaciente;
import sv.edu.udb.clinica.ui.componentes.Tarjeta;
import sv.edu.udb.clinica.ui.componentes.Texto;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Combos;
import sv.edu.udb.clinica.ui.util.Escucha;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.ui.util.FiltroDocumento;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.ui.util.ModeloTabla;
import sv.edu.udb.clinica.ui.util.Tabla;
import sv.edu.udb.clinica.ui.util.Tarea;
import sv.edu.udb.clinica.ui.util.Validador;

// Programación de citas, formulario a la izquierda y lista con buscador a la derecha

public class PanelCitas extends JPanel implements Refrescable {

    private static final LocalTime HORA_INICIO = LocalTime.of(8, 0);
    private static final LocalTime HORA_FIN = LocalTime.of(17, 0);
    private static final int MINUTOS_POR_CITA = 30;
    private static final int COL_ESTADO = 6;
    private static final String SIN_ELEGIR = "Elija un doctor y una fecha para ver los horarios libres.";

    private final Servicios servicios;

    private List<Doctor> doctores = new ArrayList<>();
    private List<CitaMedica> citas = new ArrayList<>();

    // Formulario
    private final SelectorPaciente selPaciente = new SelectorPaciente();
    private final JComboBox<String> cmbFiltroEspecialidad = Combos.crear("Todas", s -> s);
    private final JComboBox<Doctor> cmbDoctor = Combos.crear("Seleccione un doctor...",
            d -> "Dr(a). " + d.getNombreCompleto() + "  ·  " + d.getEspecialidad(),
            d -> "Dr(a). " + d.getNombreCompleto());
    private final CampoFecha fecCita = new CampoFecha();
    private final JComboBox<LocalTime> cmbHora = Combos.crear("Seleccione...", Etiquetas::hora);
    private final JTextField txtMotivo = new JTextField();
    private final JComboBox<Estado> cmbEstado = Combos.crear(null, Etiquetas::estado);
    private final JTextArea txtObservaciones = new JTextArea(3, 20);
    private final Texto lblDisponibilidad = Texto.secundario(SIN_ELEGIR);

    private final CampoForm cfPaciente = new CampoForm("Paciente", selPaciente, true)
            .validador(() -> selPaciente.getPaciente() == null ? "Seleccione un paciente." : null);
    private final CampoForm cfFiltro = new CampoForm("Especialidad", cmbFiltroEspecialidad);
    private final CampoForm cfDoctor = new CampoForm("Doctor", cmbDoctor, true)
            .validador(() -> cmbDoctor.getSelectedItem() == null ? "Seleccione un doctor." : null);
    private final CampoForm cfFecha = new CampoForm("Fecha", fecCita, true)
            .validador(this::validarFecha);
    private final CampoForm cfHora = new CampoForm("Hora", cmbHora, true)
            .validador(() -> cmbHora.getSelectedItem() == null ? "Seleccione una hora." : null);
    private final CampoForm cfMotivo = new CampoForm("Motivo de consulta", txtMotivo, true)
            .validador(() -> Validador.texto(txtMotivo.getText()).length() < 3
                    ? "Describa brevemente el motivo." : null);
    private final CampoForm cfEstado = new CampoForm("Estado", cmbEstado);
    private final CampoForm cfObservaciones = new CampoForm("Observaciones", crearAreaObservaciones());

    // Campos que se validan al guardar
    private final List<CampoForm> obligatorios = List.of(cfPaciente, cfDoctor, cfFecha, cfHora, cfMotivo);

    private final Texto tituloFormulario = Texto.fuerte("Nueva cita").negrita().tamano(3);
    private final JButton btnGuardar = Botones.primario("Programar cita", "guardar");
    private final JButton btnLimpiar = Botones.secundario("Limpiar", "limpiar");

    // Tabla
    private final ModeloTabla<CitaMedica> modelo = new ModeloTabla<CitaMedica>()
            .columna("Código", Integer.class, CitaMedica::getIdCita)
            .columna("Fecha", LocalDate.class, CitaMedica::getFecha)
            .columna("Hora", LocalTime.class, CitaMedica::getHora)
            .columna("Paciente", String.class, CitaMedica::getNombrePaciente)
            .columna("Doctor", String.class, CitaMedica::getNombreDoctor)
            .columna("Motivo", String.class, CitaMedica::getMotivo)
            .columna("Estado", Estado.class, CitaMedica::getEstado);
    private final JTable tabla = Tabla.crear(modelo);
    private final JTextField txtBuscar = Tabla.campoBusqueda("Buscar por paciente, doctor, motivo, fecha...");
    private final JComboBox<Estado> cmbFiltroEstado = Combos.crear("Todos los estados", Etiquetas::estado);
    private final Texto lblTotal = Texto.secundario(" ");
    private final JButton btnAtendida = Botones.exito("Marcar como atendida", "atendida");
    private final JButton btnCancelar = Botones.peligro("Cancelar cita", "cancelar");

    // Cita que se está editando, es null cuando el formulario programa una nueva
    private CitaMedica editando;

    // Es true mientras se llenan las listas por código, para que sus eventos no recalculen todo
    private boolean cargando;

    public PanelCitas(Servicios servicios) {
        super(new BorderLayout());
        this.servicios = servicios;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 16, 28));

        configurarCampos();
        add(Encabezado.crear("Citas médicas", "Programe citas sin choques de horario y controle su estado."),
                BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(18, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(crearTarjetaFormulario(), BorderLayout.WEST);
        cuerpo.add(crearTarjetaTabla(), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);

        modoNuevo();
    }

    // Construcción de la pantalla

    private JComponent crearAreaObservaciones() {
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(txtObservaciones);
        scroll.setPreferredSize(new Dimension(100, 74));
        return scroll;
    }

    private void configurarCampos() {
        FiltroDocumento.texto(txtMotivo, 255);
        txtMotivo.putClientProperty("JTextField.placeholderText", "Ej. Chequeo general");
        txtObservaciones.putClientProperty("JTextField.placeholderText", "Notas del médico (opcional)");
        Combos.reemplazar(cmbEstado, List.of(Estado.values()));
        Combos.reemplazar(cmbFiltroEstado, List.of(Estado.values()));
        lblDisponibilidad.setIcon(Iconos.get("reloj", 16));
        lblDisponibilidad.setIconTextGap(8);
        lblDisponibilidad.setBorder(new EmptyBorder(0, 0, 10, 0));

        cmbFiltroEspecialidad.addActionListener(e -> {
            if (!cargando) {
                filtrarDoctores();
            }
        });
        cmbDoctor.addActionListener(e -> recalcularHorarios());
        fecCita.alCambiar(this::recalcularHorarios);
        cmbHora.setEnabled(false);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnAtendida.addActionListener(e -> cambiarEstado(Estado.ATENDIDA));
        btnCancelar.addActionListener(e -> cambiarEstado(Estado.CANCELADA));
        btnAtendida.setEnabled(false);
        btnCancelar.setEnabled(false);
    }

    private JComponent crearTarjetaFormulario() {
        Formulario formulario = new Formulario()
                .fila(cfPaciente)
                .filaPesos(new double[]{0.36, 0.64}, cfFiltro, cfDoctor)
                .fila(cfFecha, cfHora)
                .fila(lblDisponibilidad)
                .fila(cfMotivo)
                .fila(cfEstado)
                .fila(cfObservaciones);
        return formulario.enTarjeta(tituloFormulario, btnGuardar, btnLimpiar, 450);
    }

    private JComponent crearTarjetaTabla() {
        Escucha.alEscribir(txtBuscar, this::filtrarTabla);
        cmbFiltroEstado.addActionListener(e -> filtrarTabla());
        cmbFiltroEstado.setPreferredSize(new Dimension(170, cmbFiltroEstado.getPreferredSize().height));

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        derecha.add(cmbFiltroEstado);
        derecha.add(lblTotal);

        Tabla.anchos(tabla, 40, 92, 52, 120, 120, 105, 105);
        Tabla.anchoMinimo(tabla, 1, 92);
        Tabla.anchoMinimo(tabla, COL_ESTADO, 105);
        tabla.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING),
                new RowSorter.SortKey(2, SortOrder.ASCENDING)));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CitaMedica c = Tabla.seleccionado(tabla, modelo);
                boolean programada = c != null && c.getEstado() == Estado.PROGRAMADA;
                btnAtendida.setEnabled(programada);
                btnCancelar.setEnabled(programada);
                if (c != null) {
                    cargar(c);
                }
            }
        });

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.setBorder(new EmptyBorder(14, 0, 0, 0));
        acciones.add(btnAtendida);
        acciones.add(btnCancelar);

        Tarjeta tarjeta = Tabla.tarjeta(txtBuscar, derecha, tabla);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }

    // Datos

    @Override
    public void refrescar() {
        Tarea.ejecutar(servicios::cargarTodo, this::aplicarDatos,
                error -> Mensajes.errorSilencioso("Cargar citas", error));
    }

    private void aplicarDatos(Servicios.Datos datos) {
        doctores = datos.doctores();
        citas = datos.citas();
        selPaciente.setPacientes(datos.pacientes());

        cargando = true;
        Set<String> especialidades = new TreeSet<>();
        for (Doctor d : doctores) {
            especialidades.add(d.getEspecialidad());
        }
        Combos.reemplazar(cmbFiltroEspecialidad, especialidades);
        cargando = false;
        filtrarDoctores();

        modelo.setDatos(citas);
        if (editando != null) {
            // Si había una cita en edición se vuelve a seleccionar, y si ya no existe se limpia el formulario
            int id = editando.getIdCita();
            editando = null;
            for (int i = 0; i < citas.size(); i++) {
                if (citas.get(i).getIdCita() == id) {
                    editando = citas.get(i);
                    Tabla.seleccionarFilaDelModelo(tabla, i);
                }
            }
            if (editando == null) {
                limpiar();
            }
        }
        filtrarTabla();
        recalcularHorarios();
    }

    private void filtrarTabla() {
        Tabla.filtrar(tabla, txtBuscar.getText(), COL_ESTADO, cmbFiltroEstado.getSelectedItem());
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        lblTotal.setText(visibles == total ? Etiquetas.plural(total, "cita", "citas") : visibles + " de " + total);
    }

    // Muestra en la lista de doctores solo los de la especialidad elegida
    private void filtrarDoctores() {
        String especialidad = (String) cmbFiltroEspecialidad.getSelectedItem();
        Doctor previo = (Doctor) cmbDoctor.getSelectedItem();
        List<Doctor> lista = new ArrayList<>();
        for (Doctor d : doctores) {
            if (especialidad == null || especialidad.equals(d.getEspecialidad())) {
                lista.add(d);
            }
        }

        cargando = true;
        Combos.reemplazar(cmbDoctor, lista);
        if (previo != null) {
            elegirDoctor(previo.getIdDoctor());
        }
        cargando = false;
        recalcularHorarios();
    }

    private void elegirDoctor(int idDoctor) {
        for (Doctor d : doctores) {
            if (d.getIdDoctor() == idDoctor) {
                cmbDoctor.setSelectedItem(d);
            }
        }
    }

    // Calcula los horarios libres del doctor en la fecha elegida
    private void recalcularHorarios() {
        if (cargando) {
            return;
        }
        Doctor doctor = (Doctor) cmbDoctor.getSelectedItem();
        LocalDate fecha = fecCita.getFecha();
        LocalTime previa = (LocalTime) cmbHora.getSelectedItem();

        if (doctor == null || fecha == null) {
            Combos.reemplazar(cmbHora, List.<LocalTime>of());
            cmbHora.setEnabled(false);
            avisoDisponibilidad(SIN_ELEGIR, false);
            return;
        }

        Set<LocalTime> ocupadas = new HashSet<>();
        for (CitaMedica c : citas) {
            boolean mismaCita = editando != null && c.getIdCita() == editando.getIdCita();
            if (c.getIdDoctor() == doctor.getIdDoctor() && fecha.equals(c.getFecha())
                    && c.getEstado() != Estado.CANCELADA && !mismaCita) {
                ocupadas.add(c.getHora());
            }
        }

        List<LocalTime> libres = new ArrayList<>();
        int totalEspacios = 0;
        for (LocalTime h = HORA_INICIO; h.isBefore(HORA_FIN); h = h.plusMinutes(MINUTOS_POR_CITA)) {
            totalEspacios++;
            boolean pasada = fecha.equals(LocalDate.now()) && !h.isAfter(LocalTime.now());
            if (!ocupadas.contains(h) && !pasada) {
                libres.add(h);
            }
        }
        // Al editar, la hora actual de la cita debe seguir apareciendo aunque ya haya pasado
        if (editando != null && editando.getIdDoctor() == doctor.getIdDoctor()
                && fecha.equals(editando.getFecha()) && !libres.contains(editando.getHora())
                && !ocupadas.contains(editando.getHora())) {
            libres.add(editando.getHora());
            libres.sort(null);
        }

        Combos.reemplazar(cmbHora, libres);
        if (previa != null && libres.contains(previa)) {
            cmbHora.setSelectedItem(previa);
        }
        cmbHora.setEnabled(!libres.isEmpty());
        if (libres.isEmpty()) {
            avisoDisponibilidad("No hay horarios libres para este doctor en esa fecha. Pruebe otra fecha.", true);
        } else {
            avisoDisponibilidad(libres.size() + " de " + totalEspacios + " horarios libres ("
                    + Etiquetas.hora(HORA_INICIO) + " a " + Etiquetas.hora(HORA_FIN) + ", cada "
                    + MINUTOS_POR_CITA + " min).", false);
        }
    }

    private void avisoDisponibilidad(String texto, boolean problema) {
        lblDisponibilidad.setText(texto);
        lblDisponibilidad.setColor(problema ? Tema::peligro : Tema::textoSecundario);
    }

    // Acciones

    private String validarFecha() {
        LocalDate f = fecCita.getFecha();
        if (f == null) {
            return fecCita.estaVacio() ? "Ingrese la fecha." : "Fecha no válida (dd/mm/aaaa).";
        }
        boolean cambioFecha = editando == null || !f.equals(editando.getFecha());
        return cambioFecha && f.isBefore(LocalDate.now()) ? "No puede ser una fecha pasada." : null;
    }

    private void guardar() {
        if (!validarTodo()) {
            Mensajes.validacion("Revise los campos marcados en rojo antes de guardar.");
            return;
        }
        Doctor doctor = (Doctor) cmbDoctor.getSelectedItem();
        CitaMedica cita = new CitaMedica(selPaciente.getPaciente().getIdPaciente(), doctor.getIdDoctor(),
                fecCita.getFecha(), (LocalTime) cmbHora.getSelectedItem(), Validador.texto(txtMotivo.getText()));
        cita.setObservaciones(Validador.nulo(txtObservaciones.getText()));

        boolean nueva = editando == null;
        if (!nueva) {
            cita.setIdCita(editando.getIdCita());
            cita.setEstado((Estado) cmbEstado.getSelectedItem());
        }

        btnGuardar.setEnabled(false);
        Tarea.ejecutar(() -> {
            if (nueva) {
                servicios.citas().registrarCita(cita);
            } else if (!servicios.citas().actualizarCita(cita)) {
                throw new IllegalArgumentException("No se encontró la cita. Actualice la lista e intente de nuevo.");
            }
            return cita;
        }, guardada -> {
            btnGuardar.setEnabled(true);
            Mensajes.exito(nueva
                    ? "Cita programada para el " + Etiquetas.fecha(guardada.getFecha()) + " a las "
                            + Etiquetas.hora(guardada.getHora()) + "."
                    : "Cita actualizada correctamente.");
            limpiar();
            refrescar();
        }, error -> {
            btnGuardar.setEnabled(true);
            Mensajes.error(this, nueva ? "Programar cita" : "Actualizar cita", error);
            // Otro usuario pudo ocupar el horario, por eso se recargan los datos para ver la disponibilidad real
            refrescar();
        });
    }

    // Se validan todos los campos (sin cortar en el primero) para marcar cada error a la vez
    private boolean validarTodo() {
        CampoForm primerError = null;
        for (CampoForm c : obligatorios) {
            if (!c.validar() && primerError == null) {
                primerError = c;
            }
        }
        if (primerError != null) {
            primerError.getCampo().requestFocusInWindow();
        }
        return primerError == null;
    }

    private void cambiarEstado(Estado nuevo) {
        CitaMedica cita = Tabla.seleccionado(tabla, modelo);
        if (cita == null) {
            return;
        }
        boolean atender = nuevo == Estado.ATENDIDA;
        if (atender && cita.getFecha().isAfter(LocalDate.now())) {
            Mensajes.error(this, "Marcar como atendida", new IllegalArgumentException(
                    "No se puede marcar como atendida una cita con fecha futura ("
                            + Etiquetas.fecha(cita.getFecha()) + ")."));
            return;
        }
        String detalle = "la cita de " + cita.getNombrePaciente() + " con " + cita.getNombreDoctor()
                + " del " + Etiquetas.fecha(cita.getFecha()) + " a las " + Etiquetas.hora(cita.getHora());
        boolean acepto = atender
                ? Mensajes.confirmar(this, "Marcar como atendida", "¿Marcar como atendida " + detalle + "?", "Marcar atendida")
                : Mensajes.confirmar(this, "Cancelar cita",
                        "¿Cancelar " + detalle + "? El horario quedará libre para otros pacientes.", "Cancelar cita");
        if (!acepto) {
            return;
        }
        Tarea.ejecutar(() -> servicios.citas().cambiarEstadoCita(cita.getIdCita(), nuevo), ok -> {
            if (!ok) {
                Mensajes.error(this, "Cambiar estado", new IllegalArgumentException(
                        "No se encontró la cita. Actualice la lista e intente de nuevo."));
            } else {
                Mensajes.exito(atender ? "Cita marcada como atendida." : "Cita cancelada. El horario quedó libre.");
            }
            limpiar();
            refrescar();
        }, error -> Mensajes.error(this, "Cambiar estado de la cita", error));
    }

    private void cargar(CitaMedica c) {
        editando = c;
        limpiarErrores();
        cargando = true;
        selPaciente.seleccionarPorId(c.getIdPaciente());
        cmbFiltroEspecialidad.setSelectedItem(null);
        Combos.reemplazar(cmbDoctor, doctores);
        elegirDoctor(c.getIdDoctor());
        fecCita.setRango(null, null);
        fecCita.setFecha(c.getFecha());
        cargando = false;

        recalcularHorarios();
        Combos.seleccionarOAgregar(cmbHora, c.getHora());
        txtMotivo.setText(c.getMotivo());
        cmbEstado.setSelectedItem(c.getEstado());
        cmbEstado.setEnabled(true);
        txtObservaciones.setText(Validador.texto(c.getObservaciones()));

        tituloFormulario.setText("Editar cita #" + c.getIdCita());
        btnGuardar.setText("Guardar cambios");
        btnLimpiar.setText("Cancelar edición");
    }

    private void limpiar() {
        editando = null;
        tabla.clearSelection();
        cargando = true;
        selPaciente.setPaciente(null);
        cmbFiltroEspecialidad.setSelectedItem(null);
        cmbDoctor.setSelectedItem(null);
        fecCita.setFecha(null);
        txtMotivo.setText("");
        txtObservaciones.setText("");
        cargando = false;
        limpiarErrores();
        filtrarDoctores();
        modoNuevo();
        selPaciente.requestFocusInWindow();
    }

    private void modoNuevo() {
        fecCita.setRango(LocalDate.now(), null);
        cmbEstado.setSelectedItem(Estado.PROGRAMADA);
        cmbEstado.setEnabled(false);
        tituloFormulario.setText("Nueva cita");
        btnGuardar.setText("Programar cita");
        btnLimpiar.setText("Limpiar");
        recalcularHorarios();
    }

    private void limpiarErrores() {
        for (CampoForm c : obligatorios) {
            c.limpiarError();
        }
    }
}

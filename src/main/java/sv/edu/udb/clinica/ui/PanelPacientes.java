package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.Paciente;
import sv.edu.udb.clinica.service.PacienteService;
import sv.edu.udb.clinica.ui.componentes.Botones;
import sv.edu.udb.clinica.ui.componentes.CampoFecha;
import sv.edu.udb.clinica.ui.componentes.CampoForm;
import sv.edu.udb.clinica.ui.componentes.Encabezado;
import sv.edu.udb.clinica.ui.componentes.Formulario;
import sv.edu.udb.clinica.ui.componentes.Texto;
import sv.edu.udb.clinica.ui.util.Combos;
import sv.edu.udb.clinica.ui.util.Escucha;
import sv.edu.udb.clinica.ui.util.Etiquetas;
import sv.edu.udb.clinica.ui.util.FiltroDocumento;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.ui.util.ModeloTabla;
import sv.edu.udb.clinica.ui.util.Tabla;
import sv.edu.udb.clinica.ui.util.Tarea;
import sv.edu.udb.clinica.ui.util.Validador;

// Registro y consulta de pacientes, formulario a la izquierda y lista con buscador a la derecha

public class PanelPacientes extends JPanel implements Refrescable {

    private final PacienteService servicio;

    // Formulario
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JTextField txtDui = new JTextField();
    private final JComboBox<String> cmbGenero = Combos.crear("Seleccione...", Etiquetas::genero);
    private final CampoFecha fecNacimiento = new CampoFecha();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JTextField txtDireccion = new JTextField();

    private final CampoForm cfNombres = new CampoForm("Nombres", txtNombres, true)
            .validador(() -> Validador.nombre(txtNombres.getText(), "los nombres"));
    private final CampoForm cfApellidos = new CampoForm("Apellidos", txtApellidos, true)
            .validador(() -> Validador.nombre(txtApellidos.getText(), "los apellidos"));
    private final CampoForm cfDui = new CampoForm("DUI", txtDui)
            .validador(() -> Validador.dui(txtDui.getText()));
    private final CampoForm cfGenero = new CampoForm("Género", cmbGenero, true)
            .validador(() -> cmbGenero.getSelectedItem() == null ? "Seleccione el género." : null);
    private final CampoForm cfFecha = new CampoForm("Fecha de nacimiento", fecNacimiento, true)
            .validador(() -> Validador.fechaNacimiento(fecNacimiento));
    private final CampoForm cfTelefono = new CampoForm("Teléfono", txtTelefono, true)
            .validador(() -> Validador.telefono(txtTelefono.getText()));
    private final CampoForm cfCorreo = new CampoForm("Correo electrónico", txtCorreo)
            .validador(() -> Validador.correo(txtCorreo.getText()));
    private final CampoForm cfDireccion = new CampoForm("Dirección", txtDireccion);

    private final Texto tituloFormulario = Texto.fuerte("Nuevo paciente").negrita().tamano(3);
    private final JButton btnGuardar = Botones.primario("Registrar paciente", "guardar");
    private final JButton btnLimpiar = Botones.secundario("Limpiar", "limpiar");

    // Tabla
    private final ModeloTabla<Paciente> modelo = new ModeloTabla<Paciente>()
            .columna("Código", Integer.class, Paciente::getIdPaciente)
            .columna("Nombre completo", String.class, Paciente::getNombreCompleto)
            .columna("Edad", Integer.class, p -> p.getFechaNacimiento() == null ? null
                    : Validador.edad(p.getFechaNacimiento()))
            .columna("DUI", String.class, p -> Etiquetas.vacio(p.getDui()))
            .columna("Teléfono", String.class, p -> Etiquetas.vacio(p.getTelefono()))
            .columna("Correo", String.class, p -> Etiquetas.vacio(p.getCorreo()));
    private final JTable tabla = Tabla.crear(modelo);
    private final JTextField txtBuscar = Tabla.campoBusqueda("Buscar por nombre, DUI, teléfono, correo...");
    private final Texto lblTotal = Texto.secundario(" ");

    // Paciente que se está editando, es null cuando el formulario registra uno nuevo
    private Paciente editando;

    public PanelPacientes(PacienteService servicio) {
        super(new BorderLayout());
        this.servicio = servicio;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 16, 28));

        configurarCampos();
        add(Encabezado.crear("Pacientes", "Registre y consulte los pacientes de la clínica."), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(18, 0));
        cuerpo.setOpaque(false);
        cuerpo.add(crearTarjetaFormulario(), BorderLayout.WEST);
        cuerpo.add(crearTarjetaTabla(), BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    // Construcción de la pantalla

    private void configurarCampos() {
        FiltroDocumento.letras(txtNombres, 100);
        FiltroDocumento.letras(txtApellidos, 100);
        FiltroDocumento.mascara(txtDui, "########-#");
        FiltroDocumento.mascara(txtTelefono, "####-####");
        FiltroDocumento.texto(txtCorreo, 100);
        FiltroDocumento.texto(txtDireccion, 255);

        txtNombres.putClientProperty("JTextField.placeholderText", "Ej. María José");
        txtApellidos.putClientProperty("JTextField.placeholderText", "Ej. Hernández Rivas");
        txtDui.putClientProperty("JTextField.placeholderText", "00000000-0");
        txtTelefono.putClientProperty("JTextField.placeholderText", "0000-0000");
        txtCorreo.putClientProperty("JTextField.placeholderText", "nombre@correo.com");
        txtDireccion.putClientProperty("JTextField.placeholderText", "Colonia, municipio, departamento");

        Combos.reemplazar(cmbGenero, List.of("M", "F"));
        fecNacimiento.setRango(null, LocalDate.now());
        fecNacimiento.alCambiar(this::mostrarEdad);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private JComponent crearTarjetaFormulario() {
        Formulario formulario = new Formulario()
                .fila(cfNombres, cfApellidos)
                .fila(cfDui, cfGenero)
                .fila(cfFecha, cfTelefono)
                .fila(cfCorreo)
                .fila(cfDireccion);
        return formulario.enTarjeta(tituloFormulario, btnGuardar, btnLimpiar, 430);
    }

    private JComponent crearTarjetaTabla() {
        Escucha.alEscribir(txtBuscar, this::filtrar);
        Tabla.anchos(tabla, 50, 190, 45, 95, 85, 165);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Paciente p = Tabla.seleccionado(tabla, modelo);
                if (p != null) {
                    cargar(p);
                }
            }
        });
        return Tabla.tarjeta(txtBuscar, lblTotal, tabla);
    }

    // Datos

    @Override
    public void refrescar() {
        Tarea.ejecutar(servicio::listarPacientes, lista -> {
            int idEditando = editando == null ? -1 : editando.getIdPaciente();
            modelo.setDatos(lista);
            actualizarTotal();
            // Si había un paciente en edición se vuelve a seleccionar en la tabla
            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getIdPaciente() == idEditando) {
                    Tabla.seleccionarFilaDelModelo(tabla, i);
                }
            }
        }, error -> Mensajes.errorSilencioso("Cargar pacientes", error));
    }

    private void filtrar() {
        Tabla.filtrar(tabla, txtBuscar.getText());
        actualizarTotal();
    }

    private void actualizarTotal() {
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        lblTotal.setText(visibles == total ? Etiquetas.plural(total, "paciente", "pacientes")
                : visibles + " de " + total);
    }

    // Acciones

    private void guardar() {
        if (!validarTodo()) {
            Mensajes.validacion("Revise los campos marcados en rojo antes de guardar.");
            return;
        }
        Paciente paciente = new Paciente(
                Validador.texto(txtNombres.getText()),
                Validador.texto(txtApellidos.getText()),
                Validador.nulo(txtDui.getText()),
                fecNacimiento.getFecha(),
                (String) cmbGenero.getSelectedItem(),
                Validador.nulo(txtTelefono.getText()),
                Validador.nulo(txtCorreo.getText()),
                Validador.nulo(txtDireccion.getText()));
        boolean nuevo = editando == null;
        if (!nuevo) {
            paciente.setIdPaciente(editando.getIdPaciente());
        }

        btnGuardar.setEnabled(false);
        Tarea.ejecutar(() -> {
            if (nuevo) {
                servicio.registrarPaciente(paciente);
            } else if (!servicio.actualizarPaciente(paciente)) {
                throw new IllegalArgumentException("No se encontró el paciente. Actualice la lista e intente de nuevo.");
            }
            return paciente;
        }, guardado -> {
            btnGuardar.setEnabled(true);
            Mensajes.exito("Paciente " + guardado.getNombreCompleto()
                    + (nuevo ? " registrado correctamente." : " actualizado correctamente."));
            limpiar();
            refrescar();
        }, error -> {
            btnGuardar.setEnabled(true);
            Mensajes.error(this, nuevo ? "Registrar paciente" : "Actualizar paciente", error);
        });
    }

    // Se validan todos los campos (sin cortar en el primero) para marcar cada error a la vez
    private boolean validarTodo() {
        CampoForm primerError = null;
        for (CampoForm c : List.of(cfNombres, cfApellidos, cfDui, cfGenero, cfFecha, cfTelefono, cfCorreo)) {
            if (!c.validar() && primerError == null) {
                primerError = c;
            }
        }
        if (primerError != null) {
            primerError.getCampo().requestFocusInWindow();
        }
        return primerError == null;
    }

    private void cargar(Paciente p) {
        editando = p;
        limpiarErrores();
        txtNombres.setText(p.getNombres());
        txtApellidos.setText(p.getApellidos());
        txtDui.setText(Validador.texto(p.getDui()));
        Combos.seleccionarOAgregar(cmbGenero, p.getGenero());
        fecNacimiento.setFecha(p.getFechaNacimiento());
        txtTelefono.setText(Validador.texto(p.getTelefono()));
        txtCorreo.setText(Validador.texto(p.getCorreo()));
        txtDireccion.setText(Validador.texto(p.getDireccion()));
        tituloFormulario.setText("Editar paciente #" + p.getIdPaciente());
        btnGuardar.setText("Guardar cambios");
        btnLimpiar.setText("Cancelar edición");
    }

    private void limpiar() {
        editando = null;
        tabla.clearSelection();
        txtNombres.setText("");
        txtApellidos.setText("");
        txtDui.setText("");
        cmbGenero.setSelectedItem(null);
        fecNacimiento.setFecha(null);
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        limpiarErrores();
        tituloFormulario.setText("Nuevo paciente");
        btnGuardar.setText("Registrar paciente");
        btnLimpiar.setText("Limpiar");
        txtNombres.requestFocusInWindow();
    }

    private void limpiarErrores() {
        for (CampoForm c : List.of(cfNombres, cfApellidos, cfDui, cfGenero, cfFecha, cfTelefono, cfCorreo,
                cfDireccion)) {
            c.limpiarError();
        }
    }

    // Muestra la edad calculada junto al título del campo de fecha de nacimiento
    private void mostrarEdad() {
        LocalDate f = fecNacimiento.getFecha();
        String titulo = "Fecha de nacimiento *";
        if (f != null && !f.isAfter(LocalDate.now())) {
            titulo += "  (" + Etiquetas.plural(Validador.edad(f), "año", "años") + ")";
        }
        cfFecha.getEtiqueta().setText(titulo);
    }
}

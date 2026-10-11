package sv.edu.udb.clinica.ui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import sv.edu.udb.clinica.modelo.Doctor;
import sv.edu.udb.clinica.service.DoctorService;
import sv.edu.udb.clinica.ui.componentes.Botones;
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

// Registro y consulta de doctores, formulario a la izquierda y lista con buscador a la derecha

public class PanelDoctores extends JPanel implements Refrescable {

    // Especialidades que atiende la clínica según el enunciado
    public static final List<String> ESPECIALIDADES =
            List.of("Medicina General", "Psicología", "Nutrición", "Fisioterapia");

    private final DoctorService servicio;

    // Formulario
    private final JTextField txtNombres = new JTextField();
    private final JTextField txtApellidos = new JTextField();
    private final JComboBox<String> cmbEspecialidad = Combos.crear("Seleccione...", s -> s);
    private final JTextField txtJvpm = new JTextField();
    private final JTextField txtTelefono = new JTextField();
    private final JTextField txtCorreo = new JTextField();

    private final CampoForm cfNombres = new CampoForm("Nombres", txtNombres, true)
            .validador(() -> Validador.nombre(txtNombres.getText(), "los nombres"));
    private final CampoForm cfApellidos = new CampoForm("Apellidos", txtApellidos, true)
            .validador(() -> Validador.nombre(txtApellidos.getText(), "los apellidos"));
    private final CampoForm cfEspecialidad = new CampoForm("Especialidad", cmbEspecialidad, true)
            .validador(() -> cmbEspecialidad.getSelectedItem() == null ? "Seleccione la especialidad." : null);
    private final CampoForm cfJvpm = new CampoForm("N.º de JVPM", txtJvpm, true)
            .validador(() -> Validador.jvpm(txtJvpm.getText()));
    private final CampoForm cfTelefono = new CampoForm("Teléfono", txtTelefono, true)
            .validador(() -> Validador.telefono(txtTelefono.getText()));
    private final CampoForm cfCorreo = new CampoForm("Correo electrónico", txtCorreo)
            .validador(() -> Validador.correo(txtCorreo.getText()));

    private final Texto tituloFormulario = Texto.fuerte("Nuevo doctor").negrita().tamano(3);
    private final JButton btnGuardar = Botones.primario("Registrar doctor", "guardar");
    private final JButton btnLimpiar = Botones.secundario("Limpiar", "limpiar");

    // Tabla
    private final ModeloTabla<Doctor> modelo = new ModeloTabla<Doctor>()
            .columna("Código", Integer.class, Doctor::getIdDoctor)
            .columna("Nombre completo", String.class, Doctor::getNombreCompleto)
            .columna("Especialidad", String.class, Doctor::getEspecialidad)
            .columna("JVPM", String.class, Doctor::getNumeroJvpm)
            .columna("Teléfono", String.class, d -> Etiquetas.vacio(d.getTelefono()))
            .columna("Correo", String.class, d -> Etiquetas.vacio(d.getCorreo()));
    private final JTable tabla = Tabla.crear(modelo);
    private final JTextField txtBuscar = Tabla.campoBusqueda("Buscar por nombre, especialidad, JVPM...");
    private final Texto lblTotal = Texto.secundario(" ");

    // Doctor que se está editando, es null cuando el formulario registra uno nuevo
    private Doctor editando;

    public PanelDoctores(DoctorService servicio) {
        super(new BorderLayout());
        this.servicio = servicio;
        setOpaque(false);
        setBorder(new EmptyBorder(24, 28, 16, 28));

        configurarCampos();
        add(Encabezado.crear("Doctores", "Registre y consulte el personal médico de la clínica."), BorderLayout.NORTH);

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
        FiltroDocumento.digitos(txtJvpm, 10);
        FiltroDocumento.mascara(txtTelefono, "####-####");
        FiltroDocumento.texto(txtCorreo, 100);

        txtNombres.putClientProperty("JTextField.placeholderText", "Ej. Roberto Ernesto");
        txtApellidos.putClientProperty("JTextField.placeholderText", "Ej. Aguilar Sánchez");
        txtJvpm.putClientProperty("JTextField.placeholderText", "Ej. 12345");
        txtTelefono.putClientProperty("JTextField.placeholderText", "0000-0000");
        txtCorreo.putClientProperty("JTextField.placeholderText", "nombre@clinica.com");

        Combos.reemplazar(cmbEspecialidad, ESPECIALIDADES);

        btnGuardar.addActionListener(e -> guardar());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private JComponent crearTarjetaFormulario() {
        Formulario formulario = new Formulario()
                .fila(cfNombres, cfApellidos)
                .fila(cfEspecialidad, cfJvpm)
                .fila(cfTelefono)
                .fila(cfCorreo);
        return formulario.enTarjeta(tituloFormulario, btnGuardar, btnLimpiar, 430);
    }

    private JComponent crearTarjetaTabla() {
        Escucha.alEscribir(txtBuscar, this::filtrar);
        Tabla.anchos(tabla, 50, 180, 120, 60, 85, 150);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Doctor d = Tabla.seleccionado(tabla, modelo);
                if (d != null) {
                    cargar(d);
                }
            }
        });
        return Tabla.tarjeta(txtBuscar, lblTotal, tabla);
    }

    // Datos

    @Override
    public void refrescar() {
        Tarea.ejecutar(servicio::listarDoctores, lista -> {
            int idEditando = editando == null ? -1 : editando.getIdDoctor();
            modelo.setDatos(lista);
            actualizarTotal();
            // Si había un doctor en edición se vuelve a seleccionar en la tabla
            for (int i = 0; i < lista.size(); i++) {
                if (lista.get(i).getIdDoctor() == idEditando) {
                    Tabla.seleccionarFilaDelModelo(tabla, i);
                }
            }
        }, error -> Mensajes.errorSilencioso("Cargar doctores", error));
    }

    private void filtrar() {
        Tabla.filtrar(tabla, txtBuscar.getText());
        actualizarTotal();
    }

    private void actualizarTotal() {
        int visibles = tabla.getRowCount();
        int total = modelo.getRowCount();
        lblTotal.setText(visibles == total ? Etiquetas.plural(total, "doctor", "doctores")
                : visibles + " de " + total);
    }

    // Acciones

    private void guardar() {
        if (!validarTodo()) {
            Mensajes.validacion("Revise los campos marcados en rojo antes de guardar.");
            return;
        }
        Doctor doctor = new Doctor(
                Validador.texto(txtNombres.getText()),
                Validador.texto(txtApellidos.getText()),
                (String) cmbEspecialidad.getSelectedItem(),
                Validador.texto(txtJvpm.getText()),
                Validador.nulo(txtTelefono.getText()),
                Validador.nulo(txtCorreo.getText()));
        boolean nuevo = editando == null;
        if (!nuevo) {
            doctor.setIdDoctor(editando.getIdDoctor());
        }

        btnGuardar.setEnabled(false);
        Tarea.ejecutar(() -> {
            if (nuevo) {
                servicio.registrarDoctor(doctor);
            } else if (!servicio.actualizarDoctor(doctor)) {
                throw new IllegalArgumentException("No se encontró el doctor. Actualice la lista e intente de nuevo.");
            }
            return doctor;
        }, guardado -> {
            btnGuardar.setEnabled(true);
            Mensajes.exito("Doctor " + guardado.getNombreCompleto()
                    + (nuevo ? " registrado correctamente." : " actualizado correctamente."));
            limpiar();
            refrescar();
        }, error -> {
            btnGuardar.setEnabled(true);
            Mensajes.error(this, nuevo ? "Registrar doctor" : "Actualizar doctor", error);
        });
    }

    // Se validan todos los campos (sin cortar en el primero) para marcar cada error a la vez
    private boolean validarTodo() {
        CampoForm primerError = null;
        for (CampoForm c : List.of(cfNombres, cfApellidos, cfEspecialidad, cfJvpm, cfTelefono, cfCorreo)) {
            if (!c.validar() && primerError == null) {
                primerError = c;
            }
        }
        if (primerError != null) {
            primerError.getCampo().requestFocusInWindow();
        }
        return primerError == null;
    }

    private void cargar(Doctor d) {
        editando = d;
        limpiarErrores();
        txtNombres.setText(d.getNombres());
        txtApellidos.setText(d.getApellidos());
        // Si el doctor tiene una especialidad que no está en la lista (dato anterior) se conserva
        Combos.seleccionarOAgregar(cmbEspecialidad, d.getEspecialidad());
        txtJvpm.setText(d.getNumeroJvpm());
        txtTelefono.setText(Validador.texto(d.getTelefono()));
        txtCorreo.setText(Validador.texto(d.getCorreo()));
        tituloFormulario.setText("Editar doctor #" + d.getIdDoctor());
        btnGuardar.setText("Guardar cambios");
        btnLimpiar.setText("Cancelar edición");
    }

    private void limpiar() {
        editando = null;
        tabla.clearSelection();
        txtNombres.setText("");
        txtApellidos.setText("");
        cmbEspecialidad.setSelectedItem(null);
        txtJvpm.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        limpiarErrores();
        tituloFormulario.setText("Nuevo doctor");
        btnGuardar.setText("Registrar doctor");
        btnLimpiar.setText("Limpiar");
        txtNombres.requestFocusInWindow();
    }

    private void limpiarErrores() {
        for (CampoForm c : List.of(cfNombres, cfApellidos, cfEspecialidad, cfJvpm, cfTelefono, cfCorreo)) {
            c.limpiarError();
        }
    }
}

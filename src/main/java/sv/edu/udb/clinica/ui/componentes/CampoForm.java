package sv.edu.udb.clinica.ui.componentes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.function.Supplier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Escucha;

// Un campo del formulario con su título arriba y el mensaje de error abajo

public class CampoForm extends JPanel {

    private final Texto etiqueta;
    private final Texto error;
    private final JComponent campo;
    private Supplier<String> validador;
    private boolean conError;

    public CampoForm(String titulo, JComponent campo, boolean obligatorio) {
        super(new BorderLayout(0, 4));
        this.campo = campo;
        setOpaque(false);

        etiqueta = Texto.secundario(obligatorio ? titulo + " *" : titulo);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, etiqueta.getFont().getSize2D() - 1f));

        // Si el mensaje no cabe en la columna se recorta en lugar de ensanchar
        error = new Texto(" ", Tema::peligro) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(0, super.getPreferredSize().height);
            }
        };
        error.setFont(error.getFont().deriveFont(error.getFont().getSize2D() - 1f));

        add(etiqueta, BorderLayout.NORTH);
        add(campo, BorderLayout.CENTER);
        add(error, BorderLayout.SOUTH);

        conectarRevalidacion();
    }

    public CampoForm(String titulo, JComponent campo) {
        this(titulo, campo, false);
    }

    public CampoForm validador(Supplier<String> validador) {
        this.validador = validador;
        return this;
    }

    public JComponent getCampo() {
        return campo;
    }

    // Ejecuta la validación y devuelve true si el dato es correcto
    public boolean validar() {
        String mensaje = validador == null ? null : validador.get();
        setError(mensaje);
        return mensaje == null;
    }

    public void setError(String mensaje) {
        conError = mensaje != null;
        error.setText(mensaje == null ? " " : mensaje);
        error.setToolTipText(mensaje);
        if (campo instanceof CampoValidable validable) {
            validable.marcarError(conError);
        } else {
            campo.putClientProperty("JComponent.outline", conError ? "error" : null);
        }
    }

    public void limpiarError() {
        setError(null);
    }

    // Mientras el campo tiene un error se vuelve a validar cada vez que el usuario lo cambia
    private void conectarRevalidacion() {
        Runnable revalidar = () -> {
            if (conError) {
                validar();
            }
        };
        if (campo instanceof CampoValidable validable) {
            validable.alCambiar(revalidar);
        } else if (campo instanceof JTextComponent texto) {
            Escucha.alEscribir(texto, revalidar);
        } else if (campo instanceof JComboBox<?> combo) {
            combo.addActionListener(e -> revalidar.run());
        }
    }

    public JLabel getEtiqueta() {
        return etiqueta;
    }
}

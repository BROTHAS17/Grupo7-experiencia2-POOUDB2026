package sv.edu.udb.clinica;

import javax.swing.SwingUtilities;
import sv.edu.udb.clinica.ui.VentanaPrincipal;
import sv.edu.udb.clinica.ui.tema.Iconos;
import sv.edu.udb.clinica.ui.tema.Tema;
import sv.edu.udb.clinica.ui.util.Mensajes;
import sv.edu.udb.clinica.util.LogErrores;


// Punto de entrada de la aplicación de escritorio
// Se ejecuta esta clase o el comando mvn compile seguido de exec java
// Se recomienda compile porque Maven debe descargar FlatLaf y algunos IDE como NetBeans no lo hacen solos

public class Main {

    public static void main(String[] args) {
        // Cualquier excepción que no se haya controlado queda en el log y se avisa en pantalla
        // en lugar de que la aplicación falle en silencio
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) ->
                SwingUtilities.invokeLater(() -> Mensajes.errorSilencioso("Error no controlado", error)));

        SwingUtilities.invokeLater(() -> {
            Tema.instalar();
            Iconos.registrarFiltroDeColor();
            LogErrores.getInstancia().info("Aplicación", "Aplicación iniciada.");
            new VentanaPrincipal().setVisible(true);
        });
    }
}

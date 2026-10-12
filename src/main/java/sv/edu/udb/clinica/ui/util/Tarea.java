package sv.edu.udb.clinica.ui.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

// Ejecuta el acceso a la base de datos en un hilo aparte, para que la ventana no se congele
// mientras MySQL responde, y entrega el resultado (o el error) en el hilo de Swing

public final class Tarea {

    private Tarea() {
    }

    public static <T> void ejecutar(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.call();
            }

            @Override
            protected void done() {
                try {
                    alTerminar.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alFallar.accept(e);
                } catch (ExecutionException e) {
                    alFallar.accept(e.getCause() != null ? e.getCause() : e);
                }
            }
        }.execute();
    }
}

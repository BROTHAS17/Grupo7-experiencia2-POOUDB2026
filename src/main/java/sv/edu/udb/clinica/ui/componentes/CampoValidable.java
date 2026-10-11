package sv.edu.udb.clinica.ui.componentes;

// Componente compuesto (por ejemplo un campo de fecha con botón) que puede mostrarse en rojo
// cuando tiene un error y que avisa cuando su valor cambia

public interface CampoValidable {

    void marcarError(boolean error);

    void alCambiar(Runnable accion);
}

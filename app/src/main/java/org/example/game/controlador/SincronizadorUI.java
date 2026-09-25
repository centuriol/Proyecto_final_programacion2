package org.example.game.controlador;

import org.example.game.ui.ControlTiempoWidget;
import org.example.game.ui.HUDRecursosTop;

/**
 * SRP: Encapsula la sincronización visual de los componentes de la interfaz de usuario tras cada tick.
 * Elimina callbacks anónimos y funciones sueltas, centralizando la actualización de la UI.
 */
public class SincronizadorUI implements Runnable {

    private final HUDRecursosTop hudRecursos;
    private final ControlTiempoWidget controlTiempo;

    public SincronizadorUI(HUDRecursosTop hudRecursos, ControlTiempoWidget controlTiempo) {
        this.hudRecursos = hudRecursos;
        this.controlTiempo = controlTiempo;
    }

    @Override
    public void run() {
        if (hudRecursos != null) {
            hudRecursos.actualizar();
        }
        if (controlTiempo != null) {
            controlTiempo.actualizarTick();
        }
    }
}

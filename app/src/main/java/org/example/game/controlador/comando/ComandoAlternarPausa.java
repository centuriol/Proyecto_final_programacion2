package org.example.game.controlador.comando;

import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.ControlTiempoWidget;

/**
 * SRP: Encapsula la acción de alternar el estado de pausa de la simulación
 * y sincronizar la selección visual en el widget de control de tiempo.
 */
public class ComandoAlternarPausa implements Comando {

    private final SimulacionSolar simulacion;
    private final ControlTiempoWidget controlTiempo;

    public ComandoAlternarPausa(SimulacionSolar simulacion, ControlTiempoWidget controlTiempo) {
        this.simulacion = simulacion;
        this.controlTiempo = controlTiempo;
    }

    @Override
    public void ejecutar() {
        if (simulacion != null) {
            simulacion.togglePause();
        }
        if (controlTiempo != null) {
            controlTiempo.actualizarSeleccion();
        }
    }
}

package org.example.game.controlador.comando;

import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.ControlTiempoWidget;

/**
 * Clase base para comandos que coordinan la simulación con el widget de control temporal.
 *
 * Principios SOLID:
 * - SRP: Encapsula la asociacion de la simulacion y el widget de tiempo para comandos de ejecucion/pausa.
 * - DRY: Centraliza referencias duplicadas entre comandos temporales.
 */
public abstract class ComandoSimulacionConTiempo implements Comando {

    protected final SimulacionSolar simulacion;
    protected final ControlTiempoWidget controlTiempo;

    public ComandoSimulacionConTiempo(SimulacionSolar simulacion, ControlTiempoWidget controlTiempo) {
        this.simulacion = simulacion;
        this.controlTiempo = controlTiempo;
    }

    protected void actualizarTickWidget() {
        if (controlTiempo != null) {
            controlTiempo.actualizarTick();
        }
    }
}

package org.example.game.controlador.comando;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.ControlTiempoWidget;
/**
 * SRP: Encapsula la acción de avanzar un único tick (paso discreto)
 * en la simulación física y actualizar el contador de ticks en la interfaz.
 */
public class ComandoAvanzarPaso extends ComandoSimulacionConTiempo {

    public ComandoAvanzarPaso(SimulacionSolar simulacion, ControlTiempoWidget controlTiempo) {
        super(simulacion, controlTiempo);
    }

    @Override
    public void ejecutar() {
        if (simulacion != null) {
            simulacion.step();
        }
        actualizarTickWidget();
    }
}

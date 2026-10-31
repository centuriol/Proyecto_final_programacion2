package org.example.game.controlador.comando;
import org.example.game.guardado.GestorGuardadoPartida; import org.example.game.render.ControlZoom; import org.example.game.simulacion.SimulacionSolar;
/**
 * Comando que ejecuta la carga rapida de la partida guardada (SRP / Patron Command).
 */
public class ComandoCargarPartida extends ComandoPersistencia {

    public ComandoCargarPartida(GestorGuardadoPartida gestorGuardado, SimulacionSolar simulacion, ControlZoom controlZoom) {
        super(gestorGuardado, simulacion, controlZoom);
    }

    @Override
    protected void ejecutarAccion() {
        gestorGuardado.cargarPartidaRapida(simulacion, controlZoom);
    }
}

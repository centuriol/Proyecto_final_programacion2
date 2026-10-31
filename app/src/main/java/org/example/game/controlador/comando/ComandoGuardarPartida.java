package org.example.game.controlador.comando;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.render.ControlZoom;
import org.example.game.simulacion.SimulacionSolar;
/**
 * Comando que ejecuta el guardado rapido de la partida actual (SRP / Patron Command).
 */
public class ComandoGuardarPartida extends ComandoPersistencia {

    public ComandoGuardarPartida(GestorGuardadoPartida gestorGuardado, SimulacionSolar simulacion, ControlZoom controlZoom) {
        super(gestorGuardado, simulacion, controlZoom);
    }

    @Override
    protected void ejecutarAccion() {
        gestorGuardado.guardarPartidaRapida(simulacion, controlZoom);
    }
}

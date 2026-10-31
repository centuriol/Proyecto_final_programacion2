package org.example.game.controlador.comando;
import org.example.game.guardado.ExcepcionPersistenciaPartida; import org.example.game.guardado.GestorGuardadoPartida; import org.example.game.render.ControlZoom; import org.example.game.simulacion.SimulacionSolar;
/**
 * Clase base abstracta para comandos que ejecutan operaciones de persistencia de partida.
 *
 * Principios SOLID:
 * - SRP: Centraliza las referencias y el manejo uniforme de errores de persistencia.
 * - OCP: Permite agregar nuevos comandos de persistencia implementando ejecutarAccion().
 */
public abstract class ComandoPersistencia implements Comando {

    protected final GestorGuardadoPartida gestorGuardado;
    protected final SimulacionSolar simulacion;
    protected final ControlZoom controlZoom;

    protected ComandoPersistencia(GestorGuardadoPartida gestorGuardado, SimulacionSolar simulacion, ControlZoom controlZoom) {
        this.gestorGuardado = gestorGuardado;
        this.simulacion = simulacion;
        this.controlZoom = controlZoom;
    }

    @Override
    public final void ejecutar() {
        if (gestorGuardado == null || simulacion == null) {
            return;
        }
        try {
            ejecutarAccion();
        } catch (ExcepcionPersistenciaPartida ignored) {
            // El gestor ya emite la notificacion correspondiente en la simulacion
        }
    }

    protected abstract void ejecutarAccion();
}

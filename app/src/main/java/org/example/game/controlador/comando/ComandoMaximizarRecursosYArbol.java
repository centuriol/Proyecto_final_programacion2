package org.example.game.controlador.comando;

import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;

/**
 * Comando de acceso rapido (Ctrl + M) que otorga 100,000 de cada recurso
 * y desbloquea el arbol de investigacion tecnologica al 100% (SRP / Command).
 */
public class ComandoMaximizarRecursosYArbol implements Comando {

    private static final double CANTIDAD_RECURSOS = 100_000.0;

    private final SimulacionSolar simulacion;
    private final BarraInventarioHotbar hotbar;
    private final Runnable callbackRefresco;

    public ComandoMaximizarRecursosYArbol(SimulacionSolar simulacion) {
        this(simulacion, null, null);
    }

    public ComandoMaximizarRecursosYArbol(SimulacionSolar simulacion, BarraInventarioHotbar hotbar) {
        this(simulacion, hotbar, null);
    }

    public ComandoMaximizarRecursosYArbol(SimulacionSolar simulacion,
                                          BarraInventarioHotbar hotbar,
                                          Runnable callbackRefresco) {
        this.simulacion = simulacion;
        this.hotbar = hotbar;
        this.callbackRefresco = callbackRefresco;
    }

    @Override
    public void ejecutar() {
        if (simulacion == null) {
            return;
        }
        InventarioJugador inventario = simulacion.getInventario();
        if (inventario != null) {
            for (TipoRecurso tipo : TipoRecurso.values()) {
                inventario.agregarRecurso(tipo, CANTIDAD_RECURSOS);
            }
        }

        SistemaProgresion progresion = simulacion.getProgresion();
        if (progresion != null) {
            progresion.desbloquearTodo();
        }

        if (hotbar != null) {
            hotbar.actualizarBloqueos();
        }
        if (callbackRefresco != null) {
            callbackRefresco.run();
        }

        if (simulacion.getBusEventos() != null) {
            simulacion.getBusEventos().notificarCambioEstado();
            simulacion.getBusEventos().notificarListenersUI();
        }
        simulacion.notificarEvento(
                "Atajo Ctrl+M activado: +100000 recursos y arbol mejorado al 100%.",
                MensajeEvento.TipoMensaje.EXITO
        );
    }
}

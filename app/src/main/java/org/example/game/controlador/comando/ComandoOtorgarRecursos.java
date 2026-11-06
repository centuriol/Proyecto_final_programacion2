package org.example.game.controlador.comando;

import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

/**
 * SRP: Comando para otorgar una cantidad fija de cada recurso al inventario del jugador (atajo Ctrl+M).
 */
public class ComandoOtorgarRecursos implements Comando {

    public static final double CANTIDAD_POR_DEFECTO = 100_000.0;

    private final SimulacionSolar simulacion;
    private final double cantidad;

    public ComandoOtorgarRecursos(SimulacionSolar simulacion, double cantidad) {
        this.simulacion = simulacion;
        this.cantidad = cantidad;
    }

    public ComandoOtorgarRecursos(SimulacionSolar simulacion) {
        this(simulacion, CANTIDAD_POR_DEFECTO);
    }

    @Override
    public void ejecutar() {
        if (simulacion == null || simulacion.getInventario() == null || cantidad <= 0) return;

        InventarioJugador inventario = simulacion.getInventario();
        for (TipoRecurso tipo : TipoRecurso.values()) {
            inventario.agregarRecurso(tipo, cantidad);
        }

        if (simulacion.getBusEventos() != null) {
            simulacion.getBusEventos().notificarCambioEstado();
            simulacion.getBusEventos().notificarListenersUI();
        }
        simulacion.notificarEvento(
                String.format("+%.0f de cada recurso otorgados.", cantidad),
                MensajeEvento.TipoMensaje.EXITO
        );
    }
}

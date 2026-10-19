package org.example.game.controlador.comando;

import org.example.game.jugador.TipoRecurso;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;

/**
 * [TEMPORAL - Remover antes de la entrega final]
 *
 * Comando de debug que al ejecutarse:
 * 1. Da el maximo de todos los tipos de recurso al jugador.
 * 2. Alterna (toggle) la visualizacion del anillo de radio
 *    de activacion del easter egg alrededor de estrellas.
 *
 * Se activa con Ctrl+M. Ver ControladorTeclado.handle().
 */
public class ComandoDebugMaxRecursos implements Comando {

    private final SimulacionSolar simulacion;
    private final RenderizadorPixelArt renderizador;

    public ComandoDebugMaxRecursos(SimulacionSolar simulacion, RenderizadorPixelArt renderizador) {
        this.simulacion = simulacion;
        this.renderizador = renderizador;
    }

    @Override
    public void ejecutar() {
        // 1. Dar maximo de todos los recursos
        double maxRecurso = 99999.0;
        for (TipoRecurso tr : TipoRecurso.values()) {
            simulacion.getInventario().getRecursoObj(tr).setCantidad(maxRecurso);
        }

        // 2. Toggle del anillo de radio de easter egg
        renderizador.toggleAnilloEasterEgg();

        simulacion.notificarEvento(
                "[DEBUG] Recursos maximizados. Anillo easter egg: " + (renderizador.isMostrarAnilloEasterEgg() ? "visible" : "oculto"),
                org.example.game.simulacion.MensajeEvento.TipoMensaje.INFO
        );
    }
}

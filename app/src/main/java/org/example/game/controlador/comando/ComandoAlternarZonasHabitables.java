package org.example.game.controlador.comando;

import org.example.game.render.RenderizadorPixelArt;

/**
 * SRP: Encapsula la acción de alternar la visualización de las zonas habitables alrededor de las estrellas.
 */
public class ComandoAlternarZonasHabitables extends ComandoRenderizador {

    public ComandoAlternarZonasHabitables(RenderizadorPixelArt renderizador) {
        super(renderizador);
    }

    @Override
    public void ejecutar() {
        if (renderizador != null) {
            renderizador.toggleZonasHabitables();
        }
    }
}

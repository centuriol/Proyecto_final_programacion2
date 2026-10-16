package org.example.game.controlador.comando;

import org.example.game.render.RenderizadorPixelArt;

/**
 * SRP: Encapsula la acción de alternar la visualización de la cuadrícula o grilla espacial.
 */
public class ComandoAlternarGrilla extends ComandoRenderizador {

    public ComandoAlternarGrilla(RenderizadorPixelArt renderizador) {
        super(renderizador);
    }

    @Override
    public void ejecutar() {
        if (renderizador != null) {
            renderizador.toggleGrilla();
        }
    }
}

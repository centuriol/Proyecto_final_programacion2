package org.example.game.controlador.comando;

import org.example.game.render.RenderizadorPixelArt;

/**
 * SRP: Encapsula la acción de alternar la visualización de la cuadrícula o grilla espacial.
 */
public class ComandoAlternarGrilla implements Comando {

    private final RenderizadorPixelArt renderizador;

    public ComandoAlternarGrilla(RenderizadorPixelArt renderizador) {
        this.renderizador = renderizador;
    }

    @Override
    public void ejecutar() {
        if (renderizador != null) {
            renderizador.toggleGrilla();
        }
    }
}

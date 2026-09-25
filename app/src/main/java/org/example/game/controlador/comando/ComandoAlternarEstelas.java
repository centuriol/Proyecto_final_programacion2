package org.example.game.controlador.comando;

import org.example.game.render.RenderizadorPixelArt;

/**
 * SRP: Encapsula la acción de alternar el trazado de estelas orbitales detrás de los cuerpos.
 */
public class ComandoAlternarEstelas implements Comando {

    private final RenderizadorPixelArt renderizador;

    public ComandoAlternarEstelas(RenderizadorPixelArt renderizador) {
        this.renderizador = renderizador;
    }

    @Override
    public void ejecutar() {
        if (renderizador != null) {
            renderizador.toggleEstelas();
        }
    }
}

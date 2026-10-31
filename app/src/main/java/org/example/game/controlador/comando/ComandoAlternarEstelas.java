package org.example.game.controlador.comando;
import org.example.game.render.RenderizadorPixelArt;
/**
 * SRP: Encapsula la acción de alternar el trazado de estelas orbitales detrás de los cuerpos.
 */
public class ComandoAlternarEstelas extends ComandoRenderizador {

    public ComandoAlternarEstelas(RenderizadorPixelArt renderizador) {
        super(renderizador);
    }

    @Override
    public void ejecutar() {
        if (renderizador != null) {
            renderizador.toggleEstelas();
        }
    }
}

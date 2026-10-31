package org.example.game.render;
import javafx.scene.canvas.GraphicsContext; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo;
/**
 * Estrategia de dibujo para un tipo especifico de cuerpo celeste.
 *
 * Principios SOLID:
 * - OCP: Permite incorporar nuevos tipos de renderizado visual sin modificar el renderizador principal.
 * - ISP: Interfaz funcional minima de dibujo.
 */
@FunctionalInterface
public interface RenderizadorCuerpo {
    void dibujar(GraphicsContext gc, CuerpoCeleste cuerpo, TipoCuerpo tipo, double radio, long tick);
}

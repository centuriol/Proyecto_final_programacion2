package org.example.game.render.cuerpo;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.UtilidadRender;

/**
 * Renderizador Pixel Art para satélites espaciales artificiales (SRP / OCP).
 */
public class RenderizadorSatelite implements RenderizadorCuerpo {

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste cuerpo, TipoCuerpo tipo, double r, long tick) {
        gc.setFill(Color.BLACK);
        gc.fillRect(-5, -5, 10, 10);
        gc.setFill(UtilidadRender.colorWeb("#e2e8f0"));
        gc.fillRect(-4, -4, 8, 8);

        gc.setFill(UtilidadRender.colorWeb("#2b6cb0"));
        gc.fillRect(-14, -3, 8, 6);
        gc.fillRect(6, -3, 8, 6);

        gc.setStroke(UtilidadRender.colorWeb("#ffd700"));
        gc.setLineWidth(1.0);
        gc.strokeRect(-14, -3, 8, 6);
        gc.strokeRect(6, -3, 8, 6);

        if ((tick / 8) % 2 == 0) {
            gc.setFill(UtilidadRender.colorWeb("#5be3ff"));
            gc.fillRect(-1, -7, 2, 2);
        }
    }
}

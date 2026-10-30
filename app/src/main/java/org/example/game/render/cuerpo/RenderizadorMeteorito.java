package org.example.game.render.cuerpo;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.UtilidadRender;

/**
 * Renderizador Pixel Art para meteoritos y cometas (SRP / OCP).
 */
public class RenderizadorMeteorito implements RenderizadorCuerpo {

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste m, TipoCuerpo tipo, double r, long tick) {
        if (m != null) {
            Vector2D vel = m.getVelocidad();
            if (vel.magnitud() > 0.01) {
                Vector2D dirOpuesta = vel.normalizar().multiplicar(-1.0);
                double lenCola = Math.min(40.0, vel.magnitud() * 0.04);
                double fx = dirOpuesta.x() * lenCola;
                double fy = dirOpuesta.y() * lenCola;

                gc.setStroke(UtilidadRender.colorRgb(255, 69, 0, 0.65));
                gc.setLineWidth(5.0);
                gc.strokeLine(0, 0, fx, fy);

                gc.setStroke(UtilidadRender.colorRgb(255, 215, 0, 0.9));
                gc.setLineWidth(2.5);
                gc.strokeLine(0, 0, fx * 0.6, fy * 0.6);
            }
        }

        UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb("#718096"));
        gc.setFill(UtilidadRender.colorWeb("#4a5568"));
        gc.fillRect(-r * 0.4, -r * 0.2, r * 0.5, r * 0.5);
    }
}

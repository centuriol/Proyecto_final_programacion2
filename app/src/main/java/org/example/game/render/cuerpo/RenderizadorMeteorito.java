package org.example.game.render.cuerpo;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.render.RenderizadorCuerpo;
import org.example.game.render.UtilidadRender;
/**
 * Renderizador Pixel Art para meteoritos con estela y contorno proporcionales a su radio (SRP / OCP).
 */
public class RenderizadorMeteorito implements RenderizadorCuerpo {

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste m, TipoCuerpo tipo, double r, long tick) {
        double radioEfectivo = Math.max(1.0, r);
        if (m != null) {
            Vector2D vel = m.getVelocidad();
            if (vel.magnitud() > 0.01) {
                Vector2D dirOpuesta = vel.normalizar().multiplicar(-1.0);
                double lenCola = Math.min(radioEfectivo * 4.5, Math.max(radioEfectivo * 2.0, vel.magnitud() * 0.04));
                double fx = dirOpuesta.x() * lenCola;
                double fy = dirOpuesta.y() * lenCola;

                gc.setStroke(UtilidadRender.colorRgb(255, 69, 0, 0.65));
                gc.setLineWidth(Math.max(1.2, radioEfectivo * 0.70));
                gc.strokeLine(0, 0, fx, fy);

                gc.setStroke(UtilidadRender.colorRgb(255, 215, 0, 0.90));
                gc.setLineWidth(Math.max(0.8, radioEfectivo * 0.35));
                gc.strokeLine(0, 0, fx * 0.6, fy * 0.6);
            }
        }

        double borde = 1.0;
        gc.setFill(Color.BLACK);
        gc.fillOval(-radioEfectivo - borde, -radioEfectivo - borde, (radioEfectivo + borde) * 2.0, (radioEfectivo + borde) * 2.0);
        gc.setFill(UtilidadRender.colorWeb("#718096"));
        gc.fillOval(-radioEfectivo, -radioEfectivo, radioEfectivo * 2.0, radioEfectivo * 2.0);
        gc.setFill(UtilidadRender.colorWeb("#4a5568"));
        gc.fillRect(-radioEfectivo * 0.4, -radioEfectivo * 0.2, radioEfectivo * 0.5, radioEfectivo * 0.5);
    }
}

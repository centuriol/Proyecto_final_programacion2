package org.example.game.render.cuerpo;
import javafx.scene.canvas.GraphicsContext; import javafx.scene.paint.Color; import javafx.scene.shape.ArcType; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.render.RenderizadorCuerpo; import org.example.game.render.UtilidadRender;
/**
 * Renderizador Pixel Art para agujeros negros con horizonte de sucesos y disco de acreción (SRP / OCP).
 */
public class RenderizadorAgujeroNegro implements RenderizadorCuerpo {

    @Override
    public void dibujar(GraphicsContext gc, CuerpoCeleste cuerpo, TipoCuerpo tipo, double r, long tick) {
        double rDisco = r * 2.2;
        double anguloGiro = tick * 2.5;

        gc.setStroke(UtilidadRender.colorRgb(189, 147, 249, 0.7));
        gc.setLineWidth(3.0);
        gc.strokeArc(-rDisco, -rDisco * 0.6, rDisco * 2, rDisco * 1.2, anguloGiro, 120, ArcType.OPEN);

        gc.setStroke(UtilidadRender.colorRgb(91, 227, 255, 0.85));
        gc.setLineWidth(2.0);
        gc.strokeArc(-rDisco * 0.8, -rDisco * 0.5, rDisco * 1.6, rDisco * 1.0, anguloGiro + 180, 140, ArcType.OPEN);

        gc.setFill(Color.BLACK);
        gc.fillOval(-r, -r, r * 2, r * 2);

        gc.setStroke(UtilidadRender.colorWeb("#bd93f9"));
        gc.setLineWidth(1.5);
        gc.strokeOval(-r, -r, r * 2, r * 2);
    }
}

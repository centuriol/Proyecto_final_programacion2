package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.UtilidadRender;

import java.util.List;

/**
 * Capa de renderizado para trayectorias proyectadas y predicciones de órbita (SRP).
 */
public class CapaTrayectorias {

    private final ConversorCoordenadas conversor;

    public CapaTrayectorias() {
        this(new TransformadorCoordenadas());
    }

    public CapaTrayectorias(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : new TransformadorCoordenadas();
    }

    public void dibujar(GraphicsContext gc, List<Vector2D> trayectoria, double anchoCanvas, double altoCanvas) {
        if (trayectoria == null || trayectoria.isEmpty()) return;

        gc.setLineWidth(2.0);
        gc.setLineCap(StrokeLineCap.ROUND);

        Vector2D prev = null;
        int i = 0;
        int total = trayectoria.size();

        for (Vector2D ptFisica : trayectoria) {
            double x = conversor.fisicaAXJavaFX(ptFisica.x(), anchoCanvas);
            double y = conversor.fisicaAYJavaFX(ptFisica.y(), altoCanvas);

            if (prev != null) {
                double x0 = conversor.fisicaAXJavaFX(prev.x(), anchoCanvas);
                double y0 = conversor.fisicaAYJavaFX(prev.y(), altoCanvas);

                double alpha = 1.0 - ((double) i / total) * 0.75;
                gc.setStroke(UtilidadRender.colorRgb(91, 227, 255, alpha));
                if (i % 2 == 0) {
                    gc.strokeLine(x0, y0, x, y);
                }
            }
            prev = ptFisica;
            i++;
        }
    }
}

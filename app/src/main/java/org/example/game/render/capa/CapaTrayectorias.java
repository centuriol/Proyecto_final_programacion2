package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.render.UtilidadRender;
import java.util.List;
/**
 * Capa de renderizado para trayectorias proyectadas y predicciones de órbita (SRP).
 */
public class CapaTrayectorias {

    public void dibujar(GraphicsContext gc, List<Vector2D> trayectoria, double anchoCanvas, double altoCanvas) {
        if (trayectoria == null || trayectoria.isEmpty()) return;

        gc.setLineWidth(2.0);
        gc.setLineCap(StrokeLineCap.ROUND);

        Vector2D prev = null;
        int i = 0;
        int total = trayectoria.size();

        for (Vector2D ptFisica : trayectoria) {
            double x = ConstantesFisicas.fisicaAXJavaFX(ptFisica.x(), anchoCanvas);
            double y = ConstantesFisicas.fisicaAYJavaFX(ptFisica.y(), altoCanvas);

            if (prev != null) {
                double x0 = ConstantesFisicas.fisicaAXJavaFX(prev.x(), anchoCanvas);
                double y0 = ConstantesFisicas.fisicaAYJavaFX(prev.y(), altoCanvas);

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

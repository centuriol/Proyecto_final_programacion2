package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import org.example.game.render.UtilidadRender;

/**
 * Responsable de renderizar la cúpula visual y pulso de escudos planetarios (SRP).
 */
public class CapaEscudoPlanetario {

    public void dibujarCupula(GraphicsContext gc, double cx, double cy, double r, long tick) {
        if (gc == null) return;
        double rEfectivo = r * 1.5;
        double pulso = 0.85 + 0.15 * Math.sin(tick * 0.2);

        gc.setFill(UtilidadRender.colorRgb(91, 227, 255, 0.20 * pulso));
        gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

        gc.setStroke(UtilidadRender.colorWeb("#5be3ff"));
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);
        gc.strokeOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);
        gc.setLineDashes(null);
    }
}

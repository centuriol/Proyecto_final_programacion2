package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.render.UtilidadRender;
/**
 * Capa de renderizado para el indicador de selección y enfoque sobre cuerpos celestes (SRP).
 */
public class CapaSeleccion {

    private static final Color COLOR_ACENTO_CYAN = UtilidadRender.colorWeb("#5be3ff");

    public void dibujar(GraphicsContext gc, CuerpoCeleste c, double anchoCanvas, double altoCanvas) {
        if (c == null) return;
        double cx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas);
        double cy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas);
        double r = c.getRadio() + 6.0;

        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(2.0);

        double len = 6.0;
        // Top-left
        gc.strokeLine(cx - r, cy - r + len, cx - r, cy - r);
        gc.strokeLine(cx - r, cy - r, cx - r + len, cy - r);
        // Top-right
        gc.strokeLine(cx + r - len, cy - r, cx + r, cy - r);
        gc.strokeLine(cx + r, cy - r, cx + r, cy - r + len);
        // Bottom-left
        gc.strokeLine(cx - r, cy + r - len, cx - r, cy + r);
        gc.strokeLine(cx - r, cy + r, cx - r + len, cy + r);
        // Bottom-right
        gc.strokeLine(cx + r - len, cy + r, cx + r, cy + r);
        gc.strokeLine(cx + r, cy + r, cx + r, cy + r - len);
    }
}

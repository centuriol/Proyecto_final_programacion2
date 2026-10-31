package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext; import javafx.scene.paint.Color; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.motor.ConstantesFisicas; import org.example.game.render.UtilidadRender; import java.util.List;
/**
 * Capa de renderizado para radios y áreas de atracción gravitacional de cuerpos (SRP).
 */
public class CapaAreaAtraccion {

    private static final Color COLOR_AREA_ESTRELLA = UtilidadRender.colorRgb(255, 215, 0, 0.16);
    private static final Color COLOR_AREA_LUNA = UtilidadRender.colorRgb(200, 210, 225, 0.15);
    private static final Color COLOR_AREA_PLANETA = UtilidadRender.colorRgb(91, 227, 255, 0.14);

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            if (!c.esSimulado()) continue;
            if (c.getTipoCuerpo() == TipoCuerpo.SATELITE || c.getTipoCuerpo() == TipoCuerpo.METEORITO) continue;

            double rAtraccion = c.getRadioAtraccion();
            if (rAtraccion <= 0) continue;

            double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
            double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);

            Color colorArea = (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) ? COLOR_AREA_ESTRELLA
                    : (c.getTipoCuerpo() == TipoCuerpo.LUNA) ? COLOR_AREA_LUNA : COLOR_AREA_PLANETA;

            gc.setStroke(colorArea);
            gc.setLineWidth(1.0);
            gc.setLineDashes(3, 5);
            gc.strokeOval(sx - rAtraccion, sy - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);
            gc.setLineDashes(null);
        }
    }
}

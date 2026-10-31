package org.example.game.render.capa;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.render.UtilidadRender;
import java.util.List;
/**
 * Capa de renderizado para radios y áreas de atracción gravitacional de cuerpos (SRP).
 */
public class CapaAreaAtraccion {

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            if (!c.esSimulado()) continue;
            TipoCuerpo tipo = c.getTipoCuerpo();
            org.example.game.render.EstrategiaVisualTipo est = org.example.game.render.RegistroEstrategiasVisuales.get(tipo);
            if (!est.tieneAreaAtraccion()) continue;

            double rAtraccion = c.getRadioAtraccion();
            if (rAtraccion <= 0) continue;

            double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas);
            double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas);

            Color colorArea = est.getColorAreaAtraccion();

            gc.setStroke(colorArea);
            gc.setLineWidth(1.0);
            gc.setLineDashes(3, 5);
            gc.strokeOval(sx - rAtraccion, sy - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);
            gc.setLineDashes(null);
        }
    }
}

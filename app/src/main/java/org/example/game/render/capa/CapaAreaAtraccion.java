package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.render.EstrategiaVisualTipo;
import org.example.game.render.RegistroEstrategiasVisuales;

import java.util.List;

/**
 * Capa de renderizado para radios y áreas de atracción gravitacional de cuerpos (SRP).
 */
public class CapaAreaAtraccion {

    private final ConversorCoordenadas conversor;

    public CapaAreaAtraccion() {
        this(new TransformadorCoordenadas());
    }

    public CapaAreaAtraccion(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : new TransformadorCoordenadas();
    }

    public void dibujar(GraphicsContext gc, List<CuerpoCeleste> cuerpos, double anchoCanvas, double altoCanvas) {
        for (CuerpoCeleste c : cuerpos) {
            if (!c.esSimulado()) continue;
            TipoCuerpo tipo = c.getTipoCuerpo();
            EstrategiaVisualTipo est = RegistroEstrategiasVisuales.get(tipo);
            if (!est.tieneAreaAtraccion()) continue;

            double rAtraccion = c.getRadioAtraccion();
            if (rAtraccion <= 0) continue;

            double sx = conversor.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas);
            double sy = conversor.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas);

            Color colorArea = est.getColorAreaAtraccion();

            gc.setStroke(colorArea);
            gc.setLineWidth(1.0);
            gc.setLineDashes(3, 5);
            gc.strokeOval(sx - rAtraccion, sy - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);
            gc.setLineDashes(null);
        }
    }
}

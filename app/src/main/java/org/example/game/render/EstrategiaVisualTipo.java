package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;

/**
 * Estrategia compuesta de renderizado visual y métricas geométricas por tipo de cuerpo (ISP / SRP / OCP).
 */
public interface EstrategiaVisualTipo extends MetricasVisualesTipo {
    boolean tieneAreaAtraccion();
    Color getColorAreaAtraccion();
    void dibujarSuperficie(GraphicsContext gc, CuerpoCeleste c, double r);
}
